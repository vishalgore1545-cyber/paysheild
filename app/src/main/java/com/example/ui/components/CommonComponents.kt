package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.data.local.BankAccountEntity
import com.example.data.local.TransactionEntity
import com.example.security.BiometricAuthHelper
import com.example.security.BiometricAvailability
import com.example.security.PacketInspectionResult
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.PendingSecureAction
import com.example.ui.viewmodel.StepUpWarningState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val indiaLocale = Locale("en", "IN")

fun formatInr(amount: Double): String {
  val fmt = NumberFormat.getCurrencyInstance(indiaLocale)
  return fmt.format(amount)
}

fun formatTimestamp(millis: Long): String {
  val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  return sdf.format(Date(millis))
}

@Composable
fun AccessibleActionTile(
  label: String,
  icon: ImageVector,
  containerColor: Color,
  iconTint: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
) {
  Card(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(containerColor),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = iconTint,
          modifier = Modifier.size(26.dp),
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun SecurityStatusChip(
  label: String,
  isPositive: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val bg = if (isPositive) {
    MaterialTheme.colorScheme.secondaryContainer
  } else {
    MaterialTheme.colorScheme.errorContainer
  }
  val fg = if (isPositive) {
    MaterialTheme.colorScheme.onSecondaryContainer
  } else {
    MaterialTheme.colorScheme.onErrorContainer
  }
  Surface(
    modifier = modifier,
    shape = CircleShape,
    color = bg,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
        imageVector = if (isPositive) Icons.Filled.Shield else Icons.Filled.Warning,
        contentDescription = null,
        tint = fg,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPinSheet(
  pendingAction: PendingSecureAction,
  pinError: String?,
  onConfirmPin: (String, Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val activity = context as? FragmentActivity
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var pin by remember(pendingAction) { mutableStateOf("") }
  var biometricFeedback by remember(pendingAction) { mutableStateOf<String?>(null) }

  val actionTitle = when (pendingAction) {
    is PendingSecureAction.Payment -> "Pay ${formatInr(pendingAction.amountInr)} to ${pendingAction.title}"
    is PendingSecureAction.Investment -> "Invest ${formatInr(pendingAction.amountInr)} in ${pendingAction.holdingName}"
    is PendingSecureAction.BalanceInquiry -> "Check Balance: ${pendingAction.account.bankName}"
  }

  val authenticator = remember(activity) {
    activity?.let { com.example.security.BiometricTransactionAuthenticator(it) }
  }

  val launchBiometric: () -> Unit = {
    if (authenticator != null) {
      authenticator.authenticateForTransaction(
        title = "Authorize PayShield Transaction",
        subtitle = actionTitle,
        description = "Fingerprint or face verification protects every payment",
      ) { result ->
        when (result) {
          is com.example.security.BiometricAuthResult.Success -> onConfirmPin("", true)
          is com.example.security.BiometricAuthResult.CancelledOrFallback -> biometricFeedback = result.reason
          is com.example.security.BiometricAuthResult.Error -> biometricFeedback = result.message
        }
      }
    } else {
      biometricFeedback = "Biometric prompt unavailable; enter 4-digit PIN 1234."
    }
  }

  androidx.compose.runtime.LaunchedEffect(pendingAction) {
    if (authenticator?.checkAvailability() == BiometricAvailability.AVAILABLE) {
      launchBiometric()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      SecurityStatusChip(label = "Biometric & Encrypted PIN Auth")
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = stringResource(R.string.upi_pin_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = actionTitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.upi_pin_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = launchBiometric,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("pin_biometric_prompt_button"),
      ) {
        Icon(Icons.Filled.Fingerprint, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Verify with Fingerprint / Face ID")
      }

      if (biometricFeedback != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = biometricFeedback!!,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.tertiary,
          textAlign = TextAlign.Center,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(4) { index ->
          val filled = index < pin.length
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(
                if (filled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
              ),
          )
        }
      }

      if (pinError != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = pinError,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL"),
      )
      rows.forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          row.forEach { key ->
            when (key) {
              "BIO" -> {
                OutlinedButton(
                  onClick = launchBiometric,
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("pin_biometric_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "BiometricPrompt",
                    tint = MaterialTheme.colorScheme.secondary,
                  )
                }
              }
              "DEL" -> {
                OutlinedButton(
                  onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("pin_backspace_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                  )
                }
              }
              else -> {
                OutlinedButton(
                  onClick = {
                    if (pin.length < 4) {
                      val next = pin + key
                      pin = next
                      if (next.length == 4) {
                        onConfirmPin(next, false)
                      }
                    }
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("pin_key_$key"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = key,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = JetBrainsMonoFamily,
                  )
                }
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        TextButton(
          onClick = onDismiss,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("pin_cancel_button"),
        ) {
          Text("Cancel")
        }
        Button(
          onClick = { onConfirmPin(pin, false) },
          enabled = pin.length == 4,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("pin_submit_button"),
        ) {
          Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Authorize")
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun StepUpFraudWarningDialog(
  warning: StepUpWarningState,
  onProceedAnyway: () -> Unit,
  onCancelPayment: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onCancelPayment,
    icon = {
      Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = "Safety Check",
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(44.dp),
      )
    },
    title = {
      Text(
        text = warning.assessment.consumerWarningTitle ?: "Safety Check",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = warning.assessment.consumerWarningMessage ?: "",
          style = MaterialTheme.typography.bodyMedium,
        )
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = MaterialTheme.shapes.small,
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "AI Safety Shield • ${warning.assessment.latencyMs}ms check",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            warning.assessment.signalsTriggered.forEach { sig ->
              Text(
                text = "• $sig",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onCancelPayment,
        modifier = Modifier.testTag("stepup_cancel_safe"),
      ) {
        Text("Cancel Payment")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onProceedAnyway,
        modifier = Modifier.testTag("stepup_proceed_anyway"),
      ) {
        Text("I Know Them, Continue")
      }
    },
  )
}

@Composable
fun BiometricAppLockOverlay(
  onUnlockSuccess: () -> Unit,
  onFallbackPinSuccess: () -> Unit,
) {
  val context = LocalContext.current
  val activity = context as? FragmentActivity
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var pinInput by remember { mutableStateOf("") }

  val triggerPrompt: () -> Unit = {
    if (activity != null) {
      BiometricAuthHelper.authenticate(
        activity = activity,
        title = context.getString(R.string.biometric_unlock_title),
        subtitle = context.getString(R.string.biometric_unlock_subtitle),
        onSuccess = onUnlockSuccess,
        onFallbackOrCancel = { msg -> statusMessage = msg },
      )
    } else {
      statusMessage = "Enter PIN 1234 to unlock."
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize().testTag("biometric_lock_screen"),
    color = MaterialTheme.colorScheme.background,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.VerifiedUser,
        contentDescription = "PayShield Locked",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text("PayShield is Locked", style = MaterialTheme.typography.headlineMedium)
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Verify your identity with Biometrics or App PIN to access your accounts.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = triggerPrompt,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("unlock_biometric_btn"),
      ) {
        Icon(Icons.Filled.Fingerprint, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Unlock with Fingerprint / Face")
      }
      if (statusMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(statusMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center)
      }
      Spacer(modifier = Modifier.height(20.dp))
      Text("Or tap Quick Demo PIN (1234)", style = MaterialTheme.typography.labelMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("1", "2", "3", "4").forEach { digit ->
          OutlinedButton(
            onClick = {
              val next = pinInput + digit
              pinInput = next
              if (next == "1234") onFallbackPinSuccess()
            },
            modifier = Modifier.size(52.dp),
          ) {
            Text(digit)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSecuritySheet(
  biometricLockEnabled: Boolean,
  onToggleBiometricLock: (Boolean) -> Unit,
  onLockNow: () -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val availability = remember { BiometricAuthHelper.checkAvailability(context) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SecurityStatusChip(label = "Defense-in-Depth Active Behind the Scenes")
      Text("Account & Security Settings", style = MaterialTheme.typography.headlineSmall)
      Text(
        text = "Aarav Sharma • aarav.sharma@payshield",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      HorizontalDivider()
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Biometric App Lock", style = MaterialTheme.typography.titleMedium)
          Text(
            text = when (availability) {
              BiometricAvailability.AVAILABLE -> "Hardware BiometricPrompt ready"
              BiometricAvailability.NONE_ENROLLED -> "Device ready (enroll fingerprint or use PIN)"
              BiometricAvailability.UNAVAILABLE -> "PIN fallback active on this device"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = biometricLockEnabled,
          onCheckedChange = onToggleBiometricLock,
          modifier = Modifier.testTag("biometric_app_lock_switch"),
        )
      }
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("Active Protection Layers", style = MaterialTheme.typography.titleSmall)
          Text("• TLS 1.3 Pinning + Firebase App Check Attestation", style = MaterialTheme.typography.bodySmall)
          Text("• Real-Time AI Fraud & Vishing Risk Scoring (<100ms)", style = MaterialTheme.typography.bodySmall)
          Text("• Parameterized AES-Encrypted Ledger Persistence", style = MaterialTheme.typography.bodySmall)
        }
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = {
            onDismiss()
            onLockNow()
          },
          modifier = Modifier.weight(1f).height(48.dp).testTag("lock_app_now_btn"),
        ) {
          Text("Test Biometric Lock")
        }
        Button(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(48.dp),
        ) {
          Text("Done")
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
fun MyQrReceiveDialog(
  upiId: String,
  accountName: String,
  onDismiss: () -> Unit,
) {
  val qrDark = MaterialTheme.colorScheme.onSurface
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Receive Money / My QR", textAlign = TextAlign.Center) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(accountName, style = MaterialTheme.typography.titleMedium)
        Box(
          modifier = Modifier
            .size(170.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White)
            .padding(14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val cells = 9
            val cellSize = size.width / cells
            for (r in 0 until cells) {
              for (c in 0 until cells) {
                val isCorner = (r < 3 && c < 3) || (r < 3 && c >= cells - 3) || (r >= cells - 3 && c < 3)
                val isPattern = ((r * 7 + c * 11) % 3 == 0)
                if (isCorner || isPattern) {
                  drawRect(
                    color = Color(0xFF201564),
                    topLeft = Offset(c * cellSize, r * cellSize),
                    size = Size(cellSize * 0.86f, cellSize * 0.86f),
                  )
                }
              }
            }
          }
          Icon(
            imageVector = Icons.Filled.QrCode2,
            contentDescription = null,
            tint = qrDark.copy(alpha = 0.01f),
            modifier = Modifier.size(1.dp),
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = MaterialTheme.shapes.small,
        ) {
          Text(
            text = upiId,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = JetBrainsMonoFamily,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
        Text(
          text = "Share this QR to receive instant UPI payments with zero fees.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, modifier = Modifier.testTag("my_qr_close_button")) {
        Text("Done")
      }
    },
  )
}

@Composable
fun TransactionReceiptDialog(
  tx: TransactionEntity,
  inspection: PacketInspectionResult?,
  onDismiss: () -> Unit,
) {
  val isSuccess = tx.status == "SUCCESS"
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Warning,
        contentDescription = tx.status,
        tint = if (isSuccess) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(44.dp),
      )
    },
    title = {
      Text(
        text = when (tx.status) {
          "SUCCESS" -> "Payment Successful"
          "PENDING" -> "Payment Pending"
          else -> "Payment Blocked"
        },
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = formatInr(tx.amountInr),
          style = MaterialTheme.typography.displaySmall,
          fontFamily = JetBrainsMonoFamily,
          color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Text(
          text = "${tx.title} (${tx.counterparty})",
          style = MaterialTheme.typography.titleMedium,
        )
        if (!tx.voucherCode.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small,
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Instant Voucher Code",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
              Text(
                text = tx.voucherCode,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = JetBrainsMonoFamily,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }
        HorizontalDivider()
        ReceiptRow("UTR / Ref", tx.txId)
        ReceiptRow("Payment Rail", tx.paymentRail)
        ReceiptRow("Time", formatTimestamp(tx.timestampMillis))
        ReceiptRow("Note", tx.note)
        if (inspection != null) {
          SecurityStatusChip(label = "Verified Across 5 Security Tiers")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("receipt_done_button"),
      ) {
        Text("Done")
      }
    },
  )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      fontFamily = JetBrainsMonoFamily,
      textAlign = TextAlign.End,
    )
  }
}

@Composable
fun BalanceCheckResultDialog(
  account: BankAccountEntity,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Balance fetched",
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(40.dp),
      )
    },
    title = {
      Text(
        text = "${account.bankName} Balance",
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = formatInr(account.balanceInr),
          style = MaterialTheme.typography.displaySmall,
          fontFamily = JetBrainsMonoFamily,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Account ${account.maskedAccount} • IFSC ${account.ifscCode}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SecurityStatusChip(label = "Biometric / PIN Verified")
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("balance_dialog_close"),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
fun FirewallBlockedDialog(
  reason: String,
  inspection: PacketInspectionResult?,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = "Payment Blocked",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(44.dp),
      )
    },
    title = {
      Text(
        text = "Blocked for Your Protection",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.error,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = reason, style = MaterialTheme.typography.bodyMedium)
        if (inspection != null && inspection.hopTraces.isNotEmpty()) {
          Text(
            text = "Protected by ${inspection.blockedAtLayer ?: "AI & WAF Shield"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
      ) {
        Text("Got It")
      }
    },
  )
}
