package mg.jkmm.baiboly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import mg.jkmm.baiboly.data.Book

private fun svgIcon(name: String, path: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

/** Icônes de la barre du bas (mêmes dessins que dans l'aperçu). */
object AppIcons {
    val Home = svgIcon("home", "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z")
    val Bible = svgIcon(
        "bible",
        "M21 5c-1.1-.35-2.3-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z",
    )
    val Search = svgIcon(
        "search",
        "M15.5 14h-.79l-.28-.27A6.47 6.47 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
    )
    val Tune = svgIcon(
        "tune",
        "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z",
    )
}

/** Dégradé de la carte « Andininy anio » : suit la couleur choisie dans Safidy. */
@Composable
fun heroBrush(): Brush = Brush.linearGradient(LocalLook.current.hero)

@Composable
fun SoftCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        content = content,
    )
}

/** Sélecteur à pastille (ex. Testamenta Taloha / Vaovao, Mazava / Maizina). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceVariant).padding(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (on) cs.surface else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                    color = if (on) cs.primary else cs.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Feuille du bas en deux étapes : 1) choisir le toko, 2) choisir les andininy
 * (un seul, ou une plage comme 1–5), ou lire le toko entier.
 * onPick(chapitre, de, à) : de = 0 signifie « toko manontolo ».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSheet(
    book: Book,
    verseCount: suspend (Int) -> Int,
    onPick: (chapter: Int, from: Int, to: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var chapter by remember { mutableStateOf<Int?>(null) }
    var from by remember { mutableIntStateOf(0) }
    var to by remember { mutableIntStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.background) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            val ch = chapter
            if (ch == null) {
                Text(
                    book.name, color = cs.primary,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 21.sp,
                )
                Spacer(Modifier.height(12.dp))
                NumberGrid(
                    count = book.chapters,
                    isOn = { false },
                    onTap = { c -> chapter = c; from = 0; to = 0 },
                )
            } else {
                val count by produceState(0, ch) { value = verseCount(ch) }
                Text(
                    "${book.name} $ch", color = cs.primary,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 21.sp,
                )
                Text(
                    "Safidio ny andininy (ohatra 1–5) na vakio ny toko manontolo",
                    color = cs.onSurfaceVariant, fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                val last = if (to > 0) to else from
                NumberGrid(
                    count = count,
                    isOn = { n -> from > 0 && n in from..last },
                    onTap = { n ->
                        when {
                            from == 0 || to != 0 -> { from = n; to = 0 }  // nouvelle sélection
                            n >= from -> to = n                            // fin de plage
                            else -> { to = from; from = n }                // plage à l'envers
                        }
                    },
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onPick(ch, from, if (from > 0) maxOf(from, to) else 0) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        if (from == 0) "Vakio ny toko manontolo"
                        else "Vakio ny andininy $from" + if (last > from) "–$last" else "",
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                TextButton(onClick = { chapter = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("‹ Toko hafa", color = cs.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun NumberGrid(count: Int, isOn: (Int) -> Boolean, onTap: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier.heightIn(max = 380.dp),
    ) {
        items((1..count).toList()) { n ->
            val on = isOn(n)
            Box(
                Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                    .background(if (on) Gold else cs.surfaceVariant)
                    .clickable { onTap(n) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$n", color = if (on) Color(0xFF2B1A14) else cs.primary,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                )
            }
        }
    }
}
