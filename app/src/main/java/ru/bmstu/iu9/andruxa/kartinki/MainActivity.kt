package ru.bmstu.iu9.andruxa.kartinki

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import ru.bmstu.iu9.andruxa.kartinki.ui.theme.KartinkiTheme
import java.util.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = MainViewModel()
    setContent {
      KartinkiTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          val navController = rememberNavController()
          NavHost(navController = navController, startDestination = "list") {
            composable("list") { MainList(navController, viewModel) }
            composable("image/{imageId}") { backStackEntry ->
              ImageViewer(backStackEntry.arguments?.getString("imageId"), viewModel)
            }
            composable("settings") { Settings(navController) }
          }
        }
      }
    }
  }
}

@Composable
fun MainList(navController: NavController, viewModel: MainViewModel) {
  val images = remember { viewModel.images }
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = {
        navController.navigate("settings")
      }) {
        Icon(Icons.Default.Menu, contentDescription = "settings")
      }
    }
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      items(
        items = images,
        key = { it.id },
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

@Composable
fun ImageViewer(id: String?, viewModel: MainViewModel) {
  id?.let {
    val image = remember { viewModel.images.find { it.id == id }}
    val context = LocalContext.current
    image?.let { image ->
      Scaffold(
        floatingActionButton = {
          FloatingActionButton(onClick = {
            val shareIntent: Intent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, image.asset)
              type = "image/*"
            }
            startActivity(context, Intent.createChooser(shareIntent, "Поделиться"), null)
          }) {
            Icon(Icons.Default.Share, contentDescription = "share")
          }
        }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(R.color.black)),
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
              .padding(start = 8.dp, top = 8.dp, end = 8.dp),
          )
        }
      }
    }
  }
}

@Composable
fun <K>SettingsItem(map: Map<K, String>, defaultKey: K, label: String) {
  var expanded by remember { mutableStateOf(false) }
  val items = remember { map.keys }
  var selectedItem: K by remember { mutableStateOf(defaultKey) }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { expanded = !expanded }
      .padding(vertical = 15.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.h6,
    )
    Text(
      text = map[selectedItem]!!,
      style = MaterialTheme.typography.subtitle1,
    )
  }
  if (expanded) {
    Dialog(onDismissRequest = { expanded = false }) {
      Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.End
        ) {
          val onClick: (K) -> Unit = { item ->
            selectedItem = item
            Timer().schedule(object : TimerTask() {
              override fun run() {
                expanded = false
              }
            }, 150)
          }
          items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = selectedItem == item, onClick = { onClick(item) })
              Text(
                text = map[item]!!,
                modifier = Modifier
                  .padding(start = 10.dp)
                  .fillMaxWidth()
                  .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                  ) { onClick(item) },
              )
            }
            Spacer(modifier = Modifier.height(20.dp))
          }
          Text("отмена", modifier = Modifier
            .wrapContentWidth()
            .clickable { expanded = false })
        }
      }
    }
  }
}

@Composable
fun Settings(navController: NavController) {
  Scaffold(
    topBar = {
      TopAppBar {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.ArrowBack,
            contentDescription = "back",
            modifier = Modifier.clickable(
              interactionSource = MutableInteractionSource(),
              indication = null,
            ) { navController.popBackStack() },
          )
          Text(
            text = "Настройки",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start = 20.dp),
          )
        }
      }
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Text(
        text = "Профиль",
        style = MaterialTheme.typography.subtitle1,
      )
      SettingsItem(map = LANGUGAGES, defaultKey = "ru", label = "\${имя профиля}")
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        text = "Настройки",
        style = MaterialTheme.typography.subtitle1,
      )
      SettingsItem(map = LANGUGAGES, defaultKey = "ru", label = "язык")
      Divider()
      SettingsItem(map = COLORS_MAP, defaultKey = COLORS.VIOLET, label = "цвет")
      Divider()
      SettingsItem(map = THEMES_NAMES_MAP, defaultKey = THEMES.SYSTEM, label = "тема")
    }
  }
}

@Composable
@Preview
fun SettingsPreview() {
  KartinkiTheme {
    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
      Settings(rememberNavController())
    }
  }
}
