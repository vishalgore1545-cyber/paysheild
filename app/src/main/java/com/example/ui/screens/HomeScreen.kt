package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BankAccountEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.AccessibleActionTile
import com.example.ui.components.MyQrReceiveDialog
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

enum class HomeModalType {
  QR_SCAN,
  TO_MOBILE,
  TO_BANK_UPI,
  TO_SELF,
  CHECK_BALANCE,
  UPI_LITE_INTL,
}

@Composable
fun HomeScreen(
  uiState: PayShieldUiState,
  onToggleHideBalances: () -> Unit,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  onRequestBalanceInquiry: (BankAccountEntity) -> Unit,
  onSetUpiInternational: (Boolean, String) -> Unit,
  onTransactionClick: (TransactionEntity) -> Unit,
  onNavigateToAssistant: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var activeModal by remember { mutableStateOf<HomeModalType?>(null) }
  var showMyQrDialog by remember { mutableStateOf(false) }

  val primaryAccount = uiState.accounts.firstOrNull { it.isPrimary } ?: uiState.accounts.firstOrNull()
  val liteAccount = uiState.accounts.firstOrNull { it.accountType == "UPI_LITE" }
  val hideBalances = uiState.preferences.hideBalances

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Hero Balance & Quick Action Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_balance_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_shield_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.42f,
            modifier = Modifier.matchParentSize(),
          )
          Box(
            modifier = Modifier
              .matchParentSize()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                  )
                )
              )
          )
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = primaryAccount?.let { "${it.bankName} ${it.maskedAccount}" } ?: "Primary Bank",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White.copy(alpha = 0.9f),
                )
                Text(
                  text = primaryAccount?.upiId ?: "aarav.sharma@payshield",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.78f),
                )
              }
              IconButton(
                onClick = onToggleHideBalances,
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("toggle_balance_visibility"),
              ) {
                Icon(
                  imageVector = if (hideBalances) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = if (hideBalances) "Show balance" else "Hide balance",
                  tint = Color.White,
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (hideBalances) "₹ ••,•••.••" else formatInr(primaryAccount?.balanceInr ?: 0.0),
              style = MaterialTheme.typography.displaySmall,
              fontFamily = JetBrainsMonoFamily,
              color = Color.White,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(MaterialTheme.shapes.small)
                  .clickable { activeModal = HomeModalType.UPI_LITE_INTL }
                  .testTag("hero_upi_lite_pill"),
                color = Color.White.copy(alpha = 0.16f),
                shape = MaterialTheme.shapes.small,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "UPI Lite",
                    tint = Color(0xFF7BFFCA),
                    modifier = Modifier.size(18.dp),
                  )
                  Column {
                    Text(
                      text = "UPI Lite",
                      style = MaterialTheme.typography.labelMedium,
                      color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                      text = if (hideBalances) "₹ ••••" else formatInr(liteAccount?.balanceInr ?: 0.0),
                      style = MaterialTheme.typography.titleSmall,
                      fontFamily = JetBrainsMonoFamily,
                      color = Color.White,
                    )
                  }
                }
              }

              Button(
                onClick = { activeModal = HomeModalType.QR_SCAN },
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF10B981),
                  contentColor = Color(0xFF03281B),
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                  .height(48.dp)
                  .testTag("hero_scan_pay_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.QrCodeScanner,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Scan & Pay",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }

    // 2. Conversational AI Voice/Chat Quick Trigger (Consumer-friendly)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(MaterialTheme.shapes.medium)
          .clickable(onClick = onNavigateToAssistant)
          .testTag("home_ai_assistant_banner"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "PayShield AI Assistant",
                tint = Color.White,
              )
            }
            Column {
              Text(
                text = "Ask PayShield AI (EN / हिंदी / मराठी)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              Text(
                text = "Say \"Send ₹500 to Dad\", check spend insights or resolve issues",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
              )
            }
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open AI Assistant",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
      }
    }

    // 3. Core Money Transfers (2x3 Accessible Grid)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Money Transfers",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          if (uiState.preferences.upiInternationalEnabled) {
            SecurityStatusChip(label = "Intl Active: ${uiState.preferences.internationalCountry.substringBefore(" ")}")
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          AccessibleActionTile(
            label = "Scan & Pay",
            subtitle = "Any QR",
            icon = Icons.Filled.QrCodeScanner,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.primary,
            testTag = "tile_scan_pay",
            onClick = { activeModal = HomeModalType.QR_SCAN },
            modifier = Modifier.weight(1f),
          )
          AccessibleActionTile(
            label = "To Mobile",
            subtitle = "Contacts",
            icon = Icons.Filled.PhoneAndroid,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.secondary,
            testTag = "tile_to_mobile",
            onClick = { activeModal = HomeModalType.TO_MOBILE },
            modifier = Modifier.weight(1f),
          )
          AccessibleActionTile(
            label = "Bank / UPI",
            subtitle = "IFSC / VPA",
            icon = Icons.Filled.AccountBalance,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.tertiary,
            testTag = "tile_to_bank",
            onClick = { activeModal = HomeModalType.TO_BANK_UPI },
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          AccessibleActionTile(
            label = "To Self",
            subtitle = "Own Accounts",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.primary,
            testTag = "tile_to_self",
            onClick = { activeModal = HomeModalType.TO_SELF },
            modifier = Modifier.weight(1f),
          )
          AccessibleActionTile(
            label = "Check Balance",
            subtitle = "Instant",
            icon = Icons.Filled.AccountBalanceWallet,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.secondary,
            testTag = "tile_check_balance",
            onClick = { activeModal = HomeModalType.CHECK_BALANCE },
            modifier = Modifier.weight(1f),
          )
          AccessibleActionTile(
            label = "My QR",
            subtitle = "Receive Money",
            icon = Icons.Filled.QrCode2,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.tertiary,
            testTag = "tile_my_qr",
            onClick = { showMyQrDialog = true },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // 4. Recent Activity
    item {
      Text(
        text = "Recent Payments",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    if (uiState.transactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Filled.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("No transactions yet. Tap Scan & Pay to start.", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    } else {
      items(uiState.transactions.take(8), key = { it.txId }) { tx ->
        TransactionRowCard(tx = tx, onClick = { onTransactionClick(tx) })
      }
    }
  }

  activeModal?.let { modal ->
    HomeTransferModals(
      modalType = modal,
      uiState = uiState,
      onDismiss = { activeModal = null },
      onRequestPayment = { payment ->
        activeModal = null
        onRequestPayment(payment)
      },
      onRequestBalanceInquiry = { account ->
        activeModal = null
        onRequestBalanceInquiry(account)
      },
      onSetUpiInternational = onSetUpiInternational,
    )
  }

  if (showMyQrDialog) {
    MyQrReceiveDialog(
      upiId = primaryAccount?.upiId ?: "aarav.sharma@payshield",
      accountName = "Aarav Sharma (${primaryAccount?.bankName ?: "HDFC Bank"})",
      onDismiss = { showMyQrDialog = false },
    )
  }
}

@Composable
fun TransactionRowCard(
  tx: TransactionEntity,
  onClick: () -> Unit,
) {
  val isBlocked = tx.status == "BLOCKED"
  val isPending = tx.status == "PENDING"
  val amountPrefix = when {
    isBlocked -> ""
    tx.isCredit -> "+"
    else -> "-"
  }
  val amountColor = when {
    isBlocked -> MaterialTheme.colorScheme.error
    isPending -> MaterialTheme.colorScheme.tertiary
    tx.isCredit -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurface
  }
  val badgeIcon = when (tx.category) {
    "QR_PAY" -> Icons.Filled.QrCodeScanner
    "TRAVEL" -> Icons.Filled.FlightTakeoff
    "BILL_PAY", "RECHARGE" -> Icons.Filled.ReceiptLong
    else -> Icons.Filled.AccountBalance
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = onClick)
      .testTag("tx_row_${tx.txId}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isBlocked) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = badgeIcon,
          contentDescription = tx.category,
          tint = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = tx.title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "${tx.paymentRail} • ${formatTimestamp(tx.timestampMillis)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "$amountPrefix${formatInr(tx.amountInr)}",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = JetBrainsMonoFamily,
          color = amountColor,
        )
        Text(
          text = tx.status,
          style = MaterialTheme.typography.labelSmall,
          color = when {
            isBlocked -> MaterialTheme.colorScheme.error
            isPending -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.secondary
          },
        )
      }
    }
  }
}
