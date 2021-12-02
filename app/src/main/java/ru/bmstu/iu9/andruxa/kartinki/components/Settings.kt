package ru.bmstu.iu9.andruxa.kartinki.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.bmstu.iu9.andruxa.kartinki.*
import ru.bmstu.iu9.andruxa.kartinki.R
import java.util.*

@Composable
fun ProfileItem(userRepo: UserRepo, dataStore: DataStore<Preferences>) {
  val defaultKey = dataStore.data.map { p ->
    p[stringPreferencesKey("name")] ?: ""
  }.collectAsState(initial = "").value
  var expanded by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val items = remember { userRepo.saved }
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
      .padding(vertical = dimensionResource(R.dimen.padding_medium)),
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
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_big)),
          horizontalAlignment = Alignment.End,
        ) {
          val onClick: (String) -> Unit = { item ->
            selectedItem = item
            onChange(item)
            Timer().schedule(object : TimerTask() {
              override fun run() {
                expanded = false
              }
            }, 150)
          }
          items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth(0.9f)
                  .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                  ) { onClick(item.name) },
                verticalAlignment = Alignment.CenterVertically,
              ) {
                RadioButton(
                  selected = selectedItem == item.name,
                  onClick = { onClick(item.name) })
                Text(
                  text = item.name,
                  modifier = Modifier
                    .padding(start = dimensionResource(R.dimen.padding_small))
                    .fillMaxWidth(),
                )
              }
              if (items.size > 1) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "delete",
                  modifier = Modifier
                    .clickable(
                      role = Role.Button,
                      interactionSource = MutableInteractionSource(),
                      indication = rememberRipple(bounded = false, radius = 24.dp),
                    ) {
                      if (selectedItem == item.name) {
                        selectedItem = items[0].name
                        onChange(selectedItem)
                      }
                      coroutineScope.launch {
                        userRepo.deleteUser(item.name)
                        userRepo.initSaved()
                      }
                    }
                )
              }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_big)))
          }
          var input by rememberSaveable { mutableStateOf("") }
          val isError = remember { mutableStateOf(false) }
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val context = LocalContext.current
            val errorText = mutableStateOf(stringResource(R.string.profile_name_error))
            val onCheckClick = {
              if (input != "") {
                coroutineScope.launch {
                  val users = userRepo.getUsers()
                  if (users.find { user -> user.name == input } != null) {
                    isError.value = true
                    Toast
                      .makeText(context, errorText.value, Toast.LENGTH_SHORT)
                      .show()
                  } else {
                    dataStore.data.collect { settings ->
                      userRepo.addUser(
                        SettingsData(
                          input,
                          settings[stringPreferencesKey("lang")] ?: "ru",
                          COLORS.values()[settings[intPreferencesKey("color")] ?: 0],
                          THEMES.values()[settings[intPreferencesKey("theme")] ?: 0],
                        )
                      )
                      userRepo.initSaved()
                      selectedItem = input
                      onChange(input)
                      input = ""
                    }
                  }
                }
              }
            }
            TextField(
              value = input,
              onValueChange = {
                input = it
                isError.value = false
              },
              placeholder = { Text(stringResource(R.string.new_profile)) },
              modifier = Modifier.fillMaxWidth(0.87f),
              singleLine = true,
              isError = isError.value,
              shape = MaterialTheme.shapes.large,
              colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
              keyboardActions = KeyboardActions(onDone = { onCheckClick() })
            )
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = "save",
              modifier = Modifier
                .padding(start = dimensionResource(R.dimen.padding_small))
                .clickable(
                  role = Role.Button,
                  interactionSource = MutableInteractionSource(),
                  indication = rememberRipple(bounded = false, radius = 24.dp),
                  onClick = onCheckClick,
                ),
            )
          }
          Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_big)))
          Text(
            stringResource(R.string.cancel),
            modifier = Modifier
              .wrapContentWidth()
              .clickable { expanded = false },
          )
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
      .padding(vertical = dimensionResource(R.dimen.padding_medium)),
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
            .padding(dimensionResource(R.dimen.padding_big)),
          horizontalAlignment = Alignment.End,
        ) {
          val onClick: (K) -> Unit = { item ->
            selectedItem = item
            onChange(item)
            Timer().schedule(object : TimerTask() {
              override fun run() {
                expanded = false
              }
            }, 150)
          }
          items.forEach { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable(
                  interactionSource = MutableInteractionSource(),
                  indication = null
                ) { onClick(item) },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = selectedItem == item, onClick = { onClick(item) })
              Text(
                text = map[item]!!,
                modifier = Modifier
                  .padding(start = dimensionResource(R.dimen.padding_small))
                  .fillMaxWidth(),
              )
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_big)))
          }
          Text(
            stringResource(R.string.cancel),
            modifier = Modifier
              .wrapContentWidth()
              .clickable { expanded = false },
          )
        }
      }
    }
  }
}

@Composable
fun Settings(userRepo: UserRepo, dataStore: DataStore<Preferences>) {
  val currentProfile = dataStore.data.map { p ->
    p[stringPreferencesKey("name")] ?: ""
  }.collectAsState(initial = "").value
  val languages: Map<String, String> =
    LANGUAGE_CODES.zip(stringArrayResource(R.array.languages)).toMap()
  val colors: Map<COLORS, String> =
    COLORS.values().zip(stringArrayResource(R.array.colors)).toMap()
  val themes: Map<THEMES, String> =
    THEMES.values().zip(stringArrayResource(R.array.themes)).toMap()
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(dimensionResource(R.dimen.padding_big))
  ) {
    val coroutineScope = rememberCoroutineScope()
    Text(
      text = stringResource(R.string.profile),
      style = MaterialTheme.typography.subtitle1,
    )
    ProfileItem(userRepo, dataStore)
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_big)))
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
          currentSettings?.let {
            userRepo.editUser(
              SettingsData(
                currentSettings.name,
                value,
                currentSettings.color,
                currentSettings.theme,
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
          currentSettings?.let {
            userRepo.editUser(
              SettingsData(
                currentSettings.name,
                currentSettings.language,
                value,
                currentSettings.theme,
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
          currentSettings?.let {
            userRepo.editUser(
              SettingsData(
                currentSettings.name,
                currentSettings.language,
                currentSettings.color,
                value,
              )
            )
          }
        }
      },
    )
  }
}