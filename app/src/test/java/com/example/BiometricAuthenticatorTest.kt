package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.BiometricAvailability
import com.example.security.BiometricTransactionAuthenticator
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BiometricAuthenticatorTest {

  @Test
  fun `checkAvailability returns valid BiometricAvailability state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val availability = BiometricTransactionAuthenticator.checkAvailability(context)
    assertTrue(
      availability in listOf(
        BiometricAvailability.AVAILABLE,
        BiometricAvailability.NONE_ENROLLED,
        BiometricAvailability.UNAVAILABLE,
      )
    )
  }
}
