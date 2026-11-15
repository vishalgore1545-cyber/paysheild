package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AccessibleActionTile
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction
import kotlin.random.Random

private data class TravelRouteOption(
  val mode: String,
  val title: String,
  val subtitle: String,
  val fareInr: Double,
)

private data class BrandVoucherItem(
  val brand: String,
  val discountLabel: String,
  val faceValueInr: Double,
  val payAmountInr: Double,
  val prefix: String,
)

private val travelRoutes = listOf(
  TravelRouteOption("Flights", "Bengaluru (BLR) -> New Delhi (DEL)", "Non-stop • 2h 40m • Zero Convenience Fee", 4890.0),
  TravelRouteOption("Trains", "New Delhi (NDLS) -> Mumbai (BCT)", "Rajdhani Express • 3AC Confirmed", 2140.0),
  TravelRouteOption("Bus", "Bengaluru -> Hyderabad", "AC Sleeper Volvo • Evening 9:30 PM", 950.0),
)

private val vouchers = listOf(
  BrandVoucherItem("Amazon Pay Gift Card", "2% Instant Off", 1000.0, 980.0, "AMZN"),
  BrandVoucherItem("Flipkart E-Gift Voucher", "3% Instant Off", 1000.0, 970.0, "FLPK"),
  BrandVoucherItem("Myntra Fashion Card", "5% Instant Off", 500.0, 475.0, "MYNT"),
  BrandVoucherItem("BookMyShow Movie Pass", "4% Instant Off", 500.0, 480.0, "BMSW"),
)

private data class SwitchMiniApp(
  val name: String,
  val subtitle: String,
  val defaultOrderAmount: Double,
  val icon: ImageVector,
)

private val switchApps = listOf(
  SwitchMiniApp("Swiggy", "Food Express", 320.0, Icons.Filled.Restaurant),
  SwitchMiniApp("BigBasket", "10-Min Groceries", 460.0, Icons.Filled.LocalGroceryStore),
  SwitchMiniApp("Apollo 24/7", "Medicines", 280.0, Icons.Filled.MedicalServices),
  SwitchMiniApp("Metro QR", "City Transit", 60.0, Icons.Filled.Subway),
)

@Composable
fun LifestyleScreen(
  uiState: PayShieldUiState,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedMode by remember { mutableStateOf("Flights") }
  val primaryAccId = uiState.accounts.firstOrNull { it.isPrimary }?.id ?: "acc_hdfc"

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SecurityStatusChip(label = "OAuth2 Sandboxed Switch Partner Mesh")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Travel, Switch & Vouchers", style = MaterialTheme.typography.headlineMedium)
        Text(
          text = "Book flights, trains & buses or buy instant brand gift cards inside PayShield.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // 1. Travel Bookings
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Travel Bookings", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(
            "Flights" to Icons.Filled.FlightTakeoff,
            "Trains" to Icons.Filled.Train,
            "Bus" to Icons.Filled.DirectionsBus,
          ).forEach { (mode, icon) ->
            FilterChip(
              selected = selectedMode == mode,
              onClick = { selectedMode = mode },
              leadingIcon = { Icon(icon, contentDescription = mode) },
              label = { Text(mode) },
              modifier = Modifier.testTag("travel_filter_$mode"),
            )
          }
        }
        travelRoutes.filter { it.mode == selectedMode }.forEach { route ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(route.title, style = MaterialTheme.typography.titleMedium)
                Text(route.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = formatInr(route.fareInr),
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = JetBrainsMonoFamily,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              Button(
                onClick = {
                  onRequestPayment(
                    PendingSecureAction.Payment(
                      title = "${route.mode}: ${route.title}",
                      counterparty = "travel.switch@payshield",
                      category = "TRAVEL",
                      amountInr = route.fareInr,
                      fromAccountId = primaryAccId,
                      paymentRail = "UPI",
                      note = "PNR Instant Ticket Confirmation",
                    )
                  )
                },
                modifier = Modifier.testTag("book_travel_${route.mode}"),
              ) {
                Text("Book")
              }
            }
          }
        }
      }
    }

    // 2. PayShield Switch Mini-Apps
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("PayShield Switch Mini-Apps", style = MaterialTheme.typography.titleLarge)
        switchApps.chunked(2).forEach { row ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { app ->
              AccessibleActionTile(
                label = app.name,
                subtitle = "${app.subtitle} • ${formatInr(app.defaultOrderAmount)}",
                icon = app.icon,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                testTag = "switch_app_${app.name.lowercase().replace(" ", "_")}",
                onClick = {
                  onRequestPayment(
                    PendingSecureAction.Payment(
                      title = "Switch: ${app.name}",
                      counterparty = "${app.name.lowercase().replace(" ", "")}@switch",
                      category = "QR_PAY",
                      amountInr = app.defaultOrderAmount,
                      fromAccountId = primaryAccId,
                      paymentRail = "UPI",
                      note = "PayShield Switch Express Order",
                    )
                  )
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }

    // 3. Brand Vouchers & Gift Cards
    item {
      Text("Brand Vouchers & Gift Cards", style = MaterialTheme.typography.titleLarge)
    }

    items(vouchers, key = { it.prefix }) { voucher ->
      Card(
        modifier = Modifier.fillMaxWidth().testTag("voucher_card_${voucher.prefix}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = if (voucher.prefix == "BMSW") Icons.Filled.ConfirmationNumber else Icons.Filled.CardGiftcard,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(voucher.brand, style = MaterialTheme.typography.titleMedium)
            Text(
              text = "Worth ${formatInr(voucher.faceValueInr)} • ${voucher.discountLabel}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
          OutlinedButton(
            onClick = {
              val generatedCode = "${voucher.prefix}-${Random.nextInt(1000, 9999)}-${Random.nextInt(1000, 9999)}"
              onRequestPayment(
                PendingSecureAction.Payment(
                  title = voucher.brand,
                  counterparty = "vouchers@payshield",
                  category = "VOUCHER",
                  amountInr = voucher.payAmountInr,
                  fromAccountId = primaryAccId,
                  paymentRail = "UPI",
                  note = "Instant Gift Card (${formatInr(voucher.faceValueInr)} Value)",
                  voucherCode = generatedCode,
                )
              )
            },
            modifier = Modifier.testTag("buy_voucher_${voucher.prefix}"),
          ) {
            Text("Buy ${formatInr(voucher.payAmountInr)}")
          }
        }
      }
    }
  }
}
