package ru.bmstu.iu9.andruxa.kartinki

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class LocaleChangeBroadcastReciever(private val activity: MainActivity): BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (Locale.getDefault().language in LANGUAGE_CODES) {
      CoroutineScope(Dispatchers.IO).launch {
        activity.dataStore.edit { settings ->
          settings[stringPreferencesKey("lang")] = Locale.getDefault().language
        }
      }
    }
  }
}