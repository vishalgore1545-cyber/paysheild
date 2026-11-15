package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
  primary = DarkIndigoPrimary,
  onPrimary = DarkIndigoOnPrimary,
  primaryContainer = DarkIndigoContainer,
  onPrimaryContainer = DarkTextPrimary,
  secondary = DarkEmerald,
  onSecondary = Color(0xFF002617),
  secondaryContainer = DarkEmeraldContainer,
  onSecondaryContainer = Color(0xFFB5F7DA),
  tertiary = DarkAmber,
  onTertiary = Color(0xFF2B1600),
  tertiaryContainer = DarkAmberContainer,
  onTertiaryContainer = Color(0xFFFFE5C2),
  background = DarkObsidianBg,
  onBackground = DarkTextPrimary,
  surface = DarkCardSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkElevatedSurface,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkOutline,
  outlineVariant = DarkOutline,
  error = DarkThreatRed,
  onError = Color(0xFF3E0000),
  errorContainer = DarkThreatRedContainer,
  onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
  primary = IndigoPrimary,
  onPrimary = Color.White,
  primaryContainer = IndigoContainer,
  onPrimaryContainer = OnIndigoContainer,
  secondary = EmeraldShield,
  onSecondary = Color.White,
  secondaryContainer = EmeraldContainer,
  onSecondaryContainer = OnEmeraldContainer,
  tertiary = AmberGold,
  onTertiary = Color.White,
  tertiaryContainer = AmberContainer,
  onTertiaryContainer = OnAmberContainer,
  background = IvoryBackground,
  onBackground = InkPrimary,
  surface = PureWhiteSurface,
  onSurface = InkPrimary,
  surfaceVariant = SoftLavenderSurface,
  onSurfaceVariant = InkSecondary,
  outline = OutlineSubtle,
  outlineVariant = OutlineSubtle,
  error = ThreatRed,
  onError = Color.White,
  errorContainer = ThreatRedContainer,
  onErrorContainer = OnThreatRedContainer,
)

private val PayShieldShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun PayShieldTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  easyReadMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = payShieldTypography(easyReadMode = easyReadMode),
    shapes = PayShieldShapes,
    content = content,
  )
}
