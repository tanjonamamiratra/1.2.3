package mg.jkmm.baiboly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Police et interligne choisis, partagés avec l'écran de lecture. */
fun BibleViewModel.verseFont(): FontFamily = if (sansFont) FontFamily.SansSerif else FontFamily.Serif
fun BibleViewModel.lineFactor(): Float = if (largeSpacing) 2.05f else 1.72f

@Composable
fun SettingsScreen(vm: BibleViewModel) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Text(
            "Safidy", color = cs.primary, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold, fontSize = 26.sp,
            modifier = Modifier.padding(top = 18.dp, bottom = 14.dp),
        )
        PreviewCard(vm)
        Gap(); ThemeCard(vm)
        Gap(); AccentCard(vm)
        Gap(); BackgroundCard(vm)
        Gap(); FontCard(vm)
        Gap(); FamilyCard(vm)
        Gap(); SpacingCard(vm)
        Gap(); AlignCard(vm)
        Gap()
        SettingBlock("Baiboly : version ${vm.contentVersion}") {
            OutlinedButton(onClick = { vm.checkUpdate() }, modifier = Modifier.fillMaxWidth()) {
                Text("Hamarino ny fanavaozana")
            }
            if (vm.updateStatus.isNotEmpty()) {
                Text(vm.updateStatus, color = cs.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Gap()
        SettingBlock("Application : version ${vm.versionName}") {
            OutlinedButton(onClick = { vm.checkAppUpdate(true) }, modifier = Modifier.fillMaxWidth()) {
                Text("Hamarino ny fanavaozana ny application")
            }
            if (vm.appUpdateStatus.isNotEmpty() && !vm.updateDialogOpen) {
                Text(vm.appUpdateStatus, color = cs.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Text(
            "Jesosy Kristy Mpanjakan’ny Mpanjaka · J.K.M.M",
            color = cs.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 26.dp),
        )
    }
}

@Composable
private fun Gap() = Spacer(Modifier.height(12.dp))

@Composable
private fun SettingBlock(title: String, content: @Composable () -> Unit) {
    SoftCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}

/** Aperçu en direct : reprend la police, la taille, l'interligne et l'alignement choisis. */
@Composable
private fun PreviewCard(vm: BibleViewModel) {
    val cs = MaterialTheme.colorScheme
    val v = vm.verseOfDay
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = cs.tertiary, fontWeight = FontWeight.Bold, fontSize = (vm.fontSize * 0.7f).sp)) {
            append("${v?.verse ?: 3}")
        }
        append("  ")
        append(v?.text ?: "Ary Andriamanitra nanao hoe: Misia mazava, dia nisy mazava.")
    }
    SettingBlock("Ohatra") {
        Text(
            text, color = cs.onSurface, fontFamily = vm.verseFont(), fontSize = vm.fontSize.sp,
            lineHeight = (vm.fontSize * vm.lineFactor()).sp,
            textAlign = if (vm.justify) TextAlign.Justify else TextAlign.Start,
        )
    }
}

@Composable
fun ThemeCard(vm: BibleViewModel) {
    SettingBlock("Endrika") {
        Segmented(listOf("Araka ny finday", "Mazava", "Maizina"), vm.theme, { vm.updateTheme(it) })
    }
}

@Composable
private fun AccentCard(vm: BibleViewModel) {
    val cs = MaterialTheme.colorScheme
    SettingBlock("Loko") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ACCENTS.forEachIndexed { i, a ->
                val on = vm.accent == i
                Box44(
                    selected = on,
                    onClick = { vm.updateAccent(i) },
                    brush = Brush.linearGradient(listOf(a.lightHero[0], a.lightHero[2])),
                    ring = if (on) cs.primary else cs.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun Box44(selected: Boolean, onClick: () -> Unit, brush: Brush, ring: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.layout.Box(
        Modifier.size(44.dp).clip(CircleShape)
            .border(if (selected) 3.dp else 1.dp, ring, CircleShape)
            .clickable(onClick = onClick)
            .padding(5.dp).clip(CircleShape)
            .background(brush),
    )
}

@Composable
private fun BackgroundCard(vm: BibleViewModel) {
    val cs = MaterialTheme.colorScheme
    val dark = LocalLook.current.dark
    SettingBlock("Fototra") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BGS.forEachIndexed { i, b ->
                val on = vm.bgStyle == i
                val shape = RoundedCornerShape(12.dp)
                Column(
                    Modifier.weight(1f).clickable { vm.updateBgStyle(i) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.fillMaxWidth().height(54.dp).clip(shape)
                            .drawBehind { b.paint(this, dark, false) }
                            .border(2.dp, if (on) cs.primary else cs.outlineVariant, shape),
                    )
                    Text(
                        b.name, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                        color = if (on) cs.primary else cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun FontCard(vm: BibleViewModel) {
    SettingBlock("Haben’ny soratra : ${vm.fontSize.toInt()}") {
        Slider(value = vm.fontSize, onValueChange = { vm.updateFontSize(it) }, valueRange = 14f..30f)
    }
}

@Composable
fun FamilyCard(vm: BibleViewModel) {
    SettingBlock("Karazana soratra") {
        Segmented(listOf("Serif", "Sans"), if (vm.sansFont) 1 else 0, { vm.updateSansFont(it == 1) })
    }
}

@Composable
private fun SpacingCard(vm: BibleViewModel) {
    SettingBlock("Elanelana ny andalana") {
        Segmented(listOf("Antonony", "Lehibe"), if (vm.largeSpacing) 1 else 0, { vm.updateLargeSpacing(it == 1) })
    }
}

@Composable
private fun AlignCard(vm: BibleViewModel) {
    SettingBlock("Fandrafetana") {
        Segmented(listOf("Havia", "Mitovy"), if (vm.justify) 1 else 0, { vm.updateJustify(it == 1) })
    }
}
