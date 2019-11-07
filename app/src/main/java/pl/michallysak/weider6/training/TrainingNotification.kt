package pl.michallysak.weider6.training

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import pl.michallysak.weider6.R
import pl.michallysak.weider6.ui.training.TrainingActivity


class TrainingNotification(val context: Context) {

    private val channelID = "training_foreground_channel_ID"
    private val channelName = context.getString(R.string.reminder_about_training)

    private var manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var lastContentText = ""
    private var lastContentTitle = ""

    init {
        createChannel()

    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }

    fun getNotification(isRunning: Boolean, contentTitle: String?, contentText: String?): Notification {

        val notification = NotificationCompat.Builder(context, channelID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.app_icon)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setColorized(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
            )
            .setLargeIcon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_notification, null)?.toBitmap())
            .setContentIntent(
                PendingIntent.getActivity(context, 0, Intent(context, TrainingActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)


        val intent = Intent(context, TrainingService::class.java)
        intent.action = "pl.michallysak.weider6.training.TRAINING_START_STOP"

        if (isRunning) {
            notification.addAction(R.drawable.ic_pause, "Pause", PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        } else {
            notification.addAction(R.drawable.ic_play, "Play", PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        }

        if (contentTitle == null) {
            notification.setContentTitle(lastContentTitle)
        } else {
            notification.setContentTitle(contentTitle)
            lastContentTitle = contentTitle
        }

        if (contentText == null) {
            notification.setContentText(lastContentText)
        } else {
            notification.setContentText(contentText)
            lastContentText = contentText
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.setCategory(Notification.CATEGORY_SERVICE)
        }

        return notification.build()
    }

    fun notify(id: Int, notification: Notification) {
        manager.notify(id, notification)
    }


}
