package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TopBar(caption: MutableState<String>, navController: NavController, isInner: Boolean = false) {
  TopAppBar {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (isInner) {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(
              Icons.Default.ArrowBack,
              contentDescription = "back",
            )
          }
        }
        Text(
          text = caption.value,
          style = MaterialTheme.typography.h5,
          modifier = Modifier.padding(start = 20.dp),
        )
      }
      IconButton(
        onClick = { navController.navigate("search") },
        modifier = Modifier
      ) {
        Icon(
          Icons.Default.Search,
          contentDescription = "search",
        )
      }
    }
  }
}

data class NavItem(
  val name: String,
  val icon: ImageVector,
)

@Composable
fun BottomBar(navController: NavController, current: String) {
  val navItems = listOf(
    NavItem("categories", Icons.Default.Menu),
    NavItem("home", Icons.Default.Home),
    NavItem("settings", Icons.Default.Settings),
  )
  BottomAppBar {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      for (item in navItems) {
        IconButton(
          onClick = { navController.navigate(item.name) },
        ) {
          Icon(
            item.icon,
            contentDescription = item.name,
            tint = LocalContentColor.current.copy(alpha =
            if (current == item.name)
              LocalContentAlpha.current
            else
              ContentAlpha.disabled
            ),
          )
        }
      }
    }
  }
}