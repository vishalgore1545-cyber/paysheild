package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.configureAppCheck
import com.example.ui.navigation.PayShieldAppScaffold
import com.example.ui.theme.PayShieldTheme
import com.example.ui.viewmodel.PayShieldViewModel

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check before rendering Compose UI or creating ViewModels
    configureAppCheck(this, intent)

    enableEdgeToEdge()
    setContent {
      val viewModel: PayShieldViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      PayShieldTheme(easyReadMode = uiState.preferences.easyReadMode) {
        PayShieldAppScaffold(
          uiState = uiState,
          viewModel = viewModel,
        )
      }
    }
  }
}
