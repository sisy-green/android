package ru.bmstu.iu9.andruxa.kartinki

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = MainViewModel()
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
      KartinkiTheme(darkTheme.value) {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          val navController = rememberNavController()
          NavHost(navController = navController, startDestination = "list") {
            composable("list") { MainList(navController, viewModel) }
            composable("image/{imageId}") { backStackEntry ->
              ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
            }
            composable("settings") { Settings(navController, dataStore) }
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
}

@Composable
fun MainList(navController: NavController, viewModel: MainViewModel) {
  val images = remember { viewModel.images }
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = {
        navController.navigate("settings")
      }) {
        Icon(Icons.Default.Menu, contentDescription = "settings")
      }
    }
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      items(
        items = images,
        key = { it.id },
        itemContent = {
          Image(
            painter = rememberImagePainter(it.asset, builder = { size(OriginalSize) }),
            contentDescription = it.description,
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight()
              .clickable { navController.navigate("image/${it.id}") },
            contentScale = ContentScale.FillWidth,
          )
        },
      )
    }
  }
}

@Composable
fun ImageViewer(id: String?, viewModel: MainViewModel) {
  id?.let {
    val image = remember { viewModel.images.find { it.id == id }}
    val context = LocalContext.current
    image?.let { image ->
      Scaffold(
        floatingActionButton = {
          val shareResource = stringResource(R.string.share)
          FloatingActionButton(onClick = {
            val shareIntent: Intent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, image.asset)
              type = "image/*"
            }
            startActivity(
              context,
              Intent.createChooser(shareIntent, shareResource),
              null
            )
          }) {
            Icon(Icons.Default.Share, contentDescription = "share")
          }
        }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(R.color.black)),
          verticalArrangement = Arrangement.Center,
        ) {
          Image(
            painter = rememberImagePainter(image.asset, builder = { size(OriginalSize) }),
            contentDescription = image.description,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
          )
          Text(
            text = image.description,
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 10.dp, top = 10.dp, end = 10.dp),
          )
        }
      }
    }
  }
}

@Composable
fun <K>SettingsItem(map: Map<K, String>, defaultKey: K, label: String, onChange: (K) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val items = remember { map.keys }
  var selectedItem: K = defaultKey
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { expanded = !expanded }
      .padding(vertical = 15.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.h6,
    )
    Text(
      text = map[defaultKey]!!,
      style = MaterialTheme.typography.subtitle1,
    )
  }
  if (expanded) {
    Dialog(onDismissRequest = { expanded = false }) {
      Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.End
        ) {
          val onClick: (K) -> Unit = { item ->
            Timer().schedule(object : TimerTask() {
              override fun run() {
                selectedItem = item
                expanded = false
                onChange(item)
              }
            }, 150)
          }
          items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = selectedItem == item, onClick = { onClick(item) })
              Text(
                text = map[item]!!,
                modifier = Modifier
                  .padding(start = 10.dp)
                  .fillMaxWidth()
                  .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                  ) { onClick(item) },
              )
            }
            Spacer(modifier = Modifier.height(20.dp))
          }
          Text(stringResource(R.string.cancel), modifier = Modifier
            .wrapContentWidth()
            .clickable { expanded = false })
        }
      }
    }
  }
}

@Composable
fun Settings(navController: NavController, dataStore: DataStore<Preferences>) {
  val languages: Map<String, String> =
    LANGUAGE_CODES.zip(stringArrayResource(R.array.languages)).toMap()
  val colors: Map<COLORS, String> =
    COLORS.values().zip(stringArrayResource(R.array.colors)).toMap()
  val themes: Map<THEMES, String> =
    THEMES.values().zip(stringArrayResource(R.array.themes)).toMap()
  Scaffold(
    topBar = {
      TopAppBar {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.ArrowBack,
            contentDescription = "back",
            modifier = Modifier.clickable(
              interactionSource = MutableInteractionSource(),
              indication = null,
            ) { navController.popBackStack() },
          )
          Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start = 20.dp),
          )
        }
      }
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      val coroutineScope = rememberCoroutineScope()
      Text(
        text = stringResource(R.string.profile),
        style = MaterialTheme.typography.subtitle1,
      )
      SettingsItem(map = languages, defaultKey = "ru", label = "\${имя профиля}", {})
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        text = stringResource(R.string.settings),
        style = MaterialTheme.typography.subtitle1,
      )
      SettingsItem(
        map = languages,
        defaultKey = dataStore.data.map{
          it[stringPreferencesKey("lang")] ?: "ru"
        }.collectAsState(initial = "ru").value,
        label = stringResource(R.string.language),
        onChange = { value ->
          coroutineScope.launch {
            dataStore.edit { settings ->
              settings[stringPreferencesKey("lang")] = value
            }
          }
        },
      )
      Divider()
      SettingsItem(
        map = colors,
        defaultKey = COLORS.values()[dataStore.data.map {
          it[intPreferencesKey("color")] ?: COLORS.values().indexOf(COLORS.PURPLE)
        }.collectAsState(initial = COLORS.values().indexOf(COLORS.PURPLE)).value],
        label = stringResource(R.string.color),
        onChange = { value ->
          coroutineScope.launch {
            dataStore.edit { settings ->
              settings[intPreferencesKey("color")] = COLORS.values().indexOf(value)
            }
          }
        },
      )
      Divider()
      SettingsItem(
        map = themes,
        defaultKey = THEMES.values()[dataStore.data.map{
          it[intPreferencesKey("theme")] ?: THEMES.values().indexOf(THEMES.SYSTEM)
        }.collectAsState(initial = THEMES.values().indexOf(THEMES.SYSTEM)).value],
        label = stringResource(R.string.theme),
        onChange = { value ->
          coroutineScope.launch {
            dataStore.edit { settings ->
              settings[intPreferencesKey("theme")] = THEMES.values().indexOf(value)
            }
          }
        },
      )
    }
  }
}
