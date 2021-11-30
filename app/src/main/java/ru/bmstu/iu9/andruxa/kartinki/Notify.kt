import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import ru.bmstu.iu9.andruxa.kartinki.MainActivity
import ru.bmstu.iu9.andruxa.kartinki.R

class Notify() {
  private var notificationId = mutableStateOf(0)

  fun send(context: Context) {
    val resultIntent = Intent(context, MainActivity::class.java)
    // Create the TaskStackBuilder
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
      // Add the intent, which inflates the back stack
      addNextIntentWithParentStack(resultIntent)
      // Get the PendingIntent containing the entire back stack
      getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    val builder = NotificationCompat.Builder(context, "kartinki")
      .setSmallIcon(R.drawable.ic_launcher_background)
      .setContentTitle("Давно тебя не было в уличных гонках!")
      .setContentText("Kartinki, которые вы пропустили")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(resultPendingIntent)
      .setAutoCancel(true)
    notificationId = mutableStateOf(notificationId.value+1)
    with(NotificationManagerCompat.from(context)) {
      // notificationId is a unique int for each notification that you must define
      notify(notificationId.value, builder.build())
    }
  }
}