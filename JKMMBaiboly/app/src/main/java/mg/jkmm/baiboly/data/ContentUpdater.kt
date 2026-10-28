package mg.jkmm.baiboly.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed class UpdateResult {
    object UpToDate : UpdateResult()
    data class Updated(val version: Int) : UpdateResult()
    data class Failed(val reason: String) : UpdateResult()
}

/**
 * Mise à jour du CONTENU de la Bible sans republier l'APK.
 *
 * Le serveur (Firebase Hosting, gratuit) héberge deux fichiers :
 *   manifest.json  -> {"version": 2, "file": "baiboly_mg1865_v2.db", "sha256": "..."}
 *   baiboly_mg1865_v2.db
 * L'application compare "version" avec celle de sa base locale et télécharge si c'est plus récent.
 */
object ContentUpdater {

    // Adresse du serveur de mise à jour (Netlify). Doit se terminer par "/".
    // manifest.json doit s'afficher à : BASE_URL + "manifest.json"
    const val BASE_URL = "https://splendorous-raindrop-bb4786.netlify.app/"

    suspend fun check(context: Context, repo: BibleRepository): UpdateResult =
        withContext(Dispatchers.IO) {
            if (BASE_URL.contains("VOTRE-SITE")) {
                return@withContext UpdateResult.Failed("Serveur non configuré")
            }
            try {
                val manifest = JSONObject(fetchText(BASE_URL + "manifest.json"))
                val remote = manifest.getInt("version")
                if (remote <= repo.contentVersion()) return@withContext UpdateResult.UpToDate

                val tmp = File(context.cacheDir, "update.db")
                download(BASE_URL + manifest.getString("file"), tmp)

                val expected = manifest.optString("sha256")
                if (expected.isNotBlank() && !sha256(tmp).equals(expected, ignoreCase = true)) {
                    tmp.delete()
                    return@withContext UpdateResult.Failed("Fichier corrompu")
                }
                if (!repo.isValid(tmp)) {
                    tmp.delete()
                    return@withContext UpdateResult.Failed("Base invalide")
                }
                repo.replaceDatabase(tmp)
                tmp.delete()
                UpdateResult.Updated(remote)
            } catch (e: Exception) {
                UpdateResult.Failed(e.message ?: "Erreur réseau")
            }
        }

    internal fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
        }

    internal fun fetchText(url: String): String {
        val conn = open(url)
        try {
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun download(url: String, target: File) {
        val conn = open(url)
        try {
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            conn.inputStream.use { input -> target.outputStream().use { input.copyTo(it) } }
        } finally {
            conn.disconnect()
        }
    }

    internal fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
