import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonObject

@Composable
fun CDropDownMenu(
    filter1Name: String,
    current1: String?,
    filter1Content: List<JsonObject>,
    transFormer:(JsonObject) -> String,
    setFilter1: (JsonObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    var display by remember { mutableStateOf(false) }
    Box(modifier.height(50.dp).width(120.dp), Alignment.Center) {
        OutlinedButton(
            { display = !display }, Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    filter1Name,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                if (current1 != null) Text(
                    current1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Thin
                )
            }
        }
        DropdownMenu(expanded = display, onDismissRequest = { display = false }) {
            for (item in filter1Content) DropdownMenuItem(onClick = { display = false;setFilter1(item) }) {
                Text(text = transFormer(item), softWrap = true, overflow = TextOverflow.Clip)
            }
        }
    }
}