package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaSansFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.Bold),
)

fun payShieldTypography(easyReadMode: Boolean = false): Typography {
  val scale = if (easyReadMode) 1.15f else 1.0f
  return Typography(
    displaySmall = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Bold,
      fontSize = (28 * scale).sp,
      lineHeight = (36 * scale).sp,
      letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Bold,
      fontSize = (22 * scale).sp,
      lineHeight = (28 * scale).sp,
    ),
    headlineSmall = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = (19 * scale).sp,
      lineHeight = (25 * scale).sp,
    ),
    titleLarge = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Bold,
      fontSize = (18 * scale).sp,
      lineHeight = (24 * scale).sp,
    ),
    titleMedium = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = (16 * scale).sp,
      lineHeight = (22 * scale).sp,
    ),
    titleSmall = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = (14 * scale).sp,
      lineHeight = (20 * scale).sp,
    ),
    bodyLarge = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Normal,
      fontSize = (15 * scale).sp,
      lineHeight = (22 * scale).sp,
    ),
    bodyMedium = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Normal,
      fontSize = (14 * scale).sp,
      lineHeight = (20 * scale).sp,
    ),
    bodySmall = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Normal,
      fontSize = (12 * scale).sp,
      lineHeight = (16 * scale).sp,
    ),
    labelLarge = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = (14 * scale).sp,
      lineHeight = (18 * scale).sp,
    ),
    labelMedium = TextStyle(
      fontFamily = PlusJakartaSansFamily,
      fontWeight = FontWeight.Medium,
      fontSize = (12 * scale).sp,
      lineHeight = (16 * scale).sp,
    ),
    labelSmall = TextStyle(
      fontFamily = JetBrainsMonoFamily,
      fontWeight = FontWeight.Medium,
      fontSize = (11 * scale).sp,
      lineHeight = (14 * scale).sp,
    ),
  )
}
