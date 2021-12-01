package ru.bmstu.iu9.andruxa.kartinki

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.bmstu.iu9.andruxa.kartinki.components.*
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
  private var localeChangeBroadcastReceiver: LocaleChangeBroadcastReciever? = null

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
      val color: COLORS = COLORS.values()[this.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("color")] ?: COLORS.values().indexOf(COLORS.PURPLE)
      }.collectAsState(
        initial = COLORS.values().indexOf(COLORS.PURPLE)
      ).value]
      val darkTheme = mutableStateOf(
        when (theme.value) {
          THEMES.values().indexOf(THEMES.LIGHT) -> false
          THEMES.values().indexOf(THEMES.DARK) -> true
          else -> isSystemInDarkTheme()
        }
      )

      KartinkiTheme(darkTheme.value, color) {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          val navController = rememberNavController()
          NavHost(navController = navController, startDestination = "home") {
            composable("home") {
              ImageList(
                listName = "Главная",
                navController = navController,
                viewModel = viewModel,
                current = "home",
                sort = "newest",
              )
            }
            composable("image/{imageId}") { backStackEntry ->
              ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
            }
            composable("settings") { Settings(navController, userRepo, dataStore) }
            composable("categories") { CategoryList(navController, viewModel) }
            composable("search") { Search(navController = navController)}
            composable(
              "search/?name={name}&current={current}&query={query}",
              arguments = listOf(
                navArgument("name") { defaultValue = "Images" },
                navArgument("current") {defaultValue = ""},
                navArgument("search") {defaultValue = ""},
              ),
            ) { backStackEntry ->
              ImageList(
                listName = backStackEntry.arguments?.getString("name"),
                navController = navController,
                viewModel = viewModel,
                current = backStackEntry.arguments?.getString("current"),
                query = backStackEntry.arguments?.getString("query"),
                )
            }
            composable(
              "category/{ID}?name={name}&current={current}",
              arguments = listOf(
                navArgument("name") { defaultValue = "Images" },
                navArgument("current") {defaultValue = ""},
              ),
            ) { backStackEntry ->
              ImageList(
                listName = backStackEntry.arguments?.getString("name"),
                navController = navController,
                viewModel = viewModel,
                categoryID = backStackEntry.arguments?.getString("ID"),
                current = backStackEntry.arguments?.getString("current"),
              )
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

@Composable
fun Search(navController: NavController) {
  Scaffold(
    topBar = {
      TopAppBar {
        Text(
          text = "Search",
          style = MaterialTheme.typography.h5,
          modifier = Modifier.padding(start = 20.dp),
        )
      }
    },
    bottomBar = {
      BottomBar(navController = navController, current = "search")
    }
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      var input by rememberSaveable { mutableStateOf("") }
      TextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("Поиск") },
        modifier = Modifier.width(200.dp),
        maxLines = 1
      )
      input = input.replace("\n", "")
      IconButton(onClick = {
        navController.navigate("search/?name=${input}&current=search&query=${input}")
      }) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "search")
      }
    }
  }
}
