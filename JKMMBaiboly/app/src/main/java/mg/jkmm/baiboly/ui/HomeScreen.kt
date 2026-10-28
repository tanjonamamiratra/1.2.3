package mg.jkmm.baiboly.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mg.jkmm.baiboly.R
import mg.jkmm.baiboly.data.VerseOfDay

@Composable
fun HomeScreen(vm: BibleViewModel, onOpen: (Int, Int, Int, Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyColumn(contentPadding = PaddingValues(18.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 18.dp)) {
                Image(painterResource(R.drawable.logo_jkmm), contentDescription = null, modifier = Modifier.size(46.dp))
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Manao ahoana", color = cs.onSurfaceVariant, fontSize = 12.5.sp)
                    Text("Baiboly JKMM", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
        vm.verseOfDay?.let { v -> item { VerseOfDayHero(v, onOpen) } }
        item {
            Text(
                "Vakiana vao haingana",
                color = cs.onSurfaceVariant, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
            )
        }
        if (vm.history.isEmpty()) {
            item {
                Text(
                    "Mbola tsy nisy novakiana.\nNy toko vakinao dia hiseho eto.",
                    color = cs.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 20.dp),
                )
            }
        } else {
            items(vm.history, key = { "${it.book}-${it.chapter}-${it.from}-${it.to}" }) { h ->
                val name = vm.books.firstOrNull { it.id == h.book }?.name ?: return@items
                // Ex. « Matio 2:2–4 », « Jaona 3:16 » ou « Jaona 3 » (toko manontolo)
                val label = "$name ${h.chapter}" +
                    if (h.from > 0) ":${h.from}" + (if (h.to > h.from) "–${h.to}" else "") else ""
                HistoryCard(label) { onOpen(h.book, h.chapter, h.from, h.to) }
            }
            item {
                Surface(
                    onClick = { vm.clearHistory() },
                    shape = RoundedCornerShape(18.dp),
                    color = cs.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.padding(top = 14.dp),
                ) {
                    Text(
                        "Fafao ny tantara", color = cs.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseOfDayHero(v: VerseOfDay, onOpen: (Int, Int, Int, Int) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(heroBrush()).padding(22.dp),
    ) {
        Column {
            Text("ANDININY ANIO", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                v.text, color = Color.White, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                fontSize = 18.sp, lineHeight = 28.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(v.reference, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(onClick = { onOpen(v.bookId, v.chapter, v.verse, v.verse) }, shape = RoundedCornerShape(20.dp), color = Color.White) {
                    Text("Vakio", color = LocalLook.current.hero[0], fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                Surface(
                    onClick = {
                        clipboard.setText(AnnotatedString("“${v.text}”\n— ${v.reference}"))
                        Toast.makeText(context, "Voadika ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                ) {
                    Text("Adikao", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(label: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = cs.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Bible, contentDescription = null, tint = cs.primary)
            }
            Text(
                label, color = cs.primary, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, fontSize = 17.sp,
                modifier = Modifier.weight(1f).padding(start = 14.dp),
            )
            Text("›", color = cs.tertiary, fontSize = 22.sp)
        }
    }
}
