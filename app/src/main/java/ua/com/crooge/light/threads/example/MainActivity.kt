package ua.com.crooge.light.threads.example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ua.com.crooge.light.threads.runInForeground

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runInForeground(2000) {
            Toast.makeText(this@MainActivity, "Enjoy using LightThreads!", Toast.LENGTH_SHORT).show()
        }
    }
}
