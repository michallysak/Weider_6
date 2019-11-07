package pl.michallysak.weider6.ui.main

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.*
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.michallysak.weider6.*
import pl.michallysak.weider6.R
import pl.michallysak.weider6.database.TrainingDayDatabase
import pl.michallysak.weider6.reminders.Reminder


class SettingsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        return view
    }


    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var sharedPreferences: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            setupTheme()
            setupScreenOrientation()
            setupKeepScreenOn()

            setupBreakBetweenExercises()
            setupBreakBetweenSeries()

            setupReminders()

            setupDeleteProgress()
        }


        private fun setupTheme() {

            var theme = sharedPreferences.getString("theme", "default") as String

            val preference = findPreference<Preference>("theme") as ListPreference

            val entries = mutableListOf(context?.getString(R.string.light), context?.getString(R.string.dark))
            val values = mutableListOf("light", "dark", "default")

            if (doesManufacturerSupportedSystemDarkTheme())
                entries.add(context?.getString(R.string.system_default))
            else
                entries.add(context?.getString(R.string.battery_saver))

            var index = values.indexOf(theme)

            if (index !in 0 .. 2)
                index = 2

            preference.setValueIndex(index)
            preference.summary = entries[index]
            preference.entries = entries.toTypedArray()
            preference.entryValues = values.toTypedArray()

            preference.setOnPreferenceChangeListener { _, newValue ->
                sharedPreferences.edit().putString("theme", newValue as String).apply()
                setTheme(context!!)
                preference.summary = entries[values.indexOf(newValue)]
                true
            }

        }


        private fun setupScreenOrientation() {

            val screenOrientation =
                sharedPreferences.getString("screen_orientation", "portrait") as String

            val preference =
                findPreference<Preference>("screen_orientation") as ListPreference

            val entries = listOf(
                context?.getString(R.string.portrait),
                context?.getString(R.string.landscape),
                context?.getString(
                    R.string.auto
                )
            )
            val values = listOf("portrait", "landscape", "auto")

            val index = values.indexOf(screenOrientation)
            preference.setValueIndex(index)
            preference.summary = entries[index]
            preference.entries = entries.toTypedArray()
            preference.entryValues = values.toTypedArray()

            preference.setOnPreferenceChangeListener { _, newValue ->
                preference.summary = entries[values.indexOf(newValue)]
                true
            }

        }

        private fun setupKeepScreenOn() {

            val preference = findPreference<Preference>("keep_screen_on") as SwitchPreferenceCompat

            preference.isChecked = sharedPreferences.getBoolean("keep_screen_on", true)

            preference.setOnPreferenceChangeListener { _, newValue ->
                sharedPreferences.edit().putBoolean("keep_screen_on", newValue as Boolean).apply()
                true
            }


        }

        private fun setupBreakBetweenExercises() {
            val breakBetweenExercises = sharedPreferences.getString("break_between_exercises", "5")

            val preference =
                findPreference<Preference>("break_between_exercises") as ListPreference

            val sec = context?.getString(R.string.sec)

            val entries = listOf("5 $sec", "10 $sec", "15 $sec", "20 $sec").toTypedArray()
            val values = listOf("5", "10", "15", "20").toTypedArray()

            val index = values.indexOf(breakBetweenExercises)
            preference.setValueIndex(index)
            preference.summary = entries[index]
            preference.entries = entries
            preference.entryValues = values

            preference.setOnPreferenceChangeListener { _, newValue ->
                preference.summary = entries[values.indexOf(newValue)]
                true
            }

        }

        private fun setupBreakBetweenSeries() {
            val breakBetweenSeries = sharedPreferences.getString("break_between_series", "10")

            val preference = findPreference<Preference>("break_between_series") as ListPreference

            val sec = context?.getString(R.string.sec)

            val entries = listOf("5 $sec", "10 $sec", "20 $sec", "30 $sec").toTypedArray()
            val values = listOf("5", "10", "20", "30").toTypedArray()

            val index = values.indexOf(breakBetweenSeries)
            preference.setValueIndex(index)
            preference.summary = entries[index]
            preference.entries = entries
            preference.entryValues = values

            preference.setOnPreferenceChangeListener { _, newValue ->
                preference.summary = entries[values.indexOf(newValue)]
                true
            }

        }


        private fun setupReminders() {

            val reminders = sharedPreferences.getBoolean("reminders", false)
            val remindersTime = sharedPreferences.getString("reminders_time", "18:00") as String

            val switchPreference = findPreference<Preference>("reminders") as SwitchPreferenceCompat
            val timePreference = findPreference<Preference>("reminders_time")

            if (reminders)
                timePreference?.summary = remindersTime

            val reminder = Reminder(context as Context)

            switchPreference.setOnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    timePreference?.summary = remindersTime
                    reminder.start(remindersTime)
                    val message = getManufacturerOwnServiceRestrictionMessage(context!!)
                    if(message != "" && sharedPreferences.getBoolean("service_restriction_dialog", true)){
                        AlertDialog.Builder(context!!)
                            .setTitle(R.string.attention)
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                val uri = Uri.Builder().scheme("package").opaquePart(context!!.packageName).build()
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))}
                            .setNegativeButton(R.string.dont_show_again) { _, _ -> sharedPreferences.edit().putBoolean("service_restriction_dialog", false).apply()}
                            .create().show()
                    }

                } else {
                    timePreference?.summary = context?.getString(R.string.reminders_disabled)
                    reminder.cancel()
                }
                true
            }

            timePreference?.setOnPreferenceClickListener {

                var time = sharedPreferences.getString("reminders_time", "18:00") as String

                val hour = time.substringBefore(":").toInt()
                val min = time.substringAfter(":").toInt()


                TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                        val sb = StringBuilder()

                        if (hourOfDay < 10)
                            sb.append("0")

                        sb.append(hourOfDay)

                        sb.append(":")

                        if (minute < 10)
                            sb.append("0")

                        sb.append(minute)

                        time = sb.toString()

                        logger("$hourOfDay $minute")

                        reminder.cancel()
                        reminder.start(time)

                        timePreference.summary = time
                        sharedPreferences.edit().putString("reminders_time", time).apply()
                    }, hour, min, true
                ).show()


                true
            }

        }


        private fun setupDeleteProgress() {

            val preference = findPreference<Preference>("delete_progress")


//        delete_progress
            preference?.setOnPreferenceClickListener {
                val dialog = AlertDialog.Builder(context as Context)
                    .setTitle(R.string.delete_progress_title)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = Room.databaseBuilder(context as Context, TrainingDayDatabase::class.java, "trainingsDatabase").build()
                            database.trainingDao().deleteAll(database.trainingDao().all)
                            sharedPreferences.edit().putBoolean("training_active", false).apply()
                            sharedPreferences.edit().putBoolean("training_completed_dialog", false).apply()
                        }

                        Toast.makeText(context, R.string.after_delete_progress_info, Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton(R.string.no) { _, _ -> }.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)

                true
            }

        }
    }
}