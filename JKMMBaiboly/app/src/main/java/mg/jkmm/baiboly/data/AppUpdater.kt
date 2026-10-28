package mg.jkmm.baiboly.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class AppUpdate(
    val versionCode: Long,
    val versionName: String,
    val file: String,
    val sha256: String,
    val notes: String,
)

/**
 * Mise à jour de l'APPLICATION elle-même, hors Play Store (gratuit).
 *
 * manifest.json contient, en plus de la partie « Bible », un bloc :
 *   "app": {"versionCode": 6, "versionName": "1.3.0", "file": "Baiboly-JKMM-1.3.0.apk",
 *           "sha256": "...", "notes": "Nouveautés..."}
 * Si versionCode est plus grand que celui installé, l'application propose de télécharger
 * le nouvel APK puis ouvre l'installateur d'Android (la personne confirme).
 * IMPORTANT : tous les APK doivent être signés avec la MÊME clé, sinon Android refuse.
 */
object AppUpdater {

    suspend fun check(currentVersionCode: Long): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val manifest = JSONObject(ContentUpdater.fetchText(ContentUpdater.BASE_URL + "manifest.json"))
            val app = manifest.optJSONObject("app") ?: return@withContext null
            val code = app.getLong("versionCode")
            if (code <= currentVersionCode) return@withContext null
            AppUpdate(
                versionCode = code,
                versionName = app.optString("versionName"),
                file = app.getString("file"),
                sha256 = app.optString("sha256"),
                notes = app.optString("notes"),
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun download(context: Context, update: AppUpdate, onProgress: (Float) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                val target = File(dir, "update.apk").also { it.delete() }
                val url = if (update.file.startsWith("http")) update.file else ContentUpdater.BASE_URL + update.file
                val conn = ContentUpdater.open(url)
                try {
                    if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
                    val total = conn.contentLength.toLong()
                    conn.inputStream.use { input ->
                        target.outputStream().use { out ->
                            val buf = ByteArray(16 * 1024)
                            var read = 0L
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                read += n
                                if (total > 0) onProgress(read.toFloat() / total)
                            }
                        }
                    }
                } finally {
                    conn.disconnect()
                }
                if (update.sha256.isNotBlank() && !ContentUpdater.sha256(target).equals(update.sha256, ignoreCase = true)) {
                    target.delete()
                    error("Fichier corrompu")
                }
                target
            }
        }

    /** Android demande d'autoriser l'installation depuis l'application (une seule fois). */
    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    fun openInstallSettings(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
