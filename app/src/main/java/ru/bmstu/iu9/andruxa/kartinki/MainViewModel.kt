package ru.bmstu.iu9.andruxa.kartinki

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Image(
  val id: String,
  val asset: String,
  val description: String,
)
@Serializable
data class Url(
  val url: String,
)
@Serializable
data class Asset(
  val preview: Url,
)
@Serializable
data class ImageMock(
  val id: String,
   val assets: Asset,
  val description: String,
)

class MainViewModel : ViewModel() {
  val images = mutableStateListOf<Image>()
//  val categories = mutableStateListOf<Category>()
  val apiService = ApiService.getInstance()

//  init {
//    viewModelScope.launch {
//      getMock()
//      val apiService = ApiService.getInstance()
//      categories.addAll(0, apiService.getCategories().data)
//    }
//  }

//  fun getMock() {
//    val result = fun(): String {
//      return "[{\"id\":\"26\",\"name\":\"Abstract\"},{\"id\":\"1\",\"name\":\"Animals/Wildlife\"},{\"id\":\"11\",\"name\":\"The Arts\"},{\"id\":\"3\",\"name\":\"Backgrounds/Textures\"},{\"id\":\"27\",\"name\":\"Beauty/Fashion\"},{\"id\":\"2\",\"name\":\"Buildings/Landmarks\"},{\"id\":\"4\",\"name\":\"Business/Finance\"},{\"id\":\"5\",\"name\":\"Education\"},{\"id\":\"6\",\"name\":\"Food and Drink\"},{\"id\":\"7\",\"name\":\"Healthcare/Medical\"},{\"id\":\"8\",\"name\":\"Holidays\"},{\"id\":\"10\",\"name\":\"Industrial\"},{\"id\":\"21\",\"name\":\"Interiors\"},{\"id\":\"22\",\"name\":\"Miscellaneous\"},{\"id\":\"12\",\"name\":\"Nature\"},{\"id\":\"9\",\"name\":\"Objects\"},{\"id\":\"25\",\"name\":\"Parks/Outdoor\"},{\"id\":\"13\",\"name\":\"People\"},{\"id\":\"14\",\"name\":\"Religion\"},{\"id\":\"15\",\"name\":\"Science\"},{\"id\":\"17\",\"name\":\"Signs/Symbols\"},{\"id\":\"18\",\"name\":\"Sports/Recreation\"},{\"id\":\"16\",\"name\":\"Technology\"},{\"id\":\"0\",\"name\":\"Transportation\"},{\"id\":\"24\",\"name\":\"Vintage\"}]"
//    }()
//    categories.clear()
//    categories.addAll(Json { ignoreUnknownKeys = true }.decodeFromString<List<Category>>(result))
//  }

  private fun searchMock() {
//    viewModelScope.launch {
      images.clear()
      val result = fun(): String {
        return "[{\"id\":\"1973821043\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/223270637/1973821043/stock-photo-fresh-summer-salad-with-prawn-strawberry-avocado-lime-and-olive-summer-salad-healthy-eating-1973821043.jpg\"}},\"description\":\"Fresh summer salad with prawn,strawberry,avocado,lime and olive.Summer salad,healthy eating\"},{\"id\":\"1966413823\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/228984501/1966413823/stock-photo-amazing-pink-sand-beach-in-budelli-island-maddalena-archipelago-sardinia-italy-1966413823.jpg\"}},\"description\":\"Amazing pink sand beach in Budelli Island, Maddalena Archipelago, Sardinia, Italy\"},{\"id\":\"1975069022\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/228986375/1975069022/stock-photo-solo-woman-drink-coffee-with-relax-and-wellbeing-feel-with-mountain-background-1975069022.jpg\"}},\"description\":\"solo woman drink coffee with relax and wellbeing feel with mountain background\"},{\"id\":\"1964873407\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/223265567/1964873407/stock-photo-male-legs-stacking-in-round-on-pastel-geometrical-background-modern-design-contemporary-art-1964873407.jpg\"}},\"description\":\"Male legs stacking in round on pastel geometrical background. Modern design, contemporary art collage. Inspiration, idea, trendy urban magazine style. Negative space to insert your text or ad\"},{\"id\":\"1972480916\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/224074085/1972480916/stock-photo-wedding-archway-with-flowers-arranged-for-a-wedding-ceremony-1972480916.jpg\"}},\"description\":\"Wedding archway with flowers arranged for a wedding ceremony\"},{\"id\":\"1974854414\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/301517351/1974854414/stock-vector-big-data-flow-technology-and-science-vector-background-tech-abstraction-with-lines-electronics-and-1974854414.jpg\"}},\"description\":\"Big data flow technology and science vector background, tech abstraction with lines electronics and digital style in 3D dimensional perspective, abstract illustration.\"},{\"id\":\"1986609188\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/301109095/1986609188/stock-vector-different-container-with-hazard-chemical-liquid-in-row-line-compressed-gas-and-oil-safety-tank-1986609188.jpg\"}},\"description\":\"Different container with hazard chemical liquid in row line. Compressed gas and oil safety tank with dangerous radioactive flammable substance vector illustration isolated on white background\"},{\"id\":\"1936186630\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/301109095/1936186630/stock-vector-floral-branch-hand-drawn-wedding-herb-plant-and-monogram-with-elegant-leaves-for-invitation-save-1936186630.jpg\"}},\"description\":\"Floral branch. Hand drawn wedding herb, plant and monogram with elegant leaves for invitation save the date card design. Botanical rustic trendy greenery vector\"},{\"id\":\"1971338468\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/223270637/1971338468/stock-photo-delicious-summer-tartlets-with-raspberries-and-yoghurt-yellow-and-red-raspberries-healthy-dessert-1971338468.jpg\"}},\"description\":\"Delicious summer tartlets with raspberries and yoghurt. Yellow and red raspberries. Healthy dessert. Keto dessert.\"},{\"id\":\"1973244086\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/223270637/1973244086/stock-photo-strawberry-desserts-in-cups-summer-snack-strawberry-yogurt-granola-1973244086.jpg\"}},\"description\":\"Strawberry desserts in cups. Summer snack strawberry yogurt granola.\"},{\"id\":\"1965801202\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/228984501/1965801202/stock-photo-blue-sky-autumn-landscape-with-bright-colorful-leaves-indian-summer-foliage-1965801202.jpg\"}},\"description\":\"Blue sky. autumn landscape with bright colorful leaves. Indian summer. foliage.\"},{\"id\":\"1947304768\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/228986375/1947304768/stock-photo-the-view-through-the-glass-of-dry-white-gypsophila-flowers-cinematic-tone-1947304768.jpg\"}},\"description\":\"The view through the glass of dry white gypsophila flowers, Cinematic tone.\"},{\"id\":\"1931260157\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/224074813/1931260157/stock-photo--d-illustration-palms-and-beach-with-chair-beach-umbrella-and-smartphone-on-sand-travel-and-1931260157.jpg\"}},\"description\":\"3d illustration. Palms and beach with chair, Beach umbrella and Smartphone on sand. Travel and Summer vacation concept.\"},{\"id\":\"1936655347\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/228984501/1936655347/stock-photo-indian-summer-beautiful-autumn-landscape-with-yellow-trees-and-sun-colorful-foliage-in-the-park-1936655347.jpg\"}},\"description\":\"Indian summer. Beautiful autumn landscape with yellow trees and sun. Colorful foliage in the park. Falling leaves natural background. \"},{\"id\":\"1938079048\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/301131357/1938079048/stock-photo-luxury-canadian-house-completely-renovated-furnished-and-staged-with-basement-deck-backyard-and-1938079048.jpg\"}},\"description\":\"Luxury Canadian House Completely Renovated, Furnished and Staged with Basement, Deck, Backyard and Garage for Sale\"},{\"id\":\"1931413580\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/301109095/1931413580/stock-vector-sale-tag-speech-bubble-red-shape-with-different-discount-set-1931413580.jpg\"}},\"description\":\"Sale tag speech bubble red shape with different discount set. 10, 20, 25, 30, 35, 40, 50, 60, 70, 80 and 90 percent price clearance sticker badge banner label vector illustration isolated on white\"},{\"id\":\"1931260166\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/224074813/1931260166/stock-photo--d-illustration-beach-umbrella-travel-suitcase-beach-ball-palms-and-smartphone-travel-and-1931260166.jpg\"}},\"description\":\"3d illustration. Beach umbrella, Travel suitcase, beach ball, palms and Smartphone. Travel and Summer vacation concept.\"},{\"id\":\"1913012410\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/224074813/1913012410/stock-photo--d-illustration-smartphone-with-credit-cards-on-the-side-online-shop-and-e-commerce-concept-1913012410.jpg\"}},\"description\":\"3D Illustration. Smartphone with credit cards on the side. Online shop and e-commerce concept.\"},{\"id\":\"1963795600\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/223820717/1963795600/stock-photo-smiling-dentist-communicating-with-african-american-woman-while-checking-her-teeth-during-dental-1963795600.jpg\"}},\"description\":\"Smiling dentist communicating with African American woman while checking her teeth during dental procedure at dentist's office.\"},{\"id\":\"1864631416\",\"assets\":{\"preview\":{\"url\":\"https://image.shutterstock.com/display_pic_with_logo/285585901/1864631416/stock-photo-portrait-of-a-healthy-and-happy-middle-aged-african-woman-living-with-hiv-1864631416.jpg\"}},\"description\":\"Portrait of a healthy and happy middle-aged African woman living with HIV\"}]"
      }()
      val imgs = Json { ignoreUnknownKeys = true }.decodeFromString<List<ImageMock>>(result)
      images.addAll(imgs.map { it ->
        Image(
          it.id,
          it.assets.preview.url,
          it.description,
        )
      })
//    }
  }

  fun search(categoryID: String? = null, query: String? = null) : List<Image>{
      viewModelScope.launch {
        try {
          val resp :ImagesSearchModel = apiService.searchImages(categoryID)
          println("HUITA0")
          images.clear()
          images.addAll(0, resp.data.map {
            Image(
              it.id,
              it.assets["preview"]!!.url,
              it.description,
            )}.distinctBy { it.id })}
        catch (e: Exception) {
          println("HUITA1")
          searchMock()
        }
  }
    return images

  }
}
@Serializable
data class Category(
  val id: String,
  val name: String
)

class CategoriesViewModel : ViewModel() {
  val categories = mutableStateListOf<Category>()

  init {
    viewModelScope.launch {
      getMock()
//      val apiService = ApiService.getInstance()
//      categories.addAll(0, apiService.getCategories().data)
    }
  }
  fun getMock(){
    val result = fun () :String { return "[{\"id\":\"26\",\"name\":\"Abstract\"},{\"id\":\"1\",\"name\":\"Animals/Wildlife\"},{\"id\":\"11\",\"name\":\"The Arts\"},{\"id\":\"3\",\"name\":\"Backgrounds/Textures\"},{\"id\":\"27\",\"name\":\"Beauty/Fashion\"},{\"id\":\"2\",\"name\":\"Buildings/Landmarks\"},{\"id\":\"4\",\"name\":\"Business/Finance\"},{\"id\":\"5\",\"name\":\"Education\"},{\"id\":\"6\",\"name\":\"Food and Drink\"},{\"id\":\"7\",\"name\":\"Healthcare/Medical\"},{\"id\":\"8\",\"name\":\"Holidays\"},{\"id\":\"10\",\"name\":\"Industrial\"},{\"id\":\"21\",\"name\":\"Interiors\"},{\"id\":\"22\",\"name\":\"Miscellaneous\"},{\"id\":\"12\",\"name\":\"Nature\"},{\"id\":\"9\",\"name\":\"Objects\"},{\"id\":\"25\",\"name\":\"Parks/Outdoor\"},{\"id\":\"13\",\"name\":\"People\"},{\"id\":\"14\",\"name\":\"Religion\"},{\"id\":\"15\",\"name\":\"Science\"},{\"id\":\"17\",\"name\":\"Signs/Symbols\"},{\"id\":\"18\",\"name\":\"Sports/Recreation\"},{\"id\":\"16\",\"name\":\"Technology\"},{\"id\":\"0\",\"name\":\"Transportation\"},{\"id\":\"24\",\"name\":\"Vintage\"}]"}()
//    categories.clear()
    println("HUITAGODA")
    categories.addAll( Json{ignoreUnknownKeys=true}.decodeFromString<List<Category>>(result))
  }
  }



