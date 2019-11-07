package pl.michallysak.weider6.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import pl.michallysak.weider6.logger
import java.util.*

class Reminder(var context: Context){

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, ReminderReceiver::class.java), 0)
    private val receiver = ComponentName(context, ReminderReceiver::class.java)

    fun start(stringTime: String){

        val calendar = getCalendarFromString(stringTime)

        logger("Start on ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)} ${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }


        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

    }

    fun cancel(){
        alarmManager.cancel(pendingIntent)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getCalendarFromString(stringTime: String): Calendar{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, stringTime.substring(0, 2).toInt())
        calendar.set(Calendar.MINUTE, stringTime.substring(3, 5).toInt())
        calendar.set(Calendar.SECOND, 0)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        return calendar
    }


}