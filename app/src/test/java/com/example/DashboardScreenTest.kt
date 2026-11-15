package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.local.BankAccountEntity
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.PayShieldTheme
import com.example.ui.viewmodel.PayShieldUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardScreenTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `dashboard renders Scan and Pay, Send Money, and Recharge grid tiles with semantic labels`() {
    val sampleState = PayShieldUiState(
      accounts = listOf(
        BankAccountEntity(
          id = "acc_hdfc",
          bankName = "HDFC Bank",
          maskedAccount = "•••• 4821",
          ifscCode = "HDFC0000240",
          upiId = "aarav.sharma@payshield",
          balanceInr = 84650.75,
          isPrimary = true,
          accountType = "SAVINGS",
        )
      ),
    )

    composeRule.setContent {
      PayShieldTheme {
        DashboardScreen(
          uiState = sampleState,
          onToggleHideBalances = {},
          onRequestPayment = {},
          onRequestBalanceInquiry = {},
          onSetUpiInternational = { _, _ -> },
          onTransactionClick = {},
          onNavigateToBills = {},
          onNavigateToAssistant = {},
        )
      }
    }

    composeRule.onNodeWithTag("dashboard_grid").assertIsDisplayed()
    composeRule.onNodeWithTag("tile_scan_pay").assertIsDisplayed()
    composeRule.onNodeWithTag("tile_send_money").assertIsDisplayed()
    composeRule.onNodeWithTag("tile_recharge").assertIsDisplayed()

    composeRule.onNodeWithContentDescription("Scan and Pay using any UPI QR code").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Send Money to a mobile number or contact").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Recharge prepaid mobile, DTH, or FASTag").assertIsDisplayed()
    composeRule.onNodeWithText("Quick Payments & Services").assertIsDisplayed()
  }
}
