package ru.bmstu.iu9.andruxa.kartinki

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

val Context.userDataStore: DataStore<User> by dataStore(
  fileName = "user.proto",
  serializer = UserDataSerializer,
)

class UserRepo(
  private val settings: DataStore<User>,
) {
  suspend fun addUser(s: SettingsData) {
    settings.updateData { store -> User(store.users + s) }
  }

  suspend fun deleteUser(name: String) {
    settings.updateData { store -> User(store.users.filter { it.name !== name }) }
  }

  val saved = mutableStateListOf<SettingsData>()

  suspend fun editUser(s: SettingsData) {
    val users = getUsers()
    val edited = mutableListOf<SettingsData>()
    users.forEach { settingsData ->
      if (settingsData.name == s.name) {
        edited.add(s)
      } else {
        edited.add(settingsData)
      }
    }
    settings.updateData { User(edited) }
  }

  suspend fun getUsers(): List<SettingsData> {
    val result = mutableListOf<SettingsData>()
    settings.data.take(1).collect { users -> result.addAll(0, users.users) }
    return result.toList()
  }

  suspend fun initSaved() {
    saved.clear()
    saved.addAll(getUsers())
  }
}

@Serializable
data class User(
  var users: List<SettingsData>
) {
  @ExperimentalSerializationApi
  fun writeTo(output: OutputStream) {
    Json.encodeToStream(serializer(), this, output)
  }
}

@ExperimentalSerializationApi
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
        "Purple Sunshine",
        "en",
        COLORS.PURPLE,
        THEMES.SYSTEM,
      ),
      SettingsData(
        "?????????????? ??????????????",
        "ru",
        COLORS.RED,
        THEMES.DARK,
      )
    )
  )

  @ExperimentalSerializationApi
  override suspend fun readFrom(input: InputStream): User {
    return try {
      parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      defaultValue
    }
  }

  @ExperimentalSerializationApi
  override suspend fun writeTo(t: User, output: OutputStream) = t.writeTo(output)
}
