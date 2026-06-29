package top.nkbe.npatch.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.nkbe.npatch.ui.util.LocalHazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.blur.blurEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopBar(
    text: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigationIcon: @Composable () -> Unit = {}
) {
    val hazeState = LocalHazeState.current
    val topBarModifier = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState) {
            blurEffect {
                blurRadius = top.nkbe.npatch.config.Configs.blurRadius.dp
            }
        }
    } else {
        Modifier
    }

    LargeTopAppBar(
        title = {
            Text(
                text = text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navigationIcon,
        scrollBehavior = scrollBehavior,
        modifier = topBarModifier,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )
}

