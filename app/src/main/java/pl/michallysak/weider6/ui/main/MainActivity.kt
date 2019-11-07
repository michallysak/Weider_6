package pl.michallysak.weider6.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.michallysak.weider6.R
import pl.michallysak.weider6.database.TrainingDay
import pl.michallysak.weider6.database.TrainingDayDatabase
import pl.michallysak.weider6.database.generateTraining
import pl.michallysak.weider6.setTheme


class MainActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var navController: NavController
    lateinit var trainingDays: List<TrainingDay>

    private fun setMenuItemVisibility(menu: Menu?, visible: Boolean){
        menu?.findItem(R.id.destination_settings)?.isVisible = visible
        menu?.findItem(R.id.destination_how_to)?.isVisible = visible
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(this)
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)


        val appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.destination_home))
            .setFallbackOnNavigateUpListener { onNavigateUp() }
            .build()

        setupActionBarWithNavController(this, navController, appBarConfiguration)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            supportActionBar?.title = destination.label

            setMenuItemVisibility(menu, destination.id == R.id.destination_home)
        }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return item?.onNavDestinationSelected(navController) ?: super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }


}
