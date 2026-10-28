package mg.jkmm.baiboly.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mg.jkmm.baiboly.data.Verse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(vm: BibleViewModel, bookId: Int, chapter: Int, from: Int, to: Int, onBack: () -> Unit) {
    val book = vm.books.firstOrNull { it.id == bookId } ?: return
    val cs = MaterialTheme.colorScheme
    val pager = rememberPagerState(initialPage = (chapter - 1).coerceIn(0, book.chapters - 1)) { book.chapters }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var sheet by remember { mutableIntStateOf(0) } // 0 = rien, 1 = chapitres, 2 = réglages du texte
    val current = pager.currentPage + 1
    var rangeSel by remember { mutableStateOf(if (from > 0) RangeSel(chapter, from, maxOf(from, to)) else null) }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(pager.currentPage) {
        selected = emptySet()
        val r = rangeSel?.takeIf { it.chapter == current }
        vm.addHistory(bookId, current, r?.from ?: 0, r?.to ?: 0)
    }
    BackHandler(enabled = selected.isNotEmpty()) { selected = emptySet() }

    /** Construit « “versets” — Livre chapitre:numéros » pour la sélection en cours. */
    fun withSelection(action: (String) -> Unit) {
        scope.launch {
            val verses = withContext(Dispatchers.IO) { vm.repo.verses(bookId, current) }
            val chosen = verses.filter { it.number in selected && !it.missing }
            val body = chosen.joinToString(" ") { it.text }
            val ref = "${book.name} $current:" + chosen.joinToString(", ") { "${it.number}" }
            action("“$body”\n— $ref")
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Hiverina", tint = cs.primary)
                }
                Text(
                    book.name, color = cs.primary, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { sheet = 2 }) {
                    Text("Aa", color = cs.primary, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                ChapterPage(
                    vm = vm,
                    bookId = bookId,
                    chapter = page + 1,
                    range = rangeSel?.takeIf { it.chapter == page + 1 }?.let { it.from..it.to },
                    onClearRange = { rangeSel = null },
                    selected = if (page == pager.currentPage) selected else emptySet(),
                    onToggle = { n -> selected = if (n in selected) selected - n else selected + n },
                )
            }
        }

        // Barre flottante : navigation entre chapitres, ou actions si des versets sont choisis
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
            shape = RoundedCornerShape(30.dp),
            color = cs.surface,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, cs.outlineVariant),
        ) {
            Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                if (selected.isEmpty()) {
                    PillButton("‹", enabled = pager.currentPage > 0) {
                        scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
                    }
                    PillDivider()
                    PillButton("${book.name} $current") { sheet = 1 }
                    PillDivider()
                    PillButton("›", enabled = pager.currentPage < book.chapters - 1) {
                        scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                } else {
                    PillButton("Adikao") {
                        withSelection {
                            clipboard.setText(AnnotatedString(it))
                            Toast.makeText(context, "Voadika ✓", Toast.LENGTH_SHORT).show()
                            selected = emptySet()
                        }
                    }
                    PillDivider()
                    PillButton("Zarao") {
                        withSelection { text ->
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$text\n(Baiboly JKMM)")
                            }
                            context.startActivity(Intent.createChooser(send, "Zarao"))
                        }
                    }
                    PillDivider()
                    PillButton("Hasongadina") {
                        vm.toggleHighlights(bookId, current, selected)
                        selected = emptySet()
                    }
                    PillDivider()
                    PillButton("✕") { selected = emptySet() }
                }
            }
        }
    }

    if (sheet == 1) {
        ChapterSheet(
            book = book,
            verseCount = { c -> withContext(Dispatchers.IO) { vm.repo.verseCount(bookId, c) } },
            onPick = { c, f, t ->
                sheet = 0
                rangeSel = if (f > 0) RangeSel(c, f, maxOf(f, t)) else null
                if (f > 0) vm.addHistory(bookId, c, f, maxOf(f, t))
                scope.launch { pager.scrollToPage(c - 1) }
            },
            onDismiss = { sheet = 0 },
        )
    }
    if (sheet == 2) {
        ModalBottomSheet(onDismissRequest = { sheet = 0 }, containerColor = cs.background) {
            Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
                FontCard(vm)
                Box(Modifier.height(12.dp))
                FamilyCard(vm)
                Box(Modifier.height(12.dp))
                ThemeCard(vm)
            }
        }
    }
}

@Composable
private fun PillButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled) {
        Text(text, color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.25f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun PillDivider() {
    Box(Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))
}

@Composable
private fun ChapterPage(
    vm: BibleViewModel,
    bookId: Int,
    chapter: Int,
    range: IntRange?,
    onClearRange: () -> Unit,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val verses by produceState<List<Verse>?>(null, bookId, chapter) {
        value = withContext(Dispatchers.IO) { vm.repo.verses(bookId, chapter) }
    }
    val list = verses
    // Quand on ouvre une plage (ex. 1–5), on défile jusqu'au premier andininy
    LaunchedEffect(range, list) {
        if (range != null && list != null) listState.scrollToItem(range.first)
    }
    if (list == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 120.dp),
    ) {
        item {
            Row(Modifier.padding(top = 6.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TOKO $chapter", color = cs.tertiary, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, letterSpacing = 0.6.sp,
                )
                if (range != null) {
                    Surface(
                        onClick = onClearRange,
                        shape = RoundedCornerShape(12.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier.padding(start = 10.dp),
                    ) {
                        Text(
                            "Andininy ${range.first}" + (if (range.last > range.first) "–${range.last}" else "") + " ✕",
                            color = cs.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
        items(list, key = { it.number }) { v ->
            VerseText(
                v = v,
                fontSize = vm.fontSize,
                family = vm.verseFont(),
                lineFactor = vm.lineFactor(),
                justify = vm.justify,
                isSelected = v.number in selected,
                isInRange = range != null && v.number in range,
                isHighlighted = "$bookId|$chapter|${v.number}" in vm.highlights,
                onClick = { onToggle(v.number) },
            )
        }
    }
}

@Composable
private fun VerseText(
    v: Verse,
    fontSize: Float,
    family: FontFamily,
    lineFactor: Float,
    justify: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = cs.tertiary, fontWeight = FontWeight.Bold, fontSize = (fontSize * 0.7f).sp)) {
            append("${v.number}")
        }
        append("  ")
        if (v.missing) {
            withStyle(SpanStyle(color = cs.onSurfaceVariant, fontStyle = FontStyle.Italic)) { append("(andininy tsy misy)") }
        } else {
            append(v.text)
        }
    }
    val bg = when {
        isSelected -> cs.tertiary.copy(alpha = 0.26f)
        isInRange -> Gold.copy(alpha = 0.5f)
        isHighlighted -> Gold.copy(alpha = 0.32f)
        else -> Color.Transparent
    }
    Text(
        text,
        fontFamily = family,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * lineFactor).sp,
        textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
        color = cs.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 5.dp),
    )
}

/** Plage d'andininy à mettre en évidence dans un toko (ex. Jaona 3:1-5). */
private data class RangeSel(val chapter: Int, val from: Int, val to: Int)
