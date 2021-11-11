package ru.bmstu.iu9.andruxa.kartinki

const val API_KEY = "v2/cUd1Z1hFbWk4WGVpUmlkNmVqQUwzdE1ub3BrUDBUSm4vMzE0OTExNjg0L2N1c3RvbWVyLzQvVEhVNC1sdFBacVdEaW43NThkOUtGNmVHSi16dk5ZQmlDYUJkREREQl9NbzVIZVVfb1NRX3hQRWpRNGRiYzlkMFRGdnhtM1UxX3k0b2VrcHVwM2hUUWM5eTNnNUNfWF9YMURfSDNyLTd2QjljbmZQN2dyVkctMlFGMnZ4SDZhRjlMRnJTeXVteDdIWmhRSHZVM3FMenZlNHBVZjV5czZqVXJZay1HSmlsY2cyWExIdUkwSEE2WnB4c2E5clNjYWszQ044ZWprNk5ZVHhqRU42QW5tQW9hdy9ra1VsWGQ1WW1SM0dRMThkMGNEcU9B"

val LANGUGAGES = mapOf("ru" to "Русский", "en" to "Английский")

enum class COLORS {
  VIOLET,
  RED,
}

val COLORS_MAP = mapOf(COLORS.VIOLET to "Фиолетовый", COLORS.RED to "Красный")

enum class THEMES {
  LIGHT,
  DARK,
  SYSTEM,
}

val THEMES_NAMES_MAP = mapOf(
  THEMES.LIGHT to "Светлая",
  THEMES.DARK to "Тёмная",
  THEMES.SYSTEM to "Системная",
)
