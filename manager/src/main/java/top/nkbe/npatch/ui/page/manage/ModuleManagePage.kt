package top.nkbe.npatch.ui.page.manage

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.nkbe.npatch.ui.util.shimmerWave
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import top.nkbe.npatch.ui.component.AnywhereDropdown
import top.nkbe.npatch.ui.component.AppItem
import top.nkbe.npatch.R
import top.nkbe.npatch.ui.viewmodel.manage.ModuleManageViewModel
import nkbe.util.NeoPackageManager

@Composable
fun ModuleManageBody() {
    val context = LocalContext.current
    val viewModel = viewModel<ModuleManageViewModel>()
    // 下拉刷新
    SwipeRefresh(
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.appList.isEmpty()) {
            if (NeoPackageManager.appList.isEmpty()) {
                ShimmerLoadingSkeleton()
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.manage_no_modules),
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
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val settingsIntent = remember { NeoPackageManager.getSettingsIntent(it.first.app.packageName) }
                    AnywhereDropdown(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onClick = { settingsIntent?.let { context.startActivity(it) } },
                        onLongClick = { expanded = true },
                        surface = {
                            AppItem(
                                icon = NeoPackageManager.getIcon(it.first),
                                label = it.first.label,
                                packageName = it.first.app.packageName,
                                additionalContent = {
                                    Text(
                                        text = it.second.description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            append(AnnotatedString("API", SpanStyle(color = MaterialTheme.colorScheme.secondary)))
                                            append("  ")
                                            append(it.second.api.toString())
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Serif,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            )
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = it.first.label, color = MaterialTheme.colorScheme.primary) },
                            onClick = {}, enabled = false
                        )
                        if (settingsIntent != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.manage_module_settings)) },
                                onClick = { context.startActivity(settingsIntent) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.manage_app_info)) },
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", it.first.app.packageName, null)
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
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
