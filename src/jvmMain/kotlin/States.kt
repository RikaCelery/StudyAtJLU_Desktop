import androidx.compose.runtime.*

object States {
    var pageState by mutableStateOf(PageState.INDEX)

    var lessonFilter by mutableStateOf("Hello, World!")
    var lessons = mutableStateListOf("Hello, World!")
    var termFilter by mutableStateOf("Hello, World!")
    var terms = mutableStateListOf("Hello, World!")
    var cookie by mutableStateOf("Hello, World!")
}