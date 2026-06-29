package top.nkbe.npatch.ui.component

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import top.nkbe.npatch.ui.theme.LSPTheme

@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    icon: ImageBitmap,
    label: String,
    packageName: String,
    checked: Boolean? = null,
    rightIcon: (@Composable () -> Unit)? = null,
    additionalContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    if (checked != null && rightIcon != null)
        throw IllegalArgumentException("`checked` and `rightIcon` should not be both set")
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    bitmap = icon,
                    contentDescription = label,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = packageName,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                if (additionalContent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    additionalContent()
                }
            }
            if (checked != null) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (rightIcon != null) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    rightIcon()
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppItemPreview() {
    LSPTheme {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(MaterialTheme.colorScheme.primary.toArgb())
        AppItem(
            icon = shape.toBitmap().asImageBitmap(),
            label = "Sample App",
            packageName = "org.lsposed.sample",
            rightIcon = { Icon(Icons.Filled.ArrowForwardIos, null) }
        )
    }
}
