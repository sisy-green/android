package ru.bmstu.iu9.andruxa.kartinki

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

val Context.userDataStore: DataStore<User> by dataStore(
  fileName = "user.proto",
  serializer = UserDataSerializer
)

class UserRepo(
  val settings: DataStore<User>,
) {
  val saved = mutableListOf<SettingsData>()
  suspend fun addUser(s: SettingsData) {
    settings.updateData { store -> User(store.users + s) }
  }

  suspend fun editUser(s: SettingsData) {
    settings.updateData { store ->
      User(
        store.users.map { settingsData ->
          if (settingsData.name == s.name) {
            s
          } else {
            settingsData
          }
        }
      )
    }
  }
  suspend fun getUsers(): List<SettingsData> {
    saved.clear()
    settings.data.collect { users -> saved.addAll(0, users.users) }
    return saved
  }
}

@Serializable
data class User(
  var users: List<SettingsData>
) {

  fun writeTo(output: OutputStream) {
    val data = Json.encodeToStream(User.serializer(), this, output)
  }

  fun addUser(settings: SettingsData) {
    users = users.toList()
  }
}

fun parseFrom(input: InputStream): User {
  return Json.decodeFromStream(input)
}

@Serializable
data class SettingsData(
  val name: String,
  val language: String,
  val color: COLORS,
  val theme: THEMES,
)

object UserDataSerializer : Serializer<User> {
  override val defaultValue: User = User(
    listOf(
      SettingsData(
        "default", "en",
        COLORS.PURPLE, THEMES.SYSTEM
      ),
      SettingsData(
        "default1", "ru",
        COLORS.RED, THEMES.DARK
      )
    )
  )

  override suspend fun readFrom(input: InputStream): User {
    return try {
      parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      Log.e("USER", exception.toString())
      defaultValue
    }
  }

  override suspend fun writeTo(
    t: User,
    output: OutputStream
  ) = t.writeTo(output)
}
