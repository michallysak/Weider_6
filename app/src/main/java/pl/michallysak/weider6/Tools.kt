package pl.michallysak.weider6

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

fun logger(text: String) {
    Log.d("Weider6 logger", text)
}

fun setTheme(context: Context) {
    context.setTheme(R.style.AppTheme)
    when (PreferenceManager.getDefaultSharedPreferences(context).getString("theme", "default") as String) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        "default" -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || doesManufacturerSupportedSystemDarkTheme())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }
}


fun isDarkTheme(context: Context): Boolean {
    return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@SuppressLint("DefaultLocale")
fun doesManufacturerSupportedSystemDarkTheme(): Boolean{
    return when(Build.MANUFACTURER.toUpperCase()){
        "XIAOMI" -> true
        //OTHER MANUFACTURER
        else -> {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.P
        }
    }
}

@SuppressLint("DefaultLocale")
fun getManufacturerOwnServiceRestrictionMessage(context: Context): String{
    return when(Build.MANUFACTURER.toUpperCase()){
        "XIAOMI" -> context.getString(R.string.service_restriction_xiaomi)
        //OTHER MANUFACTURER
        else -> ""
    }

}