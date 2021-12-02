package ru.bmstu.iu9.andruxa.kartinki.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import ru.bmstu.iu9.andruxa.kartinki.ImageSaver
import ru.bmstu.iu9.andruxa.kartinki.MainViewModel
import ru.bmstu.iu9.andruxa.kartinki.R

@Composable
fun ImageViewer(id: String?, viewModel: MainViewModel) {
  id?.let {
    val image  = rememberSaveable(stateSaver = ImageSaver) { mutableStateOf( viewModel.images.find{ item -> item.id ==id}!!) }.value
    val context = LocalContext.current
    image.let { image ->
      Scaffold(
        floatingActionButton = {
          val shareResource = stringResource(R.string.share)
          FloatingActionButton(onClick = {
            val shareIntent: Intent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, image.asset)
              type = "image/*"
            }
            ContextCompat.startActivity(
              context,
              Intent.createChooser(shareIntent, shareResource),
              null
            )
          }) {
            Icon(Icons.Default.Share, contentDescription = "share")
          }
        }
      ) {
        Surface(color = Color.Black) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
          ) {
            Image(
              painter = rememberImagePainter(image.asset, builder = { size(OriginalSize) }),
              contentDescription = image.description,
              modifier = Modifier.fillMaxWidth(),
              contentScale = ContentScale.FillWidth,
            )
            Text(
              text = image.description,
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_small))
                .padding(horizontal = dimensionResource(R.dimen.padding_small)),
            )
          }
        }
      }
    }
  }
}