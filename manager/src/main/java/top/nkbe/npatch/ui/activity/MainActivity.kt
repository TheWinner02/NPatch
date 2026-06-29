package top.nkbe.npatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.nkbe.npatch.ui.page.BottomBarDestination
import top.nkbe.npatch.ui.theme.LSPTheme
import top.nkbe.npatch.ui.util.LocalSnackbarHost

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 檢查並請求權限
        checkAndRequestPermissions()

        setContent {
            val navController = rememberAnimatedNavController()
            LSPTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val hazeState = remember { HazeState() }
                CompositionLocalProvider(
                    LocalSnackbarHost provides snackbarHostState,
                    top.nkbe.npatch.ui.util.LocalHazeState provides hazeState
                ) {
                    Scaffold(
                        bottomBar = { BottomBar(navController, hazeState) },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(hazeState)
                        ) {
                            DestinationsNavHost(
                                modifier = Modifier.padding(innerPadding),
                                navGraph = NavGraphs.root,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (SDK 30) 以上請求 "所有檔案存取權"
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 1001)
            }
        } else {
            // Android 11 以下請求普通讀寫權限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1001
            )
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, hazeState: HazeState) {
    val currentDestination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startRoute
    var topDestination by rememberSaveable { mutableStateOf(currentDestination.route) }
    LaunchedEffect(currentDestination) {
        val queue = navController.currentBackStack.value
        if (queue.size == 2) topDestination = queue[1].destination.route!!
        else if (queue.size > 2) topDestination = queue[2].destination.route!!
    }

    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.hazeEffect(state = hazeState) {
            blurEffect {
                blurRadius = top.nkbe.npatch.config.Configs.blurRadius.dp
            }
        }
    ) {
        BottomBarDestination.values().forEach { destination ->
            NavigationBarItem(
                selected = topDestination == destination.direction.route,
                onClick = {
                    navController.navigate(destination.direction.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (topDestination == destination.direction.route) destination.iconSelected else destination.iconNotSelected,
                        contentDescription = stringResource(destination.label)
                    )
                },
                label = { Text(stringResource(destination.label)) },
                alwaysShowLabel = false
            )
        }
    }
}