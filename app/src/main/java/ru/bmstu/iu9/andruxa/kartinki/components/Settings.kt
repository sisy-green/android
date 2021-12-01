package ru.bmstu.iu9.andruxa.kartinki.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.bmstu.iu9.andruxa.kartinki.*
import ru.bmstu.iu9.andruxa.kartinki.R
import java.util.*

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
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = defaultKey,
      style = MaterialTheme.typography.h6,
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
            val context = LocalContext.current
            Text("Save", modifier = Modifier
              .wrapContentWidth()
              .padding(start = 10.dp)
              .clickable {
                coroutineScope.launch {
                  val users = userRepo.getUsers()
                  if (users.find { user -> user.name == input } != null) {
                    Toast
                      .makeText(
                        context,
                        "Профиль с таким названием уже существует",
                        Toast.LENGTH_SHORT
                      )
                      .show()
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
          Text(
            stringResource(R.string.cancel), modifier = Modifier
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
          Text(
            stringResource(R.string.cancel), modifier = Modifier
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
    topBar = { TopBar(mutableStateOf(stringResource(R.string.settings)), navController) },
    bottomBar = { BottomBar(navController = navController, current = "settings") }
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