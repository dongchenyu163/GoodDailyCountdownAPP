package com.dlx.smartalarm.demo.components.favorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import dev.icerock.moko.resources.compose.stringResource
import demo.composeapp.generated.resources.Res
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import androidx.compose.animation.core.tween

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            Res.readBytes("files/FavIcon.lottie")
        )
    }

    val targetProgress = if (isFavorite) 1f else 0f
    val progressState by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progressState }
        ),
        contentDescription = stringResource(com.dlx.smartalarm.demo.MR.strings.favorite),
        modifier = modifier
            .clickable { onToggle() }
            .size(48.dp)
    )
}
