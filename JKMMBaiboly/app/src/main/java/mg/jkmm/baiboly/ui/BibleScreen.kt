package mg.jkmm.baiboly.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mg.jkmm.baiboly.data.Book

@Composable
fun BibleScreen(vm: BibleViewModel, onOpen: (Int, Int, Int, Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var tst by rememberSaveable { mutableIntStateOf(0) }
    var filter by rememberSaveable { mutableStateOf("") }
    var sheetBook by remember { mutableStateOf<Book?>(null) }

    val f = filter.trim().lowercase()
    val shown = vm.books.filter { b ->
        (f.isNotEmpty() || b.testament == if (tst == 0) "AT" else "NT") && b.name.lowercase().contains(f)
    }

    Column(Modifier.padding(horizontal = 18.dp)) {
        Text(
            "Baiboly", color = cs.primary, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold, fontSize = 26.sp,
            modifier = Modifier.padding(top = 18.dp, bottom = 14.dp),
        )
        Segmented(listOf("Testamenta Taloha", "Testamenta Vaovao"), tst, { tst = it })
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            placeholder = { Text("Hitady boky…") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (shown.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("Tsy nahita boky.", color = cs.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                }
            }
            items(shown, key = { it.id }) { book ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cs.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().clickable { sheetBook = book },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(book.name, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = cs.onSurface)
                        Text("${book.chapters} toko", color = cs.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    sheetBook?.let { book ->
        ChapterSheet(
            book = book,
            verseCount = { c -> withContext(Dispatchers.IO) { vm.repo.verseCount(book.id, c) } },
            onPick = { c, f, t -> sheetBook = null; onOpen(book.id, c, f, t) },
            onDismiss = { sheetBook = null },
        )
    }
}
