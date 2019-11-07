package pl.michallysak.weider6.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import pl.michallysak.weider6.R
import android.app.PendingIntent
import android.content.Intent
import androidx.core.content.ContextCompat
import pl.michallysak.weider6.ui.main.MainActivity


class ReminderNotification(val context: Context) {

    private val channelID = "training_reminders_channel_ID"
    private val channelName = context.getString(R.string.reminder_about_training)

    private var manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        val contentIntent: PendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(context.getString(R.string.reminder_about_training))
            //.setContentText()
            .setSmallIcon(R.drawable.app_icon)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setColorized(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.setCategory(Notification.CATEGORY_REMINDER)
        }

        return notification.build()
    }

    fun notify(id: Int){
        manager.notify(id, getNotification())
    }

}
