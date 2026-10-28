package mg.jkmm.baiboly.data

data class Book(val id: Int, val name: String, val testament: String, val chapters: Int)

data class Verse(val number: Int, val text: String, val missing: Boolean = false)

data class SearchHit(
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
)

data class VerseOfDay(
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val reference: String,
    val text: String,
)

/** Entrée de l'historique : un chapitre entier (from = 0) ou une plage d'andininy, ex. Matio 2:2-4. */
data class HistoryItem(val book: Int, val chapter: Int, val from: Int = 0, val to: Int = 0)
