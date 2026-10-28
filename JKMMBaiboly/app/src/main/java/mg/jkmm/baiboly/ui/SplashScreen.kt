package mg.jkmm.baiboly.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mg.jkmm.baiboly.R

/** Durée totale de l'écran de chargement (la barre se remplit entre ~1,3 s et ~2,9 s). */
const val SPLASH_MILLIS = 3100L

/**
 * Écran de chargement : le logo apparaît en rebondissant puis flotte doucement,
 * le titre se révèle, et une barre de chargement dorée se remplit.
 * Toujours sur fond ivoire, comme le logo.
 */
@Composable
fun SplashScreen() {
    val logoScale = remember { Animatable(0.55f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-8f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val barAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            delay(150)
            launch { logoAlpha.animateTo(1f, tween(600)) }
            launch { logoRotation.animateTo(0f, tween(900, easing = FastOutSlowInEasing)) }
            logoScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
        }
        launch { delay(800); titleAlpha.animateTo(1f, tween(700)) }
        launch { delay(1050); subtitleAlpha.animateTo(1f, tween(700)) }
        launch { delay(1200); barAlpha.animateTo(1f, tween(500)) }
        launch { delay(1300); progress.animateTo(1f, tween(1600, easing = FastOutSlowInEasing)) }
    }

    // Mouvements continus : le logo « respire » et la lueur dorée pulse
    val infinite = rememberInfiniteTransition(label = "splash")
    val floatY by infinite.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY",
    )
    val breathe by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe",
    )
    val halo by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "halo",
    )
    val density = LocalDensity.current.density

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color.White, Ivory, Color(0xFFF7E3BD))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(260.dp)
                        .graphicsLayer { alpha = halo * 0.9f; scaleX = 0.9f + halo * 0.2f; scaleY = 0.9f + halo * 0.2f }
                        .background(Brush.radialGradient(listOf(Gold.copy(alpha = 0.4f), Color.Transparent)), CircleShape),
                )
                Image(
                    painterResource(R.drawable.logo_jkmm),
                    contentDescription = "JKMM",
                    modifier = Modifier.size(170.dp).graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value * breathe
                        scaleY = logoScale.value * breathe
                        rotationZ = logoRotation.value
                        translationY = floatY * density
                    },
                )
            }
            Text(
                "Baiboly Masina", color = Bordeaux, fontSize = 27.sp,
                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = (1f - titleAlpha.value) * 20f * density
                },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Dikanteny 1865", color = Orange, fontSize = 15.sp, fontFamily = FontFamily.Serif,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                    translationY = (1f - subtitleAlpha.value) * 20f * density
                },
            )
            Spacer(Modifier.height(34.dp))
            // Barre de chargement
            Box(
                Modifier.width(180.dp).height(6.dp).clip(RoundedCornerShape(3.dp))
                    .background(Bordeaux.copy(alpha = 0.13f))
                    .graphicsLayer { alpha = barAlpha.value },
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(progress.value)
                        .background(Brush.horizontalGradient(listOf(Gold, Orange, Bordeaux))),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Miandry kely…", color = Color(0xFF7A5F4F), fontSize = 12.5.sp,
                modifier = Modifier.graphicsLayer { alpha = barAlpha.value },
            )
        }
    }
}
