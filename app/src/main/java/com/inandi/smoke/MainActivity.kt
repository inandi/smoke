package com.inandi.smoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.inandi.smoke.ui.theme.SmokeTheme
import android.widget.TextView
import android.widget.Button
import android.view.View
//import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this matches your layout file's name.
        val topLabel = findViewById<TextView>(R.id.topLabel)
        topLabel.text = "Everyday you are wining!!!"



        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }

        // You can also handle clicks programmatically
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }

        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }


//        setContent {
//            SmokeTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }
    }
}

// Handle button clicks defined in XML
fun onHomeButtonClick(view: View) {
    // Handle Home button click
}

fun onAboutButtonClick(view: View) {
    // Handle About button click
}

fun onBadgeButtonClick(view: View) {
    // Handle Badge button click
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmokeTheme {
        Greeting("Android")
    }
}