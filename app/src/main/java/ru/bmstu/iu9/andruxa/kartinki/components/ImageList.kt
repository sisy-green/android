package ru.bmstu.iu9.andruxa.kartinki.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.bmstu.iu9.andruxa.kartinki.ImagesSearchParams
import ru.bmstu.iu9.andruxa.kartinki.MainViewModel

@Composable
fun ImageList(
  navController: NavController,
  viewModel: MainViewModel,
  categoryID: String? = null,
  sort: String = "popular",
  query: String? = "",
) {
  val images = remember { viewModel.searchImages(ImagesSearchParams(categoryID, sort, query)) }
  SwipeRefresh(
    state = rememberSwipeRefreshState(viewModel.imagesRefreshing.value),
    onRefresh = { viewModel.updateImages(ImagesSearchParams(categoryID, sort, query)) },
  ) {
    NetworkError(viewModel.imagesError) {
      Loading(images.isEmpty()) {
        val state = rememberLazyListState()
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          state = state,
        ) {
          items(
            items = images,
            key = { item -> item.id },
            itemContent = {
              Image(
                painter = rememberImagePainter(it.asset, builder = { size(OriginalSize) }),
                contentDescription = it.description,
                modifier = Modifier
                  .fillMaxWidth()
                  .wrapContentHeight()
                  .clickable { navController.navigate("image/${it.id}") },
                contentScale = ContentScale.FillWidth,
              )
            },
          )
        }
      }
    }
  }
}