package top.nkbe.npatch.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.nkbe.npatch.ui.util.bouncyClickable

object SelectionColumnScope {

    @Composable
    fun SelectionItem(
        modifier: Modifier = Modifier,
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        title: String,
        desc: String? = null,
        extraContent: (@Composable ColumnScope.() -> Unit)? = null
    ) {
        val containerColor = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            border = BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .bouncyClickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (desc != null || extraContent != null) {
                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            Column {
                                if (desc != null) {
                                    Text(
                                        text = desc,
                                        modifier = Modifier.padding(top = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                extraContent?.invoke(this)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionColumn(
    modifier: Modifier = Modifier,
    content: @Composable() (SelectionColumnScope.() -> Unit)
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = { SelectionColumnScope.content() }
    )
}
