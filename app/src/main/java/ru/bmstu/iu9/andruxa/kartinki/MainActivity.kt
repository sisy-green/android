package ru.bmstu.iu9.andruxa.kartinki

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
  private fun createNotificationChannel(CHANNEL_ID: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.channel_name)
      val descriptionText = getString(R.string.channel_description)
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
      }
      val notificationManager: NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  private var localeChangeBroadcastReciever: LocaleChangeBroadcastReciever? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.localeChangeBroadcastReciever = LocaleChangeBroadcastReciever(this)
    registerReceiver(this.localeChangeBroadcastReciever, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
    val viewModel = MainViewModel()
    val categoriesViewModel = CategoriesViewModel()
    val userRepo = UserRepo(userDataStore)
    lifecycleScope.launch { userRepo.initSaved() }
    createNotificationChannel(CHANNEL_ID)
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
          NavHost(navController = navController, startDestination = "categories") {
//            composable("list") { MainList(navController, viewModel) }
            composable("image/{imageId}") { backStackEntry ->
              ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
            }
            composable("settings") { Settings(navController, userRepo, dataStore) }
            composable("categories") { CategoryList(navController, categoriesViewModel) }
            composable(
              "category/{ID}?name={name}",
              arguments = listOf(navArgument("name") { defaultValue = "Images" }
              )) { backStackEntry ->
              ImageList(
                backStackEntry.arguments?.getString("name"),
                navController = navController,
                viewModel = viewModel,
                categoryID = backStackEntry.arguments?.getString("ID")
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
    unregisterReceiver(this.localeChangeBroadcastReciever)
  }
}


@Composable
fun CategoryList(navController: NavController, viewModel: CategoriesViewModel) {

  val categories = viewModel.categories
  Scaffold {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {}
        .padding(vertical = 15.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
      items(
        items = categories,
        key = { item -> item.id },
        itemContent = {
          Text(
            text = it.name,
            style = MaterialTheme.typography.h6,
            modifier = Modifier
              .padding(start = 10.dp)
              .fillMaxWidth()
              .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null
              ) {
                navController.navigate("category/${it.id}?name=${it.name}")
              },
          )
        },
      )
    }
  }
}

@Composable
fun ImageList(
  listName: String? = "",
  navController: NavController,
  viewModel: MainViewModel,
  categoryID: String? = null
) {
//  val images = viewModel.images.distinctBy{ it.id }
  val images = remember { viewModel.search(categoryID) }

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
            ) {
              navController.popBackStack()
            },
          )
          if (listName != null) {
            Text(
              text = listName,
              style = MaterialTheme.typography.h5,
              modifier = Modifier.padding(start = 20.dp),
            )
          }
        }
      }
    },
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
        key = { item -> item.id },
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
fun MainList(navController: NavController, viewModel: MainViewModel) {
  ImageList(navController = navController, viewModel = viewModel)
}

@Composable
fun ImageViewer(id: String?, viewModel: MainViewModel) {

  id?.let {
    val image = viewModel.images.find { item -> item.id == id }
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
fun ProfileItem(userRepo: UserRepo, dataStore: DataStore<Preferences>) {
  val defaultKey = dataStore.data.map { p ->
    p[stringPreferencesKey("name")] ?: "default"
  }.collectAsState(initial = "default").value
  var expanded by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val items = mutableListOf<SettingsData>()
  items.addAll(userRepo.saved)
  var selectedItem = defaultKey
  val onChange = { value: String ->
    coroutineScope.launch {
      selectedItem = value
      dataStore.edit { settings -> settings[stringPreferencesKey("name")] = value }
      val newSettings = userRepo.getUsers().find { settingsData -> settingsData.name == value }
      dataStore.edit { settings -> settings[stringPreferencesKey("lang")] = newSettings!!.language }
      dataStore.edit { settings ->
        settings[intPreferencesKey("color")] = COLORS.values().indexOf(newSettings!!.color)
      }
      dataStore.edit { settings ->
        settings[intPreferencesKey("theme")] = THEMES.values().indexOf(newSettings!!.theme)
      }
    }
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { expanded = !expanded }
      .padding(vertical = 15.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "Профиль",
      style = MaterialTheme.typography.h6,
    )
    Text(
      text = defaultKey,
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
          val onClick: (String) -> Unit = { item ->
            Timer().schedule(object : TimerTask() {
              override fun run() {
                selectedItem = item
                expanded = false
                onChange(item)
              }
            }, 5)
          }
          items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(
                selected = selectedItem == item.name,
                onClick = { onClick(item.name) })
              Text(
                text = item.name,
                modifier = Modifier
                  .padding(start = 10.dp)
                  .fillMaxWidth()
                  .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                  ) { onClick(item.name) },
              )
            }
            Spacer(modifier = Modifier.height(20.dp))
          }
          var input by rememberSaveable { mutableStateOf("") }
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,

            ) {
            TextField(
              value = input,
              onValueChange = { input = it },
              label = { Text("Новый профиль") },
              modifier = Modifier.width(200.dp),
            )
            Text("Save", modifier = Modifier
              .wrapContentWidth()
              .padding(start = 10.dp)
              .clickable {
                coroutineScope.launch {
                  val users = userRepo.getUsers()
                  if (users.find { user -> user.name == input } != null) {
                    Log.e("New Profile", "already exists")
                  } else {
                    userRepo.addUser(SettingsData(input, "en", COLORS.RED, THEMES.LIGHT))
                    userRepo.initSaved()
                    selectedItem = input
                    expanded = false
                    onChange(input)
                  }
                }
              })
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
fun <K> SettingsItem(map: Map<K, String>, defaultKey: K, label: String, onChange: (K) -> Unit) {
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
fun Settings(
  navController: NavController,
  userRepo: UserRepo,
  dataStore: DataStore<Preferences>
) {
  val currentProfile = dataStore.data.map { p ->
    p[stringPreferencesKey("name")] ?: "default"
  }.collectAsState(initial = "default").value
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
      ProfileItem(userRepo = userRepo, dataStore = dataStore)
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        text = stringResource(R.string.settings),
        style = MaterialTheme.typography.subtitle1,
      )
      SettingsItem(
        map = languages,
        defaultKey = dataStore.data.map {
          it[stringPreferencesKey("lang")] ?: "ru"
        }.collectAsState(initial = "ru").value,
        label = stringResource(R.string.language),
        onChange = { value ->
          coroutineScope.launch {
            dataStore.edit { settings ->
              settings[stringPreferencesKey("lang")] = value
            }
            val currentSettings =
              userRepo.getUsers().find { settingsData -> settingsData.name == currentProfile }
            if (currentSettings != null) {
              userRepo.editUser(
                SettingsData(
                  currentSettings.name, value,
                  currentSettings.color, currentSettings.theme
                )
              )
            }
          }
        }
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
            val currentSettings =
              userRepo.getUsers().find { settingsData -> settingsData.name == currentProfile }
            if (currentSettings != null) {
              userRepo.editUser(
                SettingsData(
                  currentSettings.name, currentSettings.language,
                  value, currentSettings.theme
                )
              )
            }
          }
        },
      )
      Divider()
      SettingsItem(
        map = themes,
        defaultKey = THEMES.values()[dataStore.data.map {
          it[intPreferencesKey("theme")] ?: THEMES.values().indexOf(THEMES.SYSTEM)
        }.collectAsState(initial = THEMES.values().indexOf(THEMES.SYSTEM)).value],
        label = stringResource(R.string.theme),
        onChange = { value ->
          coroutineScope.launch {
            dataStore.edit { settings ->
              settings[intPreferencesKey("theme")] = THEMES.values().indexOf(value)
            }
            val currentSettings =
              userRepo.getUsers().find { settingsData -> settingsData.name == currentProfile }
            if (currentSettings != null) {
              userRepo.editUser(
                SettingsData(
                  currentSettings.name, currentSettings.language,
                  currentSettings.color, value
                )
              )
            }
          }
        },
      )
    }
  }
}

