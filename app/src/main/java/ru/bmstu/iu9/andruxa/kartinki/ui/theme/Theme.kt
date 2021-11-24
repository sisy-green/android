package ru.bmstu.iu9.andruxa.kartinki.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import ru.bmstu.iu9.andruxa.kartinki.COLORS

private fun darkColorPalette(color: COLORS): Colors {
  return when (color) {
    COLORS.RED -> darkColors(
      primary = Red200,
      primaryVariant = Red700,
      secondary = Yellow200,
    )
    else -> darkColors(
      primary = Purple200,
      primaryVariant = Purple700,
      secondary = Teal200,
    )
  }
}

private fun lightColorPalette(color: COLORS): Colors {
  return when (color) {
    COLORS.RED -> lightColors(
      primary = Red200,
      primaryVariant = Red500,
      secondary = Yellow200
    )
    else ->
      lightColors(
        primary = Purple500,
        primaryVariant = Purple700,
        secondary = Teal200
      )
  }
}

/* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/

@Composable
fun KartinkiTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  themeColor: COLORS = COLORS.PURPLE, content: @Composable () -> Unit
) {
  val colors = if (darkTheme) {
    darkColorPalette(themeColor)
  } else {
    lightColorPalette(themeColor)
  }

  MaterialTheme(
    colors = colors,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}