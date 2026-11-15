package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AutoPayMandateEntity
import com.example.ui.components.AccessibleActionTile
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

private data class BillPreset(
  val title: String,
  val category: String,
  val billerName: String,
  val defaultId: String,
  val defaultAmount: Double,
  val icon: ImageVector,
)

private val billPresets = listOf(
  BillPreset("Mobile", "RECHARGE", "Jio / Airtel Prepaid 5G", "9876543210", 299.0, Icons.Filled.PhoneIphone),
  BillPreset("DTH", "RECHARGE", "Tata Play HD Pack", "1048291044", 350.0, Icons.Filled.Tv),
  BillPreset("Electricity", "BILL_PAY", "BESCOM Power Supply", "KA-904821", 1840.0, Icons.Filled.ElectricBolt),
  BillPreset("Piped Gas", "BILL_PAY", "Mahanagar Gas Ltd", "MGL-442190", 785.0, Icons.Filled.LocalFireDepartment),
  BillPreset("Water", "BILL_PAY", "BWSSB Municipal Water", "BW-772104", 420.0, Icons.Filled.WaterDrop),
  BillPreset("Credit Card", "BILL_PAY", "HDFC Millennia Card", "4315••••8812", 12450.0, Icons.Filled.CreditCard),
  BillPreset("Loan EMI", "BILL_PAY", "Home Loan EMI", "HL-8830192", 24500.0, Icons.Filled.RequestQuote),
  BillPreset("Insurance", "BILL_PAY", "Term Shield Life Cover", "POL-992014", 3200.0, Icons.Filled.HealthAndSafety),
  BillPreset("FASTag", "RECHARGE", "NETC ICICI FASTag", "KA01MJ4821", 500.0, Icons.Filled.DirectionsCar),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
  uiState: PayShieldUiState,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  onToggleAutoPay: (AutoPayMandateEntity) -> Unit,
  onCreateAutoPay: (String, String, Double) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedPreset by remember { mutableStateOf<BillPreset?>(null) }
  var showAddMandateDialog by remember { mutableStateOf(false) }
  val primaryAccId = uiState.accounts.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SecurityStatusChip(label = "BBPS Signed Biller Network")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Recharge & Bill Payments", style = MaterialTheme.typography.headlineMedium)
        Text(
          text = "Zero convenience fee on utility bills & instant AutoPay mandates.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // 3x3 Grid of Bill Categories
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        billPresets.chunked(3).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            rowItems.forEach { preset ->
              AccessibleActionTile(
                label = preset.title,
                subtitle = preset.billerName.substringBefore(" "),
                icon = preset.icon,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                testTag = "bill_tile_${preset.title.lowercase().replace(" ", "_")}",
                onClick = { selectedPreset = preset },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }

    // AutoPay Mandates
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text("UPI AutoPay Mandates", style = MaterialTheme.typography.titleLarge)
          Text("Pre-authorized capped recurring payments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(
          onClick = { showAddMandateDialog = true },
          modifier = Modifier.testTag("add_autopay_button"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = null)
          Text("New")
        }
      }
    }

    items(uiState.autoPayMandates, key = { it.id }) { mandate ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("autopay_card_${mandate.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(mandate.merchantName, style = MaterialTheme.typography.titleMedium)
            Text(
              text = "${mandate.category} • Cap ${formatInr(mandate.maxAmountInr)} (${mandate.frequency})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "Next debit: ${mandate.nextDebitDate}",
              style = MaterialTheme.typography.labelSmall,
              color = if (mandate.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = mandate.isActive,
            onCheckedChange = { onToggleAutoPay(mandate) },
            modifier = Modifier.testTag("autopay_switch_${mandate.id}"),
          )
        }
      }
    }
  }

  selectedPreset?.let { preset ->
    BackHandler { selectedPreset = null }
    var consumerId by remember(preset) { mutableStateOf(preset.defaultId) }
    var amountText by remember(preset) { mutableStateOf(preset.defaultAmount.toInt().toString()) }
    var alsoEnableAutoPay by remember { mutableStateOf(false) }

    ModalBottomSheet(
      onDismissRequest = { selectedPreset = null },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("${preset.title} Payment", style = MaterialTheme.typography.headlineSmall)
        Text(preset.billerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
          value = consumerId,
          onValueChange = { consumerId = it },
          label = { Text("Consumer / Policy / Mobile Number") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("bill_consumer_input"),
        )
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Bill Amount (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("bill_amount_input"),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Auto-setup Monthly UPI AutoPay", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = alsoEnableAutoPay, onCheckedChange = { alsoEnableAutoPay = it })
        }
        val amt = amountText.toDoubleOrNull() ?: 0.0
        Button(
          onClick = {
            if (alsoEnableAutoPay && amt > 0) {
              onCreateAutoPay(preset.billerName, preset.title, amt * 1.25)
            }
            selectedPreset = null
            onRequestPayment(
              PendingSecureAction.Payment(
                title = preset.billerName,
                counterparty = "${preset.title.lowercase().replace(" ", "")}@bbps",
                category = preset.category,
                amountInr = amt,
                fromAccountId = primaryAccId,
                paymentRail = if (alsoEnableAutoPay) "AUTOPAY" else "UPI",
                note = "Consumer #$consumerId",
              )
            )
          },
          enabled = amt > 0 && consumerId.isNotBlank(),
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("bill_pay_submit"),
        ) {
          Text("Pay Bill ${formatInr(amt)}", fontFamily = JetBrainsMonoFamily)
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  if (showAddMandateDialog) {
    var merchant by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("2000") }
    AlertDialog(
      onDismissRequest = { showAddMandateDialog = false },
      title = { Text("Create UPI AutoPay Mandate") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = merchant,
            onValueChange = { merchant = it },
            label = { Text("Biller / Merchant Name") },
            singleLine = true,
          )
          OutlinedTextField(
            value = cap,
            onValueChange = { cap = it.filter(Char::isDigit) },
            label = { Text("Max Monthly Cap (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val maxVal = cap.toDoubleOrNull() ?: 1000.0
            if (merchant.isNotBlank()) {
              onCreateAutoPay(merchant, "Custom AutoPay", maxVal)
            }
            showAddMandateDialog = false
          },
        ) { Text("Activate") }
      },
      dismissButton = {
        TextButton(onClick = { showAddMandateDialog = false }) { Text("Cancel") }
      },
    )
  }
}
