package top.nkbe.npatch.ui.page.manage

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import top.nkbe.npatch.ui.util.shimmerWave
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch
import top.nkbe.npatch.R
import top.nkbe.npatch.BuildConfig
import top.nkbe.npatch.config.ConfigManager
import top.nkbe.npatch.config.Configs
import top.nkbe.npatch.database.entity.Module
import top.nkbe.npatch.lspApp
import top.nkbe.npatch.share.Constants
import top.nkbe.npatch.share.LSPConfig
import top.nkbe.npatch.ui.component.AnywhereDropdown
import top.nkbe.npatch.ui.component.AppItem
import top.nkbe.npatch.ui.component.LoadingDialog
import top.nkbe.npatch.ui.page.ACTION_APPLIST
import top.nkbe.npatch.ui.page.ACTION_STORAGE
import top.nkbe.npatch.ui.page.SelectAppsResult
import com.ramcosta.composedestinations.generated.destinations.NewPatchScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SelectAppsScreenDestination
import top.nkbe.npatch.ui.util.LocalSnackbarHost
import top.nkbe.npatch.ui.util.bouncyClickable
import top.nkbe.npatch.ui.viewmodel.manage.AppManageViewModel
import top.nkbe.npatch.ui.viewstate.ProcessingState
import nkbe.util.NeoPackageManager
import nkbe.util.ShizukuApi
import java.io.IOException

private const val TAG = "AppManagePage"

@Composable
fun AppManageBody(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<SelectAppsScreenDestination, SelectAppsResult>
) {
    val viewModel = viewModel<AppManageViewModel>()
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    var scopeApp by rememberSaveable { mutableStateOf("") }
    var afterCheckManager by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    resultRecipient.onNavResult {
        if (it is NavResult.Value) {
            scope.launch {
                val result = it.value as SelectAppsResult.MultipleApps
                ConfigManager.getModulesForApp(scopeApp).forEach {
                    ConfigManager.deactivateModule(scopeApp, it)
                }
                result.selected.forEach {
                    Log.d(TAG, "Activate ${it.app.packageName} for $scopeApp")
                    ConfigManager.activateModule(scopeApp, Module(it.app.packageName, it.app.sourceDir))
                }
            }
        }
    }

    when (viewModel.updateLoaderState) {
        is ProcessingState.Idle -> Unit
        is ProcessingState.Processing -> LoadingDialog()
        is ProcessingState.Done -> {
            val it = viewModel.updateLoaderState as ProcessingState.Done
            val updateSuccessfully = stringResource(R.string.manage_update_loader_successfully)
            val updateFailed = stringResource(R.string.manage_update_loader_failed)
            val copyError = stringResource(R.string.copy_error)
            LaunchedEffect(Unit) {
                it.result.onSuccess {
                    snackbarHost.showSnackbar(updateSuccessfully)
                }.onFailure {
                    val result = snackbarHost.showSnackbar(updateFailed, copyError)
                    if (result == SnackbarResult.ActionPerformed) {
                        val cm = lspApp.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("NPatch", it.toString()))
                    }
                }
                viewModel.dispatch(AppManageViewModel.ViewAction.ClearUpdateLoaderResult)
            }
        }
    }
    when (viewModel.optimizeState) {
        is ProcessingState.Idle -> Unit
        is ProcessingState.Processing -> LoadingDialog()
        is ProcessingState.Done -> {
            val it = viewModel.optimizeState as ProcessingState.Done
            val optimizeSucceed = stringResource(R.string.manage_optimize_successfully)
            val optimizeFailed = stringResource(R.string.manage_optimize_failed)
            LaunchedEffect(Unit) {
                snackbarHost.showSnackbar(if (it.result) optimizeSucceed else optimizeFailed)
                viewModel.dispatch(AppManageViewModel.ViewAction.ClearOptimizeResult)
            }
        }
    }
    // 下拉刷新
    SwipeRefresh(
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = { viewModel.dispatch(AppManageViewModel.ViewAction.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.appList.isEmpty()) {
            if (NeoPackageManager.appList.isEmpty()) {
                ShimmerLoadingSkeleton()
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.manage_no_apps),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(
                    items = viewModel.appList,
                    key = { it.first.app.packageName }
                ) { (appInfo, patchConfig) ->
                    val isRolling = patchConfig.useManager && patchConfig.lspConfig.VERSION_CODE >= Constants.MIN_ROLLING_VERSION_CODE
                    val canUpdateLoader = !isRolling && (patchConfig.lspConfig.VERSION_CODE < LSPConfig.instance.VERSION_CODE || patchConfig.managerPackageName != BuildConfig.APPLICATION_ID)
                    var expanded by remember { mutableStateOf(false) }

                    AnywhereDropdown(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onClick = { expanded = true },
                        onLongClick = { expanded = true },
                        surface = {
                            AppItem(
                                icon = NeoPackageManager.getIcon(appInfo),
                                label = appInfo.label,
                                packageName = appInfo.app.packageName,
                                additionalContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val patchText = if (patchConfig.useManager) {
                                            stringResource(R.string.patch_local)
                                        } else {
                                            stringResource(R.string.patch_integrated)
                                        }
                                        val patchColor = if (patchConfig.useManager) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.tertiary
                                        }
                                        val versionText = if (isRolling) {
                                            stringResource(R.string.manage_rolling)
                                        } else {
                                            patchConfig.lspConfig.VERSION_CODE.toString()
                                        }

                                        Text(
                                            text = "$patchText  $versionText",
                                            color = patchColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Serif,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (canUpdateLoader) {
                                            with(LocalDensity.current) {
                                                val size = MaterialTheme.typography.bodySmall.fontSize * 1.2
                                                Icon(Icons.Filled.KeyboardCapslock, null, Modifier.size(size.toDp()))
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = appInfo.label, color = MaterialTheme.colorScheme.primary) },
                            onClick = {}, enabled = false
                        )
                        val shizukuUnavailable = stringResource(R.string.shizuku_unavailable)
                        if (canUpdateLoader || BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.manage_update_loader)) },
                                onClick = {
                                    expanded = false
                                    scope.launch {
                                        viewModel.dispatch(AppManageViewModel.ViewAction.UpdateLoader(appInfo, patchConfig))
                                    }
                                }
                            )
                        }
                        if (patchConfig.useManager) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.manage_module_scope)) },
                                onClick = {
                                    expanded = false
                                    scope.launch {
                                        scopeApp = appInfo.app.packageName
                                        val activated = ConfigManager.getModulesForApp(scopeApp).map { it.pkgName }.toSet()
                                        val initialSelected = NeoPackageManager.appList.mapNotNullTo(ArrayList()) {
                                            if (activated.contains(it.app.packageName)) it.app.packageName else null
                                        }
                                        navigator.navigate(SelectAppsScreenDestination(true, initialSelected))
                                    }
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.manage_optimize)) },
                            onClick = {
                                expanded = false
                                scope.launch {
                                    if (!ShizukuApi.isPermissionGranted) {
                                        snackbarHost.showSnackbar(shizukuUnavailable)
                                    } else {
                                        viewModel.dispatch(AppManageViewModel.ViewAction.PerformOptimize(appInfo))
                                    }
                                }
                            }
                        )
                        val uninstallSuccessfully = stringResource(R.string.manage_uninstall_successfully)
                        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                scope.launch {
                                    snackbarHost.showSnackbar(uninstallSuccessfully)
                                    viewModel.dispatch(AppManageViewModel.ViewAction.Refresh)
                                }
                            }
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.uninstall)) },
                            onClick = {
                                expanded = false
                                val intent = Intent(Intent.ACTION_DELETE).apply {
                                    data = Uri.parse("package:${appInfo.app.packageName}")
                                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                }
                                launcher.launch(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppManageFab(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    var shouldSelectDirectory by remember { mutableStateOf(false) }
    var showNewPatchDialog by remember { mutableStateOf(false) }

    val errorText = stringResource(R.string.patch_select_dir_error)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try {
            if (it.resultCode == Activity.RESULT_CANCELED) return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: throw IOException("No data")
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            Configs.storageDirectory = uri.toString()
            Log.i(TAG, "Storage directory: ${uri.path}")
            showNewPatchDialog = true
        } catch (e: Exception) {
            Log.e(TAG, "Error when requesting saving directory", e)
            scope.launch { snackbarHost.showSnackbar(errorText) }
        }
    }

    if (shouldSelectDirectory) {
        AlertDialog(
            onDismissRequest = { shouldSelectDirectory = false },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.ok)) },
                    onClick = {
                        launcher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                        shouldSelectDirectory = false
                    }
                )
            },
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.cancel)) },
                    onClick = { shouldSelectDirectory = false }
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.patch_select_dir_title),
                    textAlign = TextAlign.Center
                )
            },
            text = { Text(stringResource(R.string.patch_select_dir_text)) }
        )
    }

    if (showNewPatchDialog) {
        AlertDialog(
            onDismissRequest = { showNewPatchDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.cancel)) },
                    onClick = { showNewPatchDialog = false }
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.screen_new_patch),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        onClick = {
                            navigator.navigate(NewPatchScreenDestination(id = ACTION_STORAGE))
                            showNewPatchDialog = false
                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = stringResource(R.string.patch_from_storage),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        onClick = {
                            navigator.navigate(NewPatchScreenDestination(id = ACTION_APPLIST))
                            showNewPatchDialog = false
                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = stringResource(R.string.patch_from_applist),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        )
    }

    val onFabClick: () -> Unit = {
        val uri = Configs.storageDirectory?.toUri()
        if (uri == null) {
            shouldSelectDirectory = true
        } else {
            runCatching {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                if (DocumentFile.fromTreeUri(context, uri)?.exists() == false) throw IOException("Storage directory was deleted")
            }.onSuccess {
                showNewPatchDialog = true
            }.onFailure {
                Log.w(TAG, "Failed to take persistable permission for saved uri", it)
                Configs.storageDirectory = null
                shouldSelectDirectory = true
            }
        }
    }

    ExtendedFloatingActionButton(
        text = { Text("Nuova Patch", fontWeight = FontWeight.Bold) },
        icon = { Icon(Icons.Filled.Add, null) },
        onClick = onFabClick,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.bouncyClickable(onClick = onFabClick)
    )
}

@Composable
private fun ShimmerLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .shimmerWave()
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerWave()
                        )
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmerWave()
                        )
                    }
                }
            }
        }
    }
}
