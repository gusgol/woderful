package me.goldhardt.woderful.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val primary = Color(0xFF99CBFF)
val onPrimary = Color(0xFF003354)
val primaryContainer = Color(0xFF004A78)
val secondary = Color(0xFFECC30A)
val onSecondary = Color(0xFF3B2F00)
val secondaryContainer = Color(0xFF564500)
val error = Color(0xFFFFB4AB)
val onError = Color(0xFF690005)
val background = Color(0xFF1A1C1E)
val onBackground = Color(0xFFE2E2E5)
val onSurface = Color(0xFFE2E2E5)
val surface = Color(0xFF42474E)
val onSurfaceVariant = Color(0xFFC2C7CF)

internal val wearColorPalette: Colors = Colors(
    primary = primary,
    primaryVariant = primaryContainer,
    secondary = secondary,
    secondaryVariant = secondaryContainer,
    background = background,
    surface = surface,
    error  = error,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onBackground = onBackground,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    onError = onError
)