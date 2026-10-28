package mg.jkmm.baiboly.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mg.jkmm.baiboly.data.Book
import mg.jkmm.baiboly.data.SearchHit

private fun marked(text: String, q: String): AnnotatedString = buildAnnotatedString {
    val i = text.indexOf(q, ignoreCase = true)
    if (i < 0) {
        append(text)
    } else {
        append(text.substring(0, i))
        withStyle(SpanStyle(background = Gold.copy(alpha = 0.4f))) { append(text.substring(i, i + q.length)) }
        append(text.substring(i + q.length))
    }
}

private data class Ref(val book: Book, val chapter: Int, val from: Int, val to: Int)

private val REF = Regex("""^(.+?)\s+(\d+)(?:\s*[:.,]\s*(\d+)(?:\s*[-–]\s*(\d+))?)?$""")

/** Reconnaît « Jaona 3 », « Jaona 3:16 » ou « Jaona 3:1-5 ». */
private fun parseRef(term: String, books: List<Book>): Ref? {
    val m = REF.find(term) ?: return null
    val name = m.groupValues[1].lowercase()
    val book = books.firstOrNull { it.name.lowercase() == name }
        ?: books.firstOrNull { it.name.lowercase().startsWith(name) }
        ?: return null
    val chapter = m.groupValues[2].toInt()
    if (chapter < 1 || chapter > book.chapters) return null
    val from = m.groupValues[3].toIntOrNull() ?: 0
    val to = m.groupValues[4].toIntOrNull() ?: from
    return Ref(book, chapter, from, maxOf(from, to))
}

@Composable
fun SearchScreen(vm: BibleViewModel, onOpen: (Int, Int, Int, Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var q by rememberSaveable { mutableStateOf("") }
    val term = q.trim()
    val ref = remember(term, vm.books) { parseRef(term, vm.books) }
    val results by produceState(emptyList<SearchHit>(), term) {
        value = if (term.length < 3) emptyList() else {
            delay(300) // attend que la personne finisse de taper
            withContext(Dispatchers.IO) { vm.repo.search(term) }
        }
    }

    Column(Modifier.padding(horizontal = 18.dp)) {
        Text(
            "Karohy", color = cs.primary, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold, fontSize = 26.sp,
            modifier = Modifier.padding(top = 18.dp, bottom = 14.dp),
        )
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text("Teny, na Jaona 3:1-5") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        ref?.let { r ->
            Surface(
                onClick = { onOpen(r.book.id, r.chapter, r.from, r.to) },
                shape = RoundedCornerShape(16.dp),
                color = cs.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                val label = "${r.book.name} ${r.chapter}" +
                    (if (r.from > 0) ":${r.from}" + (if (r.to > r.from) "–${r.to}" else "") else "")
                Text(
                    "Mankany amin’ny $label ›", color = cs.onPrimary,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(15.dp),
                )
            }
        }
        when {
            term.length < 3 -> Text(
                "Soraty eto ny teny tadiavina.\nOhatra : Andriamanitra · Jaona 3:1-5",
                color = cs.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
                modifier = Modifier.padding(vertical = 26.dp),
            )
            results.isEmpty() && ref != null -> Unit
            results.isEmpty() -> Text("Tsy nahita valiny.", color = cs.onSurfaceVariant, modifier = Modifier.padding(vertical = 26.dp))
            else -> Text(
                if (results.size >= 150) "Valiny 150 voalohany" else "${results.size} valiny",
                color = cs.onSurfaceVariant, fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(results, key = { "${it.bookId}-${it.chapter}-${it.verse}" }) { hit ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cs.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onOpen(hit.bookId, hit.chapter, hit.verse, hit.verse) },
                ) {
                    Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
                        Text("${hit.bookName} ${hit.chapter}:${hit.verse}", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            marked(hit.text, term),
                            fontFamily = FontFamily.Serif, fontSize = 15.5.sp, lineHeight = 23.sp,
                            maxLines = 3, overflow = TextOverflow.Ellipsis, color = cs.onSurface,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
