package mg.jkmm.baiboly.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Accès à la base de la Bible.
 * Au premier lancement, la base fournie dans l'APK (assets) est copiée dans le stockage
 * de l'application. Une mise à jour à distance remplace simplement ce fichier local.
 */
class BibleRepository(private val context: Context) {

    private val dbFile = File(context.filesDir, "baiboly.db")
    private var db: SQLiteDatabase? = null

    @Synchronized
    private fun database(): SQLiteDatabase {
        db?.takeIf { it.isOpen }?.let { return it }
        if (!dbFile.exists()) {
            context.assets.open("baiboly_mg1865.db").use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            .also { db = it }
    }

    fun books(): List<Book> {
        val list = mutableListOf<Book>()
        database().rawQuery("SELECT id, nom, testament, nb_chapitres FROM livres ORDER BY id", null)
            .use { c ->
                while (c.moveToNext()) {
                    list += Book(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3))
                }
            }
        return list
    }

    /** Versets d'un chapitre. Les numéros absents de l'édition 1865 sont signalés (missing). */
    fun verses(bookId: Int, chapter: Int): List<Verse> {
        val found = linkedMapOf<Int, String>()
        database().rawQuery(
            "SELECT verset, texte FROM versets WHERE livre_id = ? AND chapitre = ? ORDER BY verset",
            arrayOf(bookId.toString(), chapter.toString()),
        ).use { c ->
            while (c.moveToNext()) found[c.getInt(0)] = c.getString(1)
        }
        val max = found.keys.maxOrNull() ?: return emptyList()
        return (1..max).map { n ->
            found[n]?.let { Verse(n, it) } ?: Verse(n, "", missing = true)
        }
    }

    /** Nombre d'andininy (versets) d'un chapitre. */
    fun verseCount(bookId: Int, chapter: Int): Int =
        database().rawQuery(
            "SELECT COALESCE(MAX(verset), 0) FROM versets WHERE livre_id = ? AND chapitre = ?",
            arrayOf(bookId.toString(), chapter.toString()),
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    fun verse(bookId: Int, chapter: Int, verse: Int): String? =
        database().rawQuery(
            "SELECT texte FROM versets WHERE livre_id = ? AND chapitre = ? AND verset = ?",
            arrayOf(bookId.toString(), chapter.toString(), verse.toString()),
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null }

    fun search(query: String): List<SearchHit> {
        val list = mutableListOf<SearchHit>()
        database().rawQuery(
            """SELECT v.livre_id, l.nom, v.chapitre, v.verset, v.texte
               FROM versets v JOIN livres l ON l.id = v.livre_id
               WHERE v.texte LIKE ?
               ORDER BY v.livre_id, v.chapitre, v.verset LIMIT 150""",
            arrayOf("%$query%"),
        ).use { c ->
            while (c.moveToNext()) {
                list += SearchHit(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getString(4))
            }
        }
        return list
    }

    fun contentVersion(): Int =
        database().rawQuery("SELECT valeur FROM meta WHERE cle = 'version_contenu'", null)
            .use { c -> if (c.moveToFirst()) c.getString(0).toIntOrNull() ?: 1 else 1 }

    /** Vérifie qu'un fichier téléchargé est bien une base de Bible complète. */
    fun isValid(file: File): Boolean = try {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { d ->
            d.rawQuery("SELECT COUNT(*) FROM versets", null).use { c ->
                c.moveToFirst() && c.getInt(0) > 30000
            }
        }
    } catch (e: Exception) {
        false
    }

    /** Remplace la base locale (avec sauvegarde, restaurée en cas d'échec). */
    @Synchronized
    fun replaceDatabase(newFile: File) {
        db?.close()
        db = null
        val backup = File(context.filesDir, "baiboly.db.bak")
        if (dbFile.exists()) dbFile.copyTo(backup, overwrite = true)
        try {
            newFile.copyTo(dbFile, overwrite = true)
        } catch (e: Exception) {
            if (backup.exists()) backup.copyTo(dbFile, overwrite = true)
            throw e
        }
    }
}
