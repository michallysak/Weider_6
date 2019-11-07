package pl.michallysak.weider6.watch

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        b.setOnClickListener {
            Toast.makeText(this, "ic_pause", Toast.LENGTH_LONG).show()
        }
        // Enables Always-on
        setAmbientEnabled()
    }
}
