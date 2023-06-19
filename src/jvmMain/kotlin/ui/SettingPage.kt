
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utils.conf.Conf
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
fun SettingPage() {
    var savePath by remember { mutableStateOf("") } // use a mutable state variable

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = savePath,
            onValueChange = { savePath = it }, // update the mutable state variable
            label = { Text(text = "保存路径") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = { savePath = chooseDirectory() }) { // update the mutable state variable
            Text(text = "选择路径")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = { saveToFile(savePath) }) {
            Text(text = "保存")
        }
    }
}

private fun saveToFile(savePath: String) {
    val conf =utils.conf.Conf
    if (savePath.isNotEmpty()) {
        conf.setConf("savepath",savePath)
    }
}

private fun chooseDirectory(): String {
    val chooser = JFileChooser()
    chooser.currentDirectory = File(Conf.getConf("savepath")).let { if (it.exists()) it else File(".") }
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val result = chooser.showOpenDialog(null)

    return if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else {
        ""
    }
}