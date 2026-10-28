package dev.def.lab.droid.securecommunication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.def.lab.droid.securecommunication.theme.SecureCommunicationTheme
import dev.def.lab.droid.securecommunication.ui.ExchangeScreen

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureCommunicationTheme {
                val viewModel: SecureCommunicationViewModel =
                    viewModel(factory = container.viewModelFactory)
                ExchangeScreen(viewModel = viewModel)
            }
        }
    }
}
