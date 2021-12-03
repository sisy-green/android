package ru.bmstu.iu9.andruxa.kartinki

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.bmstu.iu9.andruxa.kartinki.components.*
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
  private var localeChangeBroadcastReceiver: LocaleChangeBroadcastReciever? = null

  @ExperimentalComposeUiApi
  @ExperimentalAnimationApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.localeChangeBroadcastReceiver = LocaleChangeBroadcastReciever(this)
    registerReceiver(this.localeChangeBroadcastReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))

    val viewModel = MainViewModel()

    val userRepo = UserRepo(userDataStore)
    lifecycleScope.launch { userRepo.initSaved() }

    Intent(this, NotificationService::class.java).also { intent ->
      startService(intent)
    }

    setContent {
      val language = this.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("lang")] ?: "ru"
      }.collectAsState(initial = "ru")
      this.changeLocale(language.value)

      val theme: State<Int> = this.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("theme")] ?: THEMES.values().indexOf(THEMES.SYSTEM)
      }.collectAsState(initial = THEMES.values().indexOf(THEMES.SYSTEM))
      val darkTheme = mutableStateOf(
        when (theme.value) {
          THEMES.values().indexOf(THEMES.LIGHT) -> false
          THEMES.values().indexOf(THEMES.DARK) -> true
          else -> isSystemInDarkTheme()
        }
      )

      val color: COLORS = COLORS.values()[this.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("color")] ?: COLORS.values().indexOf(COLORS.PURPLE)
      }.collectAsState(initial = COLORS.values().indexOf(COLORS.PURPLE)).value]

      KartinkiTheme(darkTheme.value, color) {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          val navController = rememberNavController()
          val currentBackStackEntry = navController.currentBackStackEntryAsState()
          val caption = remember { mutableStateOf("") }
          Scaffold(
            topBar = {
              TopBar(caption, navController, currentBackStackEntry.value?.destination?.route?.startsWith("image/") == false)
            },
            bottomBar = {
              BottomBar(navController, currentBackStackEntry.value?.destination?.route?.startsWith("image/") == false)
            },
          ) {
            NavHost(
              navController = navController,
              startDestination = "home",
              modifier = if (currentBackStackEntry.value?.destination?.route?.startsWith("image/") == false) {
                Modifier.padding(bottom = 56.dp)
              } else {
                Modifier
              },
            ) {
              composable("home") {
                ImageList(
                  navController = navController,
                  viewModel = viewModel,
                  sort = "newest",
                )
                caption.value = getString(R.string.home)
              }
              composable("image/{imageId}") { backStackEntry ->
                ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
              }
              composable("settings") {
                Settings(userRepo, dataStore)
                caption.value = getString(R.string.settings)
              }
              composable("categories") {
                CategoryList(navController, viewModel)
                caption.value = getString(R.string.categories)
              }
              composable("search/?name={name}&query={query}") { backStackEntry ->
                ImageList(
                  navController = navController,
                  viewModel = viewModel,
                  query = backStackEntry.arguments?.getString("query"),
                )
                caption.value = backStackEntry.arguments?.getString("name")!!
              }
              composable("category/{categoryId}?name={name}") { backStackEntry ->
                ImageList(
                  navController = navController,
                  viewModel = viewModel,
                  categoryID = backStackEntry.arguments?.getString("categoryId"),
                )
                caption.value = backStackEntry.arguments?.getString("name")!!
              }
            }
          }
        }
      }
    }
  }

  private fun changeLocale(code: String) {
    val locale = Locale(code)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
//    createConfigurationContext(config)
    resources.updateConfiguration(config, resources.displayMetrics)
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(this.localeChangeBroadcastReceiver)
  }
}
