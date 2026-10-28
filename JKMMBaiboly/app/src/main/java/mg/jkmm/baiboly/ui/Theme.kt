package mg.jkmm.baiboly.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlin.math.atan2

// Couleurs de la marque (écran de chargement, surlignage)
val Ivory = Color(0xFFFFF8EC)
val Gold = Color(0xFFF0A830)
val Orange = Color(0xFFE8821A)
val Bordeaux = Color(0xFF7A1230)
private val Ink = Color(0xFF2B1A14)

private fun c(v: Long) = Color(0xFF000000 or v)

/** Couleur principale au choix : titres, boutons, carte du verset du jour. */
class AccentSpec(
    val name: String,
    val lightPri: Color, val lightAcc: Color, val lightHero: List<Color>,
    val darkPri: Color, val darkAcc: Color, val darkHero: List<Color>,
)

val ACCENTS = listOf(
    AccentSpec("Bordeaux", c(0x7A1230), c(0xE8821A), listOf(c(0x7A1230), c(0xB4331F), c(0xE8821A)),
        c(0xF0A830), c(0xE8821A), listOf(c(0x5C1226), c(0x8A2A1C), c(0xC0661A))),
    AccentSpec("Volamena", c(0x8A5A00), c(0xE09A1A), listOf(c(0x8A5A00), c(0xC98410), c(0xF0B83A)),
        c(0xF6C25B), c(0xF0A830), listOf(c(0x5A3A00), c(0x8A6008), c(0xC8921E))),
    AccentSpec("Maitso", c(0x1F6B4A), c(0x2E9E6E), listOf(c(0x14543A), c(0x238B60), c(0x4DBB8A)),
        c(0x6FD3A2), c(0x3FA374), listOf(c(0x0E3A28), c(0x1A6A4A), c(0x2E9E6E))),
    AccentSpec("Manga", c(0x1F4E8C), c(0x3B82D6), listOf(c(0x163B6B), c(0x2A66B8), c(0x5AA0EC)),
        c(0x8DBEF2), c(0x4A90D9), listOf(c(0x10294A), c(0x1C4A86), c(0x3B7CC8))),
    AccentSpec("Volomparasy", c(0x5B2A86), c(0x9457CF), listOf(c(0x43206A), c(0x7A3FB0), c(0xB07AE0)),
        c(0xC9A2F0), c(0xA06AD4), listOf(c(0x33174F), c(0x5E2F8E), c(0x8A55C2))),
)

/** Style de fond : couleurs des surfaces + dessin du fond (dégradés, branches de laurier). */
class BgSpec(
    val name: String,
    val lightSolid: Color, val lightCard: Color, val lightSoft: Color, val lightLine: Color,
    val darkSolid: Color, val darkCard: Color, val darkSoft: Color, val darkLine: Color,
    val paint: DrawScope.(dark: Boolean, leaves: Boolean) -> Unit,
)

val BGS = listOf(
    BgSpec("Klasika",
        c(0xFFF8EC), c(0xFFFFFF), c(0xF6E9D0), c(0xECDDBE),
        c(0x1A1210), c(0x271B17), c(0x33241E), c(0x44332B),
    ) { dark, leaves ->
        if (!dark) {
            drawRect(Brush.verticalGradient(listOf(c(0xFFFCF4), c(0xFFF4E0))))
        } else {
            drawRect(c(0x1A1210))
            drawRect(Brush.radialGradient(listOf(c(0x3A2420), c(0x3A2420).copy(alpha = 0f)), Offset.Zero, size.maxDimension * 0.8f))
            drawRect(Brush.radialGradient(listOf(c(0x2E1A22), c(0x2E1A22).copy(alpha = 0f)), Offset(size.width, size.height), size.maxDimension * 0.65f))
        }
        if (leaves) laurel(if (dark) Gold else c(0xE8A93A), if (dark) 0.14f else 0.24f)
    },
    BgSpec("Malefaka",
        c(0xF6EBD3), c(0xFFFBF2), c(0xEFDDB8), c(0xE2CEA2),
        c(0x0F1A2B), c(0x16253B), c(0x1F324E), c(0x2C4263),
    ) { dark, _ ->
        val center = Offset(size.width / 2f, 0f)
        drawRect(
            if (!dark) Brush.radialGradient(0f to c(0xFFF6E2), 0.7f to c(0xF4E5C6), 1f to c(0xEAD6AD), center = center, radius = size.maxDimension)
            else Brush.radialGradient(0f to c(0x1B2E4A), 0.65f to c(0x0F1A2B), 1f to c(0x0A1220), center = center, radius = size.maxDimension),
        )
    },
    BgSpec("Maraina",
        c(0xFDEBD8), c(0xFFFBF6), c(0xF8DCC4), c(0xEFCBB0),
        c(0x1C1018), c(0x2A1A24), c(0x38222F), c(0x4B3040),
    ) { dark, _ ->
        val colors = if (!dark) listOf(c(0xFFF6EA), c(0xFDE6CF), c(0xF7D3BE)) else listOf(c(0x2C1522), c(0x1C1018), c(0x120B12))
        drawRect(Brush.linearGradient(colors, Offset.Zero, Offset(size.width, size.height)))
    },
    BgSpec("Tsotra",
        c(0xFFFFFF), c(0xF7F3EA), c(0xEFE8D8), c(0xE6DECB),
        c(0x000000), c(0x121212), c(0x1C1C1C), c(0x2A2A2A),
    ) { dark, _ -> drawRect(if (dark) Color.Black else Color.White) },
)

/** Deux branches de laurier en filigrane : coin haut-gauche et coin bas-droit. */
private fun DrawScope.laurel(color: Color, alpha: Float) {
    val box = 190.dp.toPx()
    val k = box / 200f
    for (flip in listOf(false, true)) {
        withTransform({
            if (flip) {
                translate(size.width - box, size.height - box)
                rotate(180f, Offset(box / 2f, box / 2f))
            }
            scale(k, k, Offset.Zero)
        }) { laurelBranch(color, alpha) }
    }
}

private fun DrawScope.laurelBranch(color: Color, alpha: Float) {
    val pts = (0..8).map { i ->
        val t = i / 8f
        Offset(10f + t * 165f, 8f + t * t * 80f + t * 14f)
    }
    val stem = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    val leaf = Path().apply {
        moveTo(0f, 0f)
        cubicTo(8f, -9f, 30f, -9f, 46f, 0f)
        cubicTo(30f, 9f, 8f, 9f, 0f, 0f)
        close()
    }
    // Un seul calque semi-transparent : les feuilles qui se chevauchent ne foncent pas
    val layerPaint = Paint()
    layerPaint.alpha = alpha
    drawIntoCanvas { it.saveLayer(Rect(-60f, -60f, 260f, 260f), layerPaint) }
    drawPath(stem, color, style = Stroke(width = 2.2f))
    for (i in 1 until pts.size) {
        val angle = Math.toDegrees(
            atan2((pts[i].y - pts[i - 1].y).toDouble(), (pts[i].x - pts[i - 1].x).toDouble()),
        ).toFloat()
        for (side in listOf(-1f, 1f)) {
            withTransform({
                translate(pts[i].x, pts[i].y)
                rotate(angle + side * 42f, Offset.Zero)
            }) { drawPath(leaf, color) }
        }
    }
    drawIntoCanvas { it.restore() }
}

/** Apparence courante, lue par les écrans (fond, dégradé de la carte du verset du jour). */
class Look(val dark: Boolean, val bg: BgSpec, val hero: List<Color>)

val LocalLook = staticCompositionLocalOf { Look(false, BGS[0], ACCENTS[0].lightHero) }

/** Dessine le fond de l'application derrière tous les écrans. */
@Composable
fun Modifier.appBackground(): Modifier {
    val look = LocalLook.current
    return drawBehind { look.bg.paint(this, look.dark, true) }
}

/** theme : 0 = automatique, 1 = clair, 2 = sombre */
@Composable
fun isDark(theme: Int): Boolean = when (theme) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun JKMMTheme(dark: Boolean, accent: Int, bgStyle: Int, content: @Composable () -> Unit) {
    val a = ACCENTS[accent.coerceIn(0, ACCENTS.lastIndex)]
    val b = BGS[bgStyle.coerceIn(0, BGS.lastIndex)]

    val scheme = if (!dark) {
        lightColorScheme(
            primary = a.lightPri, onPrimary = Color.White,
            primaryContainer = b.lightSoft, onPrimaryContainer = a.lightPri,
            secondary = Gold, onSecondary = Ink,
            tertiary = a.lightAcc,
            background = b.lightSolid, onBackground = Ink,
            surface = b.lightCard, onSurface = Ink,
            surfaceVariant = b.lightSoft, onSurfaceVariant = Color(0xFF5A3A2E),
            outlineVariant = b.lightLine,
        )
    } else {
        darkColorScheme(
            primary = a.darkPri, onPrimary = Color(0xFF2B1A14),
            primaryContainer = b.darkSoft, onPrimaryContainer = a.darkPri,
            secondary = a.darkAcc, onSecondary = Color.White,
            tertiary = a.darkAcc,
            background = b.darkSolid, onBackground = Color(0xFFF3E7D3),
            surface = b.darkCard, onSurface = Color(0xFFF3E7D3),
            surfaceVariant = b.darkSoft, onSurfaceVariant = Color(0xFFD9C3A5),
            outlineVariant = b.darkLine,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalLook provides Look(dark, b, if (dark) a.darkHero else a.lightHero)) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
