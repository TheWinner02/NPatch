package top.nkbe.npatch.ui.page

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import top.nkbe.npatch.R
import top.nkbe.npatch.share.LSPConfig
import top.nkbe.npatch.ui.component.CenterTopBar
import com.ramcosta.composedestinations.generated.destinations.ManageScreenDestination
import com.ramcosta.composedestinations.generated.destinations.NewPatchScreenDestination
import top.nkbe.npatch.ui.util.HtmlText
import top.nkbe.npatch.ui.util.LocalSnackbarHost
import nkbe.util.ShizukuApi
import rikka.shizuku.Shizuku

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import top.nkbe.npatch.ui.util.bouncyClickable
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    var isIntentLaunched by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as Activity
    val intent = activity.intent
    LaunchedEffect(Unit) {
        if (!isIntentLaunched && intent.action == Intent.ACTION_VIEW && intent.hasCategory(Intent.CATEGORY_DEFAULT) && intent.type == "application/vnd.android.package-archive") {
            isIntentLaunched = true
            val uri = intent.data
            if (uri != null) {
                navigator.navigate(ManageScreenDestination)
                navigator.navigate(
                    NewPatchScreenDestination(
                        id = ACTION_INTENT_INSTALL,
                        data = uri
                    )
                )
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { CenterTopBar(stringResource(R.string.app_name), scrollBehavior = scrollBehavior) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShizukuCard()
            InfoCard()
            SupportCard()
            Spacer(Modifier.height(16.dp))
        }
    }
}

private val listener: (Int, Int) -> Unit = { _, grantResult ->
    ShizukuApi.isPermissionGranted = grantResult == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun ShizukuCard() {
    LaunchedEffect(Unit) {
        Shizuku.addRequestPermissionResultListener(listener)
    }
    DisposableEffect(Unit) {
        onDispose {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }

    val isGranted = ShizukuApi.isPermissionGranted
    val isAvailable = ShizukuApi.isBinderAvailable

    val cardColor = if (isGranted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    }

    val statusColor = if (isGranted) {
        MaterialTheme.colorScheme.primary
    } else if (isAvailable) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Visual Status Ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track Ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = statusColor.copy(alpha = 0.15f),
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Active Glowing Ring
                CircularProgressIndicator(
                    progress = { if (isGranted) 1f else 0.4f },
                    modifier = Modifier.fillMaxSize(),
                    color = statusColor,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Center Icon / Status Symbol
                Icon(
                    imageVector = if (isGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(if (isGranted) R.string.shizuku_available else R.string.shizuku_unavailable),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isGranted) "API ${Shizuku.getVersion()} • " + stringResource(R.string.shizuku_available) else stringResource(R.string.home_shizuku_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (isAvailable && !isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { Shizuku.requestPermission(114514) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.bouncyClickable {
                        Shizuku.requestPermission(114514)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.shizuku_available),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val context = LocalContext.current
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    val apiVersion = if (Build.VERSION.PREVIEW_SDK_INT != 0) {
        "${Build.VERSION.CODENAME} Preview (API ${Build.VERSION.PREVIEW_SDK_INT})"
    } else {
        "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    val deviceName = buildString {
        append(Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
        if (Build.BRAND != Build.MANUFACTURER) {
            append(" " + Build.BRAND.replaceFirstChar { it.uppercase() })
        }
        append(" " + Build.MODEL)
    }

    val infoList = listOf(
        stringResource(R.string.home_api_version) to "${LSPConfig.instance.API_CODE}",
        stringResource(R.string.home_npatch_version) to "${LSPConfig.instance.VERSION_NAME} (${LSPConfig.instance.VERSION_CODE})",
        stringResource(R.string.home_framework_version) to "${LSPConfig.instance.CORE_VERSION_NAME} (${LSPConfig.instance.CORE_VERSION_CODE})",
        stringResource(R.string.home_system_version) to apiVersion,
        stringResource(R.string.home_device) to deviceName,
        stringResource(R.string.home_system_abi) to Build.SUPPORTED_ABIS[0]
    )

    val copySuccessMessage = stringResource(R.string.home_info_copied)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_device_info),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .bouncyClickable {
                        val contentString = infoList.joinToString("\n") { "${it.first}: ${it.second}" }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("NPatch Info", contentString))
                        scope.launch { snackbarHost.showSnackbar(copySuccessMessage) }
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Row 1: Device and System
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.PhoneAndroid,
                label = stringResource(R.string.home_device),
                value = deviceName,
                iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.home_system_version),
                value = apiVersion,
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        // Row 2: NPatch and Core Framework
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Label,
                label = stringResource(R.string.home_npatch_version),
                value = LSPConfig.instance.VERSION_NAME,
                iconBgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Extension,
                label = stringResource(R.string.home_framework_version),
                value = LSPConfig.instance.CORE_VERSION_NAME,
                iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Row 3: API Level and ABI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Bolt,
                label = stringResource(R.string.home_api_version),
                value = "${LSPConfig.instance.API_CODE}",
                iconBgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onErrorContainer
            )
            DashboardGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Memory,
                label = stringResource(R.string.home_system_abi),
                value = Build.SUPPORTED_ABIS[0],
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DashboardGridCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconBgColor: Color,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SupportCard() {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_support),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HtmlText(
                stringResource(
                    R.string.home_view_source_code,
                    "<b><a href=\"https://github.com/7723mod/NPatch\">GitHub</a></b>",
                    "<b><a href=\"https://t.me/NPatch\">Telegram</a></b>"
                )
            )
        }
    }
}