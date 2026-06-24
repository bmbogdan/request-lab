package eu.mihaibadea.requestlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import eu.mihaibadea.requestlab.core.designsystem.theme.AppTheme
import eu.mihaibadea.requestlab.core.navigation.RequestLabNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                RequestLabNavHost()
            }
        }
    }
}
