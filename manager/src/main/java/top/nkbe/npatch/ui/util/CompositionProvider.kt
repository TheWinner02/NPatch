package top.nkbe.npatch.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState

val LocalSnackbarHost = compositionLocalOf<SnackbarHostState> {
    error("CompositionLocal LocalSnackbarController not present")
}

val LocalHazeState = compositionLocalOf<HazeState?> {
    null
}
