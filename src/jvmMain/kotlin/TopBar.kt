import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import utils.String

@Composable
@Suppress("FunctionName")
fun TopBar(
    cookieString: String,
    setCookieString: (String) -> Unit,
    filter1Name: String,
    filter1Content: List<JsonObject>,
    setFilter1: (JsonObject) -> Unit,
    current1: String? = null,
    filter2Name: String,
    filter2Content: List<JsonObject>,
    setFilter2: (JsonObject) -> Unit,
    current2: String? = null,
    syncState: SyncState,
    onSync: () -> Unit,
    fnIcon: ImageVector,
    fnDescription: String,
    onFnClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextField(
            cookieString, setCookieString, Modifier.weight(1f, true).height(50.dp), visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.width(2.dp))
        IconButton(onClick = onSync) {
            val rotationState = remember { Animatable(0f) }
            LaunchedEffect(syncState) {
                if (syncState == SyncState.SYNCING)
                    rotationState.animateTo(
                        targetValue = rotationState.value - 360f, animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, delayMillis = 200),
                            repeatMode = RepeatMode.Restart
                        )
                    )
            }
            when (syncState) {
                is SyncState.FAILED -> Icon(painterResource("sync_problem.svg"), "refresh list")
                SyncState.OUT_DATE -> Icon(painterResource("sync.svg"), "refresh list")
                SyncState.SYNCED -> Icon(painterResource("done_outline.svg"), "refresh list")
                SyncState.SYNCING -> {
                    Icon(painterResource("sync.svg"), "refresh list", Modifier.rotate(rotationState.value))
                }
            }
        }
        Spacer(Modifier.width(2.dp))
        CDropDownMenu(filter1Name, current1, filter1Content, {
            buildString {
                append(it.String("courseName"))
                append(":")
                append(it.String("teacherName"))
            }
        }, setFilter1, Modifier)
        Spacer(Modifier.width(2.dp))
        CDropDownMenu(filter2Name, current2, filter2Content, {
            buildString {
                append(it.String("year"))
                append(it.String("name"))
            }
        },setFilter2, Modifier)
        Spacer(Modifier.width(2.dp))
        OutlinedButton(
            onFnClick, Modifier.size(50.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)
        ) {
            Icon(fnIcon, fnDescription)
        }

    }
}