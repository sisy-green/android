package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.bmstu.iu9.andruxa.kartinki.MainViewModel

@Composable
fun CategoryList(navController: NavController, viewModel: MainViewModel) {
  val categories = remember { viewModel.getCategories() }
  val categoriesError = remember { viewModel.categoriesError }
  Scaffold(
    topBar = { TopBar(mutableStateOf("Categories"), navController) },
    bottomBar = { BottomBar(navController = navController, current = "categories") }
  )
  {
    if (categoriesError.value) {
      NetworkError()
    } else {
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
                  navController.navigate("category/${it.id}?name=${it.name}&current=categories")
                },
            )
          },
        )
      }
    }
  }
}