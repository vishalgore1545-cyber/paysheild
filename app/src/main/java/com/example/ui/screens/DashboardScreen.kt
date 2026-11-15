package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BankAccountEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.MyQrReceiveDialog
import com.example.ui.components.SecurityStatusChip
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PendingSecureAction

private data class DashboardServiceItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val semanticAnnouncement: String,
  val icon: ImageVector,
  val testTag: String,
  val useSecondaryAccent: Boolean = false,
  val useTertiaryAccent: Boolean = false,
  val onClick: () -> Unit,
)

@Composable
fun DashboardScreen(
  uiState: PayShieldUiState,
  onToggleHideBalances: () -> Unit,
  onRequestPayment: (PendingSecureAction.Payment) -> Unit,
  onRequestBalanceInquiry: (BankAccountEntity) -> Unit,
  onSetUpiInternational: (Boolean, String) -> Unit,
  onTransactionClick: (TransactionEntity) -> Unit,
  onNavigateToBills: () -> Unit,
  onNavigateToAssistant: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var activeModal by remember { mutableStateOf<HomeModalType?>(null) }
  var showMyQrDialog by remember { mutableStateOf(false) }

  val primaryAccount = uiState.accounts.firstOrNull { it.isPrimary } ?: uiState.accounts.firstOrNull()
  val liteAccount = uiState.accounts.firstOrNull { it.accountType == "UPI_LITE" }
  val hideBalances = uiState.preferences.hideBalances

  val serviceGridItems = remember(
    onNavigateToBills,
    onNavigateToAssistant,
  ) {
    listOf(
      DashboardServiceItem(
        id = "scan_pay",
        title = "Scan & Pay",
        subtitle = "Any QR code",
        semanticAnnouncement = "Scan and Pay using any UPI QR code",
        icon = Icons.Filled.QrCodeScanner,
        testTag = "tile_scan_pay",
        onClick = { activeModal = HomeModalType.QR_SCAN },
      ),
      DashboardServiceItem(
        id = "send_money",
        title = "Send Money",
        subtitle = "To mobile",
        semanticAnnouncement = "Send Money to a mobile number or contact",
        icon = Icons.AutoMirrored.Filled.Send,
        testTag = "tile_send_money",
        useSecondaryAccent = true,
        onClick = { activeModal = HomeModalType.TO_MOBILE },
      ),
      DashboardServiceItem(
        id = "recharge",
        title = "Recharge",
        subtitle = "Mobile & DTH",
        semanticAnnouncement = "Recharge prepaid mobile, DTH, or FASTag",
        icon = Icons.Filled.PhoneAndroid,
        testTag = "tile_recharge",
        useTertiaryAccent = true,
        onClick = onNavigateToBills,
      ),
      DashboardServiceItem(
        id = "bank_upi",
        title = "Bank / UPI",
        subtitle = "IFSC or VPA",
        semanticAnnouncement = "Transfer money to a bank account or UPI ID",
        icon = Icons.Filled.AccountBalance,
        testTag = "tile_to_bank",
        onClick = { activeModal = HomeModalType.TO_BANK_UPI },
      ),
      DashboardServiceItem(
        id = "to_self",
        title = "To Self",
        subtitle = "Own accounts",
        semanticAnnouncement = "Transfer money between your own linked bank accounts",
        icon = Icons.AutoMirrored.Filled.CompareArrows,
        testTag = "tile_to_self",
        useSecondaryAccent = true,
        onClick = { activeModal = HomeModalType.TO_SELF },
      ),
      DashboardServiceItem(
        id = "check_balance",
        title = "Balance",
        subtitle = "Check instant",
        semanticAnnouncement = "Check bank account balance securely",
        icon = Icons.Filled.AccountBalanceWallet,
        testTag = "tile_check_balance",
        useTertiaryAccent = true,
        onClick = { activeModal = HomeModalType.CHECK_BALANCE },
      ),
      DashboardServiceItem(
        id = "my_qr",
        title = "My QR",
        subtitle = "Receive money",
        semanticAnnouncement = "Show My QR code to receive money",
        icon = Icons.Filled.QrCode2,
        testTag = "tile_my_qr",
        onClick = { showMyQrDialog = true },
      ),
      DashboardServiceItem(
        id = "upi_lite",
        title = "UPI Lite",
        subtitle = "PIN-less <₹500",
        semanticAnnouncement = "Manage UPI Lite wallet and UPI International roaming",
        icon = Icons.Filled.Bolt,
        testTag = "tile_upi_lite_intl",
        useSecondaryAccent = true,
        onClick = { activeModal = HomeModalType.UPI_LITE_INTL },
      ),
      DashboardServiceItem(
        id = "ai_help",
        title = "AI Help",
        subtitle = "Voice & chat",
        semanticAnnouncement = "Open multilingual AI voice and chat payment assistant",
        icon = Icons.Filled.AutoAwesome,
        testTag = "tile_ai_help",
        useTertiaryAccent = true,
        onClick = onNavigateToAssistant,
      ),
    )
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val gridColumns = if (maxWidth >= 600.dp) 4 else 3

    LazyVerticalGrid(
      columns = GridCells.Fixed(gridColumns),
      modifier = Modifier
        .fillMaxSize()
        .testTag("dashboard_grid"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // 1. High-Contrast Hero Balance Card
      item(span = { GridItemSpan(maxLineSpan) }) {
        val balanceVoiceText = if (hideBalances) {
          "Primary account ${primaryAccount?.bankName ?: "HDFC Bank"}, balance hidden"
        } else {
          "Primary account ${primaryAccount?.bankName ?: "HDFC Bank"}, balance ${formatInr(primaryAccount?.balanceInr ?: 0.0)}"
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = balanceVoiceText }
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
              alpha = 0.35f,
              modifier = Modifier.matchParentSize(),
            )
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.68f),
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
                    color = MaterialTheme.colorScheme.onPrimary,
                  )
                  Text(
                    text = primaryAccount?.upiId ?: "aarav.sharma@payshield",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                  )
                }
                IconButton(
                  onClick = onToggleHideBalances,
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .semantics {
                      stateDescription = if (hideBalances) "Balance hidden" else "Balance visible"
                    }
                    .testTag("toggle_balance_visibility"),
                ) {
                  Icon(
                    imageVector = if (hideBalances) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (hideBalances) "Show account balance" else "Hide account balance",
                    tint = MaterialTheme.colorScheme.onPrimary,
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (hideBalances) "₹ ••,•••.••" else formatInr(primaryAccount?.balanceInr ?: 0.0),
                style = MaterialTheme.typography.displaySmall,
                fontFamily = JetBrainsMonoFamily,
                color = MaterialTheme.colorScheme.onPrimary,
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
                    .minimumInteractiveComponentSize()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { activeModal = HomeModalType.UPI_LITE_INTL }
                    .semantics(mergeDescendants = true) {
                      role = Role.Button
                      contentDescription = "UPI Lite wallet, balance ${formatInr(liteAccount?.balanceInr ?: 0.0)}"
                    }
                    .testTag("hero_upi_lite_pill"),
                  color = Color.Black.copy(alpha = 0.28f),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Bolt,
                      contentDescription = null,
                      tint = Color(0xFF7BFFCA),
                      modifier = Modifier.size(18.dp),
                    )
                    Column {
                      Text(
                        text = "UPI Lite",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
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
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                  ),
                  shape = MaterialTheme.shapes.small,
                  modifier = Modifier
                    .height(48.dp)
                    .semantics { contentDescription = "Scan QR code and pay" }
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

      // 2. Voice & Multilingual AI Assistant Quick Bar
      item(span = { GridItemSpan(maxLineSpan) }) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onNavigateToAssistant)
            .semantics(mergeDescendants = true) {
              role = Role.Button
              contentDescription = "Ask PayShield AI in English, Hindi, or Marathi. Tap to speak or type a payment command."
            }
            .testTag("home_ai_assistant_banner"),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
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
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondary,
                )
              }
              Column {
                Text(
                  text = "Ask PayShield AI (EN / हिंदी / मराठी)",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                  text = "Say \"Send ₹500 to Dad\", check insights or resolve issues",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      // 3. Services Section Heading
      item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Quick Payments & Services",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
          )
          if (uiState.preferences.upiInternationalEnabled) {
            SecurityStatusChip(label = "Intl Active")
          }
        }
      }

      // 4. Accessible Service Icon Grid (Scan & Pay, Send Money, Recharge, etc.)
      items(serviceGridItems, key = { it.id }) { item ->
        val containerColor = when {
          item.useSecondaryAccent -> MaterialTheme.colorScheme.secondaryContainer
          item.useTertiaryAccent -> MaterialTheme.colorScheme.tertiaryContainer
          else -> MaterialTheme.colorScheme.primaryContainer
        }
        val iconTint = when {
          item.useSecondaryAccent -> MaterialTheme.colorScheme.onSecondaryContainer
          item.useTertiaryAccent -> MaterialTheme.colorScheme.onTertiaryContainer
          else -> MaterialTheme.colorScheme.onPrimaryContainer
        }
        DashboardServiceGridTile(
          item = item,
          iconContainerColor = containerColor,
          iconTint = iconTint,
        )
      }

      // 5. Recent Payments Heading
      item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
          text = "Recent Payments",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier
            .padding(top = 6.dp)
            .semantics { heading() },
        )
      }

      if (uiState.transactions.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
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
              Text(
                text = "No transactions yet. Tap Scan & Pay to start.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      } else {
        items(uiState.transactions.take(8), key = { it.txId }, span = { GridItemSpan(maxLineSpan) }) { tx ->
          TransactionRowCard(tx = tx, onClick = { onTransactionClick(tx) })
        }
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
private fun DashboardServiceGridTile(
  item: DashboardServiceItem,
  iconContainerColor: Color,
  iconTint: Color,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = item.onClick)
      .semantics(mergeDescendants = true) {
        role = Role.Button
        contentDescription = item.semanticAnnouncement
      }
      .testTag(item.testTag),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(iconContainerColor),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = item.icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(26.dp),
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = item.title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = item.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
