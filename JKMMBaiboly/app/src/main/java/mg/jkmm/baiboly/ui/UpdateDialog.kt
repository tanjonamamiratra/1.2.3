package mg.jkmm.baiboly.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Fenêtre « Version vaovao misy » : télécharge puis lance l'installation du nouvel APK. */
@Composable
fun UpdateDialog(vm: BibleViewModel) {
    val update = vm.appUpdate ?: return
    if (!vm.updateDialogOpen) return
    val busy = vm.downloadProgress >= 0f

    AlertDialog(
        onDismissRequest = { if (!busy) vm.dismissUpdateDialog() },
        title = { Text("Version vaovao : ${update.versionName}") },
        text = {
            Column {
                if (update.notes.isNotBlank()) Text(update.notes, fontSize = 14.sp)
                if (busy) {
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(progress = { vm.downloadProgress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text("${(vm.downloadProgress * 100).toInt()} %", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (vm.appUpdateStatus.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(vm.appUpdateStatus, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.downloadAndInstall() }, enabled = !busy) { Text("Hanavao") } },
        dismissButton = { TextButton(onClick = { vm.dismissUpdateDialog() }, enabled = !busy) { Text("Tsy izao") } },
    )
}
