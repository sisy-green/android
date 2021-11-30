package ru.bmstu.iu9.andruxa.kartinki

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.Duration

const val CHANNEL_ID = "kartinki"
class NotificationService : Service() {
  private val timer =
    object : CountDownTimer(Duration.ofDays(5).toMillis(), Duration.ofHours(10).toMillis()) {
      override fun onTick(millisUntilFinished: Long) {
        send()
      }
      override fun onFinish() {
        send()
      }
    }
  private var notificationId = mutableStateOf(0)
  fun send(context: Context = applicationContext) {
     val resultIntent = Intent(context, MainActivity::class.java)
     val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
      addNextIntentWithParentStack(resultIntent)
      getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

     val builder = NotificationCompat.Builder(context, "kartinki")
      .setSmallIcon(R.drawable.ic_launcher_background)
      .setContentTitle("Давно тебя не было в уличных гонках!")
      .setContentText("Kartinki, которые вы пропустили")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(resultPendingIntent)
      .setAutoCancel(true)
    notificationId = mutableStateOf(notificationId.value + 1)
    with(NotificationManagerCompat.from(context)) {
      notify(notificationId.value, builder.build())
    }
  }
  override fun onCreate() {
    HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
      start()
      timer.start()
    }
  }
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}

