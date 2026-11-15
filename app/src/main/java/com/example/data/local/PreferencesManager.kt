package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "payshield_prefs")

data class UserPreferences(
  val easyReadMode: Boolean = false,
  val hideBalances: Boolean = false,
  val upiInternationalEnabled: Boolean = false,
  val internationalCountry: String = "Singapore (SGQR)",
  val biometricAppLockEnabled: Boolean = false,
)

class PreferencesManager(private val context: Context) {

  private object Keys {
    val EASY_READ_MODE = booleanPreferencesKey("easy_read_mode")
    val HIDE_BALANCES = booleanPreferencesKey("hide_balances")
    val UPI_INTL_ENABLED = booleanPreferencesKey("upi_intl_enabled")
    val INTL_COUNTRY = stringPreferencesKey("intl_country")
    val BIOMETRIC_APP_LOCK = booleanPreferencesKey("biometric_app_lock")
  }

  val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
    UserPreferences(
      easyReadMode = prefs[Keys.EASY_READ_MODE] ?: false,
      hideBalances = prefs[Keys.HIDE_BALANCES] ?: false,
      upiInternationalEnabled = prefs[Keys.UPI_INTL_ENABLED] ?: false,
      internationalCountry = prefs[Keys.INTL_COUNTRY] ?: "Singapore (SGQR)",
      biometricAppLockEnabled = prefs[Keys.BIOMETRIC_APP_LOCK] ?: false,
    )
  }

  suspend fun toggleEasyReadMode() {
    context.dataStore.edit { prefs ->
      prefs[Keys.EASY_READ_MODE] = !(prefs[Keys.EASY_READ_MODE] ?: false)
    }
  }

  suspend fun toggleHideBalances() {
    context.dataStore.edit { prefs ->
      prefs[Keys.HIDE_BALANCES] = !(prefs[Keys.HIDE_BALANCES] ?: false)
    }
  }

  suspend fun setUpiInternational(enabled: Boolean, country: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.UPI_INTL_ENABLED] = enabled
      prefs[Keys.INTL_COUNTRY] = country
    }
  }

  suspend fun setBiometricAppLock(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.BIOMETRIC_APP_LOCK] = enabled
    }
  }
}
