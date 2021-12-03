package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.bmstu.iu9.andruxa.kartinki.R

@ExperimentalAnimationApi
@Composable
fun TopBar(caption: MutableState<String>, navController: NavController, visible: Boolean) {
  val searchExpanded = remember { mutableStateOf(false) }
  var input by rememberSaveable { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  val backStackEntry = navController.currentBackStackEntryAsState()
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(expandFrom = Alignment.Top),
    exit = shrinkVertically(shrinkTowards = Alignment.Top),
  ) {
    TopAppBar {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val modifier = Modifier.focusRequester(focusRequester)
          TextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text(stringResource(R.string.search)) },
            modifier = if (searchExpanded.value) modifier.fillMaxWidth(0.9f) else modifier.width(0.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.textFieldColors(
              backgroundColor = Color.Transparent,
              cursorColor =
              if (MaterialTheme.colors.isLight) {
                MaterialTheme.colors.onPrimary
              } else {
                MaterialTheme.colors.primary
              },
              focusedIndicatorColor = (
                if (MaterialTheme.colors.isLight) {
                  MaterialTheme.colors.onPrimary
                } else {
                  MaterialTheme.colors.primary
                }
                ).copy(ContentAlpha.high),
              placeholderColor = (
                if (MaterialTheme.colors.isLight) {
                  MaterialTheme.colors.onPrimary
                } else {
                  MaterialTheme.colors.onSurface
                }
                ).copy(ContentAlpha.medium),
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
              navController.navigate("search/?name=${input}&current=search&query=${input}")
              input = ""
              searchExpanded.value = false
              focusManager.clearFocus()
            }),
          )
          input = input.replace("\n", "")
          if (!searchExpanded.value) {
            if (backStackEntry.value?.destination?.route?.contains('/') == true) {
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
              modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_big)),
            )
          }
        }
        IconButton(onClick = {
          searchExpanded.value = !searchExpanded.value
          if (searchExpanded.value) {
            focusRequester.requestFocus()
          } else {
            focusManager.clearFocus()
            input = ""
          }
        }) {
          Icon(
            if (searchExpanded.value) Icons.Default.Clear else Icons.Default.Search,
            contentDescription = "search",
          )
        }
      }
    }
  }
}

data class NavItem(
  val name: String,
  val icon: ImageVector,
  val activeStateCondition: () -> Boolean,
)

@ExperimentalAnimationApi
@Composable
fun BottomBar(navController: NavController, visible: Boolean) {
  val backStackEntry = navController.currentBackStackEntryAsState()
  val navItems = listOf(
    NavItem("categories", Icons.Default.Menu) {
      backStackEntry.value?.destination?.route == "categories" ||
        backStackEntry.value?.destination?.route?.startsWith("category/") == true
    },
    NavItem("home", Icons.Default.Home) {
      backStackEntry.value?.destination?.route == "home"
    },
    NavItem("settings", Icons.Default.Settings) {
      backStackEntry.value?.destination?.route == "settings"
    },
  )
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(),
    exit = shrinkVertically(),
  ) {
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
              tint = LocalContentColor.current.copy(
                alpha =
                if (item.activeStateCondition())
                  ContentAlpha.high
                else
                  ContentAlpha.disabled
              ),
            )
          }
        }
      }
    }
  }
}