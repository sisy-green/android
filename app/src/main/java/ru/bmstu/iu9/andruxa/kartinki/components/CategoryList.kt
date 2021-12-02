package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.bmstu.iu9.andruxa.kartinki.MainViewModel
import ru.bmstu.iu9.andruxa.kartinki.R

@Composable
fun CategoryList(navController: NavController, viewModel: MainViewModel) {
  val categories = remember { viewModel.getCategories() }
  SwipeRefresh(
    state = rememberSwipeRefreshState(viewModel.imagesRefreshing.value),
    onRefresh = { viewModel.updateCategories() },
  ) {
    NetworkError(viewModel.categoriesError) {
      Loading(categories.size == 0) {
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          items(
            items = categories,
            key = { item -> item.id },
            itemContent = {
              Text(
                text = it.name,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                  .clickable {
                    navController.navigate("category/${it.id}?name=${it.name}&current=categories")
                  }
                  .padding(start = dimensionResource(R.dimen.padding_big))
                  .fillMaxWidth()
                  .padding(vertical = dimensionResource(R.dimen.padding_medium)),
              )
            },
          )
        }
      }
    }
  }
}