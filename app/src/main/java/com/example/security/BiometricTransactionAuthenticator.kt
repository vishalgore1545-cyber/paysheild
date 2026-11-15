package com.example.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val TAG = "BiometricTxAuth"
private const val TX_KEY_ALIAS = "payshield_tx_signing_key"

sealed interface BiometricAuthResult {
  data class Success(val usedCryptoObject: Boolean) : BiometricAuthResult
  data class CancelledOrFallback(val reason: String) : BiometricAuthResult
  data class Error(val errorCode: Int, val message: String) : BiometricAuthResult
}

/**
 * Reusable Kotlin utility class encapsulating Android's BiometricPrompt API
 * with AndroidKeyStore Cipher binding (on API 30+ strong biometrics) and
 * graceful fallback handling for financial transaction authorization.
 */
class BiometricTransactionAuthenticator(
  private val activity: FragmentActivity,
) {

  private val supportedAuthenticators: Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
      BIOMETRIC_STRONG or BIOMETRIC_WEAK
    }

  fun checkAvailability(): BiometricAvailability = checkAvailability(activity)

  fun authenticateForTransaction(
    title: String,
    subtitle: String,
    description: String? = null,
    onResult: (BiometricAuthResult) -> Unit,
  ) {
    val availability = checkAvailability()
    if (availability != BiometricAvailability.AVAILABLE) {
      val fallbackMsg = when (availability) {
        BiometricAvailability.NONE_ENROLLED ->
          "No fingerprint or face credential enrolled on this device. Please use your 4-digit UPI PIN."
        BiometricAvailability.UNAVAILABLE ->
          "Biometric hardware unavailable. Please authorize with your 4-digit UPI PIN."
        BiometricAvailability.AVAILABLE -> ""
      }
      onResult(BiometricAuthResult.CancelledOrFallback(fallbackMsg))
      return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onResult(BiometricAuthResult.Success(usedCryptoObject = result.cryptoObject != null))
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        val isCancelOrFallback = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
          errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
          errorCode == BiometricPrompt.ERROR_CANCELED
        if (isCancelOrFallback) {
          onResult(BiometricAuthResult.CancelledOrFallback(errString.toString()))
        } else {
          onResult(BiometricAuthResult.Error(errorCode, errString.toString()))
        }
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Log.w(TAG, "Biometric did not match; system prompt remains open for retry.")
      }
    }

    val prompt = BiometricPrompt(activity, executor, callback)
    val promptBuilder = BiometricPrompt.PromptInfo.Builder()
      .setTitle(title)
      .setSubtitle(subtitle)
      .setConfirmationRequired(true)

    if (!description.isNullOrBlank()) {
      promptBuilder.setDescription(description)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      promptBuilder.setAllowedAuthenticators(supportedAuthenticators)
    } else {
      promptBuilder
        .setAllowedAuthenticators(supportedAuthenticators)
        .setNegativeButtonText("Use UPI PIN")
    }

    val promptInfo = promptBuilder.build()
    val cryptoObject = buildCryptoObjectIfStrongAvailable()

    if (cryptoObject != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
      BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    ) {
      try {
        prompt.authenticate(promptInfo, cryptoObject)
        return
      } catch (e: Exception) {
        Log.w(TAG, "CryptoObject auth unavailable with current authenticator set; falling back: ${e.message}")
      }
    }

    prompt.authenticate(promptInfo)
  }

  private fun buildCryptoObjectIfStrongAvailable(): BiometricPrompt.CryptoObject? {
    return try {
      val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
      if (!keyStore.containsAlias(TX_KEY_ALIAS)) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val specBuilder = KeyGenParameterSpec.Builder(
          TX_KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
          .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
          .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          specBuilder.setUserAuthenticationParameters(
            0, // Auth-per-use for high-assurance financial transactions
            KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
          )
        }
        keyGenerator.init(specBuilder.build())
        keyGenerator.generateKey()
      }

      val secretKey = keyStore.getKey(TX_KEY_ALIAS, null) as? SecretKey ?: return null
      val cipher = Cipher.getInstance(
        "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
      )
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      BiometricPrompt.CryptoObject(cipher)
    } catch (e: Exception) {
      Log.d(TAG, "Skipping hardware CryptoObject binding on this device: ${e.message}")
      null
    }
  }

  companion object {
    fun checkAvailability(context: Context): BiometricAvailability {
      val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
      } else {
        BIOMETRIC_STRONG or BIOMETRIC_WEAK
      }
      return when (BiometricManager.from(context).canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
        else -> BiometricAvailability.UNAVAILABLE
      }
    }
  }
}
