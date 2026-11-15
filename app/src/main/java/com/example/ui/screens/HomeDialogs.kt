package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.BankAccountEntity
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTransferModals(
  modalType: HomeModalType,
  uiState: PayShieldUiState,
  onDismiss: () -> Unit,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  onRequestBalanceInquiry: (BankAccountEntity) -> Unit,
  onSetUpiInternational: (Boolean, String) -> Unit,
) {
  BackHandler { onDismiss() }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val primaryAcc = uiState.accounts.firstOrNull { it.isPrimary } ?: uiState.accounts.firstOrNull()
  val liteAcc = uiState.accounts.firstOrNull { it.accountType == "UPI_LITE" }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      when (modalType) {
        HomeModalType.QR_SCAN -> {
          QrPaySheetContent(
            primaryAccId = primaryAcc?.id ?: "acc_hdfc",
            liteAccId = liteAcc?.id ?: "acc_lite",
            onPay = onRequestPayment,
          )
        }
        HomeModalType.TO_MOBILE -> {
          ToMobileSheetContent(
            primaryAccId = primaryAcc?.id ?: "acc_hdfc",
            liteAccId = liteAcc?.id ?: "acc_lite",
            onPay = onRequestPayment,
          )
        }
        HomeModalType.TO_BANK_UPI -> {
          ToBankUpiSheetContent(
            primaryAccId = primaryAcc?.id ?: "acc_hdfc",
            onPay = onRequestPayment,
          )
        }
        HomeModalType.TO_SELF -> {
          ToSelfSheetContent(
            accounts = uiState.accounts,
            onPay = onRequestPayment,
          )
        }
        HomeModalType.CHECK_BALANCE -> {
          Text("Select Account to Check Balance", style = MaterialTheme.typography.headlineSmall)
          uiState.accounts.forEach { acc ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onRequestBalanceInquiry(acc) }
                .testTag("balance_account_${acc.id}"),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                  Text(acc.bankName, style = MaterialTheme.typography.titleMedium)
                  Text("${acc.maskedAccount} • ${acc.upiId}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Verify PIN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
        HomeModalType.UPI_LITE_INTL -> {
          UpiLiteAndIntlSheetContent(
            uiState = uiState,
            primaryAccId = primaryAcc?.id ?: "acc_hdfc",
            liteAcc = liteAcc,
            onTopUp = onRequestPayment,
            onSetUpiInternational = onSetUpiInternational,
            onDone = onDismiss,
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun QrPaySheetContent(
  primaryAccId: String,
  liteAccId: String,
  onPay: (PendingSecureAction.Payment) -> Unit,
) {
  var merchantName by remember { mutableStateOf("Metro Coffee Roasters") }
  var merchantVpa by remember { mutableStateOf("metrocoffee@okmerchant") }
  var amountText by remember { mutableStateOf("240") }
  var useUpiLite by remember { mutableStateOf(true) }

  SecurityStatusChip(label = "Signed BharatQR / UPI Intent Verification")
  Text("Scan & Pay (QR)", style = MaterialTheme.typography.headlineSmall)

  // Viewfinder simulation frame
  val frameColor = MaterialTheme.colorScheme.primary
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(116.dp)) {
      val stroke = Stroke(width = 6f, cap = StrokeCap.Round)
      val c = 26f
      drawLine(frameColor, Offset(0f, 0f), Offset(c, 0f), strokeWidth = 6f)
      drawLine(frameColor, Offset(0f, 0f), Offset(0f, c), strokeWidth = 6f)
      drawLine(frameColor, Offset(size.width, 0f), Offset(size.width - c, 0f), strokeWidth = 6f)
      drawLine(frameColor, Offset(size.width, 0f), Offset(size.width, c), strokeWidth = 6f)
      drawLine(frameColor, Offset(0f, size.height), Offset(c, size.height), strokeWidth = 6f)
      drawLine(frameColor, Offset(0f, size.height), Offset(0f, size.height - c), strokeWidth = 6f)
      drawLine(frameColor, Offset(size.width, size.height), Offset(size.width - c, size.height), strokeWidth = 6f)
      drawLine(frameColor, Offset(size.width, size.height), Offset(size.width, size.height - c), strokeWidth = 6f)
      drawRect(frameColor.copy(alpha = 0.08f), topLeft = Offset(12f, 12f), size = Size(size.width - 24f, size.height - 24f))
    }
    Icon(
      imageVector = Icons.Filled.QrCode2,
      contentDescription = "QR Scanner Frame",
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(64.dp),
    )
  }

  Text("Tap a verified merchant QR or edit VPA:", style = MaterialTheme.typography.labelLarge)
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
      selected = merchantVpa == "metrocoffee@okmerchant",
      onClick = {
        merchantName = "Metro Coffee Roasters"
        merchantVpa = "metrocoffee@okmerchant"
        amountText = "240"
      },
      label = { Text("Metro Coffee") },
    )
    FilterChip(
      selected = merchantVpa == "citypharma@okmerchant",
      onClick = {
        merchantName = "City Care Pharmacy"
        merchantVpa = "citypharma@okmerchant"
        amountText = "480"
      },
      label = { Text("Pharmacy") },
    )
    FilterChip(
      selected = merchantVpa == "nammametro@upi",
      onClick = {
        merchantName = "Namma Metro Pass"
        merchantVpa = "nammametro@upi"
        amountText = "120"
      },
      label = { Text("Metro") },
    )
  }

  OutlinedTextField(
    value = merchantVpa,
    onValueChange = { merchantVpa = it },
    label = { Text("Merchant UPI ID (or test SQLi payload)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("qr_vpa_input"),
  )
  OutlinedTextField(
    value = amountText,
    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
    label = { Text("Amount (₹)") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("qr_amount_input"),
  )

  val amt = amountText.toDoubleOrNull() ?: 0.0
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text("Use UPI Lite (PIN-less up to ₹500)", style = MaterialTheme.typography.titleSmall)
      Text("Instant on-device wallet clearance", style = MaterialTheme.typography.bodySmall)
    }
    Switch(checked = useUpiLite && amt <= 500.0, onCheckedChange = { useUpiLite = it })
  }

  val rail = if (useUpiLite && amt in 1.0..500.0) "UPI_LITE" else "UPI"
  val fromId = if (rail == "UPI_LITE") liteAccId else primaryAccId

  Button(
    onClick = {
      if (amt > 0 && merchantVpa.isNotBlank()) {
        onPay(
          PendingSecureAction.Payment(
            title = merchantName,
            counterparty = merchantVpa,
            category = "QR_PAY",
            amountInr = amt,
            fromAccountId = fromId,
            paymentRail = rail,
            note = "QR Scan Payment",
          )
        )
      }
    },
    enabled = amt > 0 && merchantVpa.isNotBlank(),
    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("qr_pay_submit"),
  ) {
    Text(if (rail == "UPI_LITE") "Pay Instantly (No PIN) ${formatInr(amt)}" else "Proceed to PIN ${formatInr(amt)}")
  }
}

@Composable
private fun ToMobileSheetContent(
  primaryAccId: String,
  liteAccId: String,
  onPay: (PendingSecureAction.Payment) -> Unit,
) {
  var contactName by remember { mutableStateOf("Priya Nair") }
  var phone by remember { mutableStateOf("9876543210") }
  var amountText by remember { mutableStateOf("500") }
  var note by remember { mutableStateOf("Lunch split") }

  Text("Send to Mobile Number", style = MaterialTheme.typography.headlineSmall)
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    listOf("Priya Nair" to "9876543210", "Rohan Verma" to "9811223344", "Kavita Joshi" to "9900112233").forEach { (name, num) ->
      FilterChip(
        selected = phone == num,
        onClick = {
          contactName = name
          phone = num
        },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
        label = { Text(name.substringBefore(" ")) },
      )
    }
  }

  OutlinedTextField(
    value = phone,
    onValueChange = { phone = it.filter(Char::isDigit).take(10) },
    label = { Text("10-Digit Mobile Number") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("mobile_number_input"),
  )
  OutlinedTextField(
    value = amountText,
    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
    label = { Text("Amount (₹)") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("mobile_amount_input"),
  )
  OutlinedTextField(
    value = note,
    onValueChange = { note = it },
    label = { Text("Note (Sanitized by WAF)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )

  val amt = amountText.toDoubleOrNull() ?: 0.0
  val isValid = phone.length == 10 && amt > 0
  Button(
    onClick = {
      onPay(
        PendingSecureAction.Payment(
          title = contactName,
          counterparty = "$phone@payshield",
          category = "TRANSFER",
          amountInr = amt,
          fromAccountId = if (amt <= 300.0) liteAccId else primaryAccId,
          paymentRail = if (amt <= 300.0) "UPI_LITE" else "UPI",
          note = note,
        )
      )
    },
    enabled = isValid,
    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("mobile_pay_submit"),
  ) {
    Text("Send ${formatInr(amt)}")
  }
}

@Composable
private fun ToBankUpiSheetContent(
  primaryAccId: String,
  onPay: (PendingSecureAction.Payment) -> Unit,
) {
  var recipientName by remember { mutableStateOf("Vikram Mehta") }
  var vpaOrAccount by remember { mutableStateOf("vikram.mehta@okhdfcbank") }
  var ifsc by remember { mutableStateOf("HDFC0000128") }
  var amountText by remember { mutableStateOf("2500") }

  Text("Send to Bank / UPI ID", style = MaterialTheme.typography.headlineSmall)
  OutlinedTextField(
    value = recipientName,
    onValueChange = { recipientName = it },
    label = { Text("Beneficiary Name") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = vpaOrAccount,
    onValueChange = { vpaOrAccount = it },
    label = { Text("UPI ID or Bank Account Number") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("bank_vpa_input"),
  )
  OutlinedTextField(
    value = ifsc,
    onValueChange = { ifsc = it.uppercase().take(11) },
    label = { Text("IFSC Code (Optional for UPI ID)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = amountText,
    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
    label = { Text("Amount (₹)") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("bank_amount_input"),
  )
  val amt = amountText.toDoubleOrNull() ?: 0.0
  Button(
    onClick = {
      onPay(
        PendingSecureAction.Payment(
          title = recipientName.ifBlank { "Bank Transfer" },
          counterparty = vpaOrAccount,
          category = "TRANSFER",
          amountInr = amt,
          fromAccountId = primaryAccId,
          paymentRail = "UPI",
          note = "IFSC $ifsc",
        )
      )
    },
    enabled = amt > 0 && vpaOrAccount.isNotBlank(),
    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("bank_pay_submit"),
  ) {
    Text("Verify & Send ${formatInr(amt)}")
  }
}

@Composable
private fun ToSelfSheetContent(
  accounts: List<BankAccountEntity>,
  onPay: (PendingSecureAction.Payment) -> Unit,
) {
  val hdfc = accounts.firstOrNull { it.id == "acc_hdfc" }
  val sbi = accounts.firstOrNull { it.id == "acc_sbi" }
  var transferAmount by remember { mutableStateOf("5000") }
  var hdfcToSbi by remember { mutableStateOf(true) }

  Text("Transfer Between Self Accounts", style = MaterialTheme.typography.headlineSmall)
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
      selected = hdfcToSbi,
      onClick = { hdfcToSbi = true },
      label = { Text("HDFC -> SBI") },
    )
    FilterChip(
      selected = !hdfcToSbi,
      onClick = { hdfcToSbi = false },
      label = { Text("SBI -> HDFC") },
    )
  }

  val fromAcc = if (hdfcToSbi) hdfc else sbi
  val toAcc = if (hdfcToSbi) sbi else hdfc
  if (fromAcc != null && toAcc != null) {
    Text(
      text = "From: ${fromAcc.bankName} (${formatInr(fromAcc.balanceInr)})\nTo: ${toAcc.bankName} (${formatInr(toAcc.balanceInr)})",
      style = MaterialTheme.typography.bodyMedium,
    )
    OutlinedTextField(
      value = transferAmount,
      onValueChange = { transferAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
      label = { Text("Amount (₹)") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    val amt = transferAmount.toDoubleOrNull() ?: 0.0
    Button(
      onClick = {
        onPay(
          PendingSecureAction.Payment(
            title = "Self Transfer -> ${toAcc.bankName}",
            counterparty = toAcc.upiId,
            category = "TRANSFER",
            amountInr = amt,
            fromAccountId = fromAcc.id,
            toAccountId = toAcc.id,
            paymentRail = "UPI",
            note = "Self account sweep",
          )
        )
      },
      enabled = amt > 0,
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag("self_transfer_submit"),
    ) {
      Text("Transfer ${formatInr(amt)}")
    }
  }
}

@Composable
private fun UpiLiteAndIntlSheetContent(
  uiState: PayShieldUiState,
  primaryAccId: String,
  liteAcc: BankAccountEntity?,
  onTopUp: (PendingSecureAction.Payment) -> Unit,
  onSetUpiInternational: (Boolean, String) -> Unit,
  onDone: () -> Unit,
) {
  var selectedCountry by remember(uiState.preferences.internationalCountry) {
    mutableStateOf(uiState.preferences.internationalCountry)
  }

  Text("UPI Lite & International", style = MaterialTheme.typography.headlineSmall)
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text("UPI Lite Wallet", style = MaterialTheme.typography.titleMedium)
      }
      Text(
        text = "Current Balance: ${formatInr(liteAcc?.balanceInr ?: 0.0)} • Max ₹2,000",
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = JetBrainsMonoFamily,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(500.0, 1000.0).forEach { topUpAmt ->
          OutlinedButton(
            onClick = {
              onTopUp(
                PendingSecureAction.Payment(
                  title = "UPI Lite Top-Up",
                  counterparty = "lite.aarav@payshield",
                  category = "TRANSFER",
                  amountInr = topUpAmt,
                  fromAccountId = primaryAccId,
                  toAccountId = liteAcc?.id,
                  paymentRail = "UPI",
                  note = "Wallet load",
                )
              )
            },
            modifier = Modifier.weight(1f),
          ) {
            Text("+ ${formatInr(topUpAmt)}")
          }
        }
      }
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("UPI International Roaming", style = MaterialTheme.typography.titleMedium)
          Text("Scan merchant QRs abroad with live INR FX lock", style = MaterialTheme.typography.bodySmall)
        }
        Switch(
          checked = uiState.preferences.upiInternationalEnabled,
          onCheckedChange = { enabled -> onSetUpiInternational(enabled, selectedCountry) },
          modifier = Modifier.testTag("upi_intl_switch"),
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Singapore (SGQR)", "UAE (NPCI)", "France (Lyra)").forEach { corridor ->
          FilterChip(
            selected = selectedCountry == corridor,
            onClick = {
              selectedCountry = corridor
              onSetUpiInternational(uiState.preferences.upiInternationalEnabled, corridor)
            },
            label = { Text(corridor.substringBefore(" ")) },
          )
        }
      }
      Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text("Done")
      }
    }
  }
}
