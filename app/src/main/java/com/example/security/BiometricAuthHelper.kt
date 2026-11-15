package com.example.security

import android.content.Context
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability {
  AVAILABLE,
  NONE_ENROLLED,
  UNAVAILABLE,
}

object BiometricAuthHelper {

  fun checkAvailability(context: Context): BiometricAvailability =
    BiometricTransactionAuthenticator.checkAvailability(context)

  fun authenticate(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFallbackOrCancel: (String) -> Unit,
  ) {
    BiometricTransactionAuthenticator(activity).authenticateForTransaction(
      title = title,
      subtitle = subtitle,
      description = "Confirm this financial action with your device biometrics",
    ) { result ->
      when (result) {
        is BiometricAuthResult.Success -> onSuccess()
        is BiometricAuthResult.CancelledOrFallback -> onFallbackOrCancel(result.reason)
        is BiometricAuthResult.Error -> onFallbackOrCancel(result.message)
      }
    }
  }
}
