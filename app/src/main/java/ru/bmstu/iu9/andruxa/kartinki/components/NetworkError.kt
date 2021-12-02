package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.bmstu.iu9.andruxa.kartinki.R

@Composable
fun NetworkError(isError: MutableState<Boolean>, content: @Composable () -> Unit) {
  if (isError.value) {
    Column(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(Icons.Default.Warning, contentDescription = "network error")
      Text(
        text = stringResource(R.string.network_error),
        style = MaterialTheme.typography.body1,
      )
    }
  } else {
    content()
  }
}