package top.nkbe.npatch.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSlot(
    modifier: Modifier,
    enabled: Boolean,
    icon: ImageVector? = null,
    title: String,
    desc: String?,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)?,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Column {
                if (desc != null) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .alpha(0.75f)
                            .padding(top = 4.dp)
                    )
                }
                extraContent?.invoke(this)
            }
        }
        action?.invoke(this)
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    title: String,
    desc: String? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null
) = SettingsSlot(modifier, enabled, icon, title, desc, extraContent, null)
