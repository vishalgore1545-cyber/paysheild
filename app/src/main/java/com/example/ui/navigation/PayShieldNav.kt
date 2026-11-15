package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.components.AccountSecuritySheet
import com.example.ui.components.BalanceCheckResultDialog
import com.example.ui.components.BiometricAppLockOverlay
import com.example.ui.components.FirewallBlockedDialog
import com.example.ui.components.StepUpFraudWarningDialog
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.UpiPinSheet
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BillsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LifestyleScreen
import com.example.ui.screens.WealthScreen
import com.example.ui.viewmodel.PayShieldUiState
import com.example.ui.viewmodel.PayShieldViewModel

const val ROUTE_HOME = "home"
const val ROUTE_BILLS = "bills"
const val ROUTE_WEALTH = "wealth"
const val ROUTE_LIFESTYLE = "lifestyle"
const val ROUTE_ASSISTANT = "assistant"

private data class NavDestinationItem(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val navItems = listOf(
  NavDestinationItem(ROUTE_HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
  NavDestinationItem(ROUTE_BILLS, R.string.nav_bills, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
  NavDestinationItem(ROUTE_WEALTH, R.string.nav_wealth, Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
  NavDestinationItem(ROUTE_LIFESTYLE, R.string.nav_lifestyle, Icons.Filled.FlightTakeoff, Icons.Outlined.FlightTakeoff),
  NavDestinationItem(ROUTE_ASSISTANT, R.string.nav_assistant, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayShieldAppScaffold(
  uiState: PayShieldUiState,
  viewModel: PayShieldViewModel,
) {
  if (!uiState.isAppUnlocked) {
    BiometricAppLockOverlay(
      onUnlockSuccess = viewModel::unlockApp,
      onFallbackPinSuccess = viewModel::unlockApp,
    )
    return
  }

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME
  var showAccountSecuritySheet by remember { mutableStateOf(false) }

  val navigateToTab: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      topBar = {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { showAccountSecuritySheet = true }
                .testTag("profile_security_button"),
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_user_avatar),
                contentDescription = "Account and Security Settings",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape),
              )
              Column {
                Text(
                  text = stringResource(R.string.app_name),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = stringResource(R.string.security_badge_active),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.secondary,
                )
              }
            }
          },
          actions = {
            FilterChip(
              selected = uiState.preferences.easyReadMode,
              onClick = viewModel::toggleEasyReadMode,
              leadingIcon = {
                Icon(
                  imageVector = Icons.Filled.FormatSize,
                  contentDescription = "Toggle Easy Read Large Text",
                  modifier = Modifier.size(16.dp),
                )
              },
              label = { Text(stringResource(R.string.easy_read_mode)) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier
                .padding(end = 12.dp)
                .testTag("easy_read_toggle"),
            )
          },
        )
      },
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              val label = stringResource(item.labelRes)
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(label, maxLines = 1) },
                modifier = Modifier.testTag("nav_item_${item.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (useNavigationRail) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(),
          ) {
            Spacer(modifier = Modifier.size(12.dp))
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              val label = stringResource(item.labelRes)
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(label) },
                modifier = Modifier.testTag("rail_item_${item.route}"),
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter,
        ) {
          NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 840.dp),
          ) {
            composable(ROUTE_HOME) {
              DashboardScreen(
                uiState = uiState,
                onToggleHideBalances = viewModel::toggleHideBalances,
                onRequestPayment = viewModel::requestPayment,
                onRequestBalanceInquiry = viewModel::requestBalanceInquiry,
                onSetUpiInternational = viewModel::setUpiInternational,
                onTransactionClick = viewModel::showReceipt,
                onNavigateToBills = { navigateToTab(ROUTE_BILLS) },
                onNavigateToAssistant = { navigateToTab(ROUTE_ASSISTANT) },
              )
            }
            composable(ROUTE_BILLS) {
              BillsScreen(
                uiState = uiState,
                onRequestPayment = viewModel::requestPayment,
                onToggleAutoPay = viewModel::toggleAutoPay,
                onCreateAutoPay = viewModel::createAutoPayMandate,
              )
            }
            composable(ROUTE_WEALTH) {
              WealthScreen(
                uiState = uiState,
                onRequestInvestment = viewModel::requestInvestment,
              )
            }
            composable(ROUTE_LIFESTYLE) {
              LifestyleScreen(
                uiState = uiState,
                onRequestPayment = viewModel::requestPayment,
              )
            }
            composable(ROUTE_ASSISTANT) {
              AssistantScreen(
                uiState = uiState,
                onSelectLanguage = viewModel::setLanguage,
                onSendCommand = viewModel::sendAssistantCommand,
                onRequestPayment = viewModel::requestPayment,
                onGenerateInsights = viewModel::generateSmartSpendInsights,
                onTroubleshootTx = viewModel::troubleshootTransaction,
              )
            }
          }
        }
      }
    }
  }

  if (showAccountSecuritySheet) {
    AccountSecuritySheet(
      biometricLockEnabled = uiState.preferences.biometricAppLockEnabled,
      onToggleBiometricLock = viewModel::setBiometricAppLock,
      onLockNow = viewModel::lockAppForTesting,
      onDismiss = { showAccountSecuritySheet = false },
    )
  }

  uiState.stepUpWarning?.let { warning ->
    StepUpFraudWarningDialog(
      warning = warning,
      onProceedAnyway = viewModel::confirmStepUpWarning,
      onCancelPayment = viewModel::dismissStepUpWarning,
    )
  }

  uiState.pendingAction?.let { action ->
    UpiPinSheet(
      pendingAction = action,
      pinError = uiState.pinError,
      onConfirmPin = viewModel::verifyPinAndExecute,
      onDismiss = viewModel::cancelPendingAction,
    )
  }

  uiState.latestReceipt?.let { tx ->
    TransactionReceiptDialog(
      tx = tx,
      inspection = uiState.latestInspection,
      onDismiss = viewModel::dismissReceipt,
    )
  }

  uiState.latestBalanceInquiry?.let { account ->
    BalanceCheckResultDialog(
      account = account,
      onDismiss = viewModel::dismissBalanceInquiry,
    )
  }

  uiState.blockedAlertMessage?.let { reason ->
    FirewallBlockedDialog(
      reason = reason,
      inspection = uiState.latestInspection,
      onDismiss = viewModel::dismissBlockedAlert,
    )
  }
}
