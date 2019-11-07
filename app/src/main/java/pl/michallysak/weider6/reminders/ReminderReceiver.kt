package pl.michallysak.weider6.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import kotlin.random.Random


class ReminderReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        val nextTime = PreferenceManager.getDefaultSharedPreferences(context).getString("reminders_time", "18:00") as String

        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            Reminder(context as Context).start(nextTime)
        }else if (intent?.action == null){
            ReminderNotification(context as Context).notify(Random(22).nextInt(Int.MAX_VALUE))
            Reminder(context).start(nextTime)
        }

    }

}
