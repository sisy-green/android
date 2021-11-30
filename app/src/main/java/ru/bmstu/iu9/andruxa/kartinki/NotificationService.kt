package ru.bmstu.iu9.andruxa.kartinki

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.Duration

const val CHANNEL_ID = "kartinki"
class NotificationService : Service() {
  private fun createNotificationChannel(CHANNEL_ID: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.channel_name)
      val descriptionText = getString(R.string.channel_description)
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
      }
      val notificationManager: NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }
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
      createNotificationChannel(CHANNEL_ID)
      timer.start()
    }
  }
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}

