package defdev.labs.droid.securecommunication

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labs.droid.securecommunication.theme.SecureCommunicationTheme
import defdev.labs.droid.securecommunication.ui.ExchangeScreen

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            SecureCommunicationTheme {
                val viewModel: SecureCommunicationViewModel =
                    viewModel(factory = container.viewModelFactory)
                ExchangeScreen(viewModel = viewModel)
            }
        }
    }
}
