package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.InvestmentHoldingEntity
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WealthScreen(
  uiState: PayShieldUiState,
  onRequestInvestment: (PendingSecureAction.Investment) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedHolding by remember { mutableStateOf<InvestmentHoldingEntity?>(null) }
  var showCreditReportDetails by remember { mutableStateOf(false) }

  val primaryAccId = uiState.accounts.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"
  val totalInvested = uiState.investments.sumOf { it.investedInr }
  val totalValue = uiState.investments.sumOf { it.currentValueInr }
  val totalGain = totalValue - totalInvested
  val elssInvested = uiState.investments.firstOrNull { it.assetType == "TAX_SAVER" }?.investedInr ?: 0.0

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Portfolio Hero Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("wealth_portfolio_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Image(
            painter = painterResource(id = R.drawable.img_wealth_gold_banner),
            contentDescription = "Wealth and Digital Gold Banner",
            contentScale = ContentScale.Crop,
            alpha = 0.28f,
            modifier = Modifier.matchParentSize(),
          )
          Box(
            modifier = Modifier
              .matchParentSize()
              .background(
                Brush.horizontalGradient(
                  listOf(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                  )
                )
              )
          )
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SecurityStatusChip(label = "SEBI Regulated • Depository Encrypted")
            Text("Total Wealth Portfolio", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = formatInr(totalValue),
              style = MaterialTheme.typography.displaySmall,
              fontFamily = JetBrainsMonoFamily,
              color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Invested: ${formatInr(totalInvested)}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = JetBrainsMonoFamily,
              )
              Text(
                text = "+${formatInr(totalGain)} returns",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = JetBrainsMonoFamily,
              )
            }
          }
        }
      }
    }

    // 2. Credit Score Gauge Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("credit_score_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Credit Score Check", style = MaterialTheme.typography.titleLarge)
              Text(
                text = "792 / 900 • Excellent Standing",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "100% on-time payments • 14% utilization",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = { showCreditReportDetails = !showCreditReportDetails },
                modifier = Modifier.testTag("toggle_credit_details"),
              ) {
                Text(if (showCreditReportDetails) "Hide Factors" else "Inspect Report")
              }
            }
            CreditScoreArcGauge(score = 792)
          }
          if (showCreditReportDetails) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              CreditFactorPill("On-Time", "100%", Modifier.weight(1f))
              CreditFactorPill("Utilization", "14%", Modifier.weight(1f))
              CreditFactorPill("Age", "6.2 yrs", Modifier.weight(1f))
            }
          }
        }
      }
    }

    // 3. Tax Saving 80C Card
    item {
      val limit = 150000.0
      val progress = (elssInvested / limit).toFloat().coerceIn(0f, 1f)
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Tax Saving Section 80C Tracker", style = MaterialTheme.typography.titleMedium)
          }
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            trackColor = MaterialTheme.colorScheme.surface,
          )
          Text(
            text = "${formatInr(elssInvested)} invested of ${formatInr(limit)} limit (Save up to ₹46,800 tax)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }

    // 4. Mutual Funds, Tax Saver & Digital Gold/Silver Holdings
    item {
      Text("Mutual Funds, SIP & Digital Gold/Silver", style = MaterialTheme.typography.titleLarge)
    }

    items(uiState.investments, key = { it.id }) { holding ->
      val gain = holding.currentValueInr - holding.investedInr
      val unitLabel = when (holding.assetType) {
        "DIGITAL_GOLD" -> "${"%.2f".format(holding.unitsOrGrams)}g (24K • ₹7,840/g)"
        "DIGITAL_SILVER" -> "${"%.1f".format(holding.unitsOrGrams)}g (999 • ₹94/g)"
        else -> "${"%.2f".format(holding.unitsOrGrams)} units • ${holding.cagrPercent}% CAGR"
      }
      Card(
        modifier = Modifier.fillMaxWidth().testTag("holding_card_${holding.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(holding.name, style = MaterialTheme.typography.titleMedium)
              Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formatInr(holding.currentValueInr),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = JetBrainsMonoFamily,
              )
              Text(
                text = "+${formatInr(gain)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (holding.monthlySipInr > 0) "Active SIP: ${formatInr(holding.monthlySipInr)}/mo" else "One-time / Vault",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Button(
              onClick = { selectedHolding = holding },
              modifier = Modifier.testTag("invest_btn_${holding.id}"),
            ) {
              Icon(
                imageVector = if (holding.assetType.startsWith("DIGITAL")) Icons.Filled.AutoAwesome else Icons.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (holding.assetType.startsWith("DIGITAL")) "Buy More" else "Invest / SIP")
            }
          }
        }
      }
    }
  }

  selectedHolding?.let { holding ->
    BackHandler { selectedHolding = null }
    var amountText by remember(holding) { mutableStateOf("1000") }
    var startSip by remember(holding) { mutableStateOf(holding.assetType == "MUTUAL_FUND" || holding.assetType == "TAX_SAVER") }

    ModalBottomSheet(
      onDismissRequest = { selectedHolding = null },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(holding.name, style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(500, 1000, 5000).forEach { preset ->
            FilterChip(
              selected = amountText == preset.toString(),
              onClick = { amountText = preset.toString() },
              label = { Text("₹$preset") },
            )
          }
        }
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Investment Amount (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("invest_amount_input"),
        )
        if (!holding.assetType.startsWith("DIGITAL")) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Set as Monthly SIP Mandate", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = startSip, onCheckedChange = { startSip = it })
          }
        }
        val amt = amountText.toDoubleOrNull() ?: 0.0
        Button(
          onClick = {
            selectedHolding = null
            onRequestInvestment(
              PendingSecureAction.Investment(
                holdingId = holding.id,
                holdingName = holding.name,
                amountInr = amt,
                fromAccountId = primaryAccId,
                isSipStart = startSip,
              )
            )
          },
          enabled = amt > 0,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("invest_confirm_button"),
        ) {
          Text("Proceed to Pay ${formatInr(amt)}")
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun CreditScoreArcGauge(score: Int) {
  val primary = MaterialTheme.colorScheme.secondary
  val track = MaterialTheme.colorScheme.outlineVariant
  val sweep = ((score - 300).coerceIn(0, 600) / 600f) * 240f
  Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.size(94.dp)) {
      drawArc(
        color = track,
        startAngle = 150f,
        sweepAngle = 240f,
        useCenter = false,
        style = Stroke(width = 14f, cap = StrokeCap.Round),
      )
      drawArc(
        color = primary,
        startAngle = 150f,
        sweepAngle = sweep,
        useCenter = false,
        style = Stroke(width = 14f, cap = StrokeCap.Round),
      )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = score.toString(),
        style = MaterialTheme.typography.titleLarge,
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
      )
      Text("EXCELLENT", style = MaterialTheme.typography.labelSmall, color = primary)
    }
  }
}

@Composable
private fun CreditFactorPill(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
      Text(value, style = MaterialTheme.typography.titleSmall, fontFamily = JetBrainsMonoFamily)
      Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
