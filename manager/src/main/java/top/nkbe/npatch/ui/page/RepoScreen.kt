package top.nkbe.npatch.ui.page

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import top.nkbe.npatch.R
import top.nkbe.npatch.ui.component.CenterTopBar
import top.nkbe.npatch.ui.util.bouncyClickable
import androidx.compose.ui.input.nestedscroll.nestedScroll

data class Repository(
    val id: String,
    val name: String,
    val url: String,
    val modulesCount: Int,
    val isEnabled: Boolean = true,
    val lastSynced: String = "2 ore fa"
)

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun RepoScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var repos by remember {
        mutableStateOf(
            listOf(
                Repository("1", "NPatch Official Repo", "https://repo.nkbe.top/npatch", 18, true, "1 ora fa"),
                Repository("2", "RVX Lite Patches", "https://github.com/rvxfe/lite-patches", 9, true, "4 ore fa"),
                Repository("3", "Community Modules Source", "https://raw.githubusercontent.com/community/modules", 32, false, "Ieri")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newRepoName by remember { mutableStateOf("") }
    var newRepoUrl by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Repo.label), scrollBehavior = scrollBehavior) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.bouncyClickable {
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Repo")
            }
        }
    ) { innerPadding ->
        if (repos.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Source,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nessun repository",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aggiungi un repository di terze parti per scaricare nuovi moduli.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(repos, key = { it.id }) { repo ->
                    RepositoryCard(
                        repo = repo,
                        onToggle = { enabled ->
                            repos = repos.map { if (it.id == repo.id) it.copy(isEnabled = enabled) else it }
                        },
                        onDelete = {
                            repos = repos.filter { it.id != repo.id }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(text = "Nuovo Repository", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newRepoName,
                            onValueChange = { newRepoName = it },
                            label = { Text("Nome Repository") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newRepoUrl,
                            onValueChange = { newRepoUrl = it },
                            label = { Text("URL Repository") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRepoName.isNotBlank() && newRepoUrl.isNotBlank()) {
                                repos = repos + Repository(
                                    id = System.currentTimeMillis().toString(),
                                    name = newRepoName,
                                    url = newRepoUrl,
                                    modulesCount = 0,
                                    isEnabled = true,
                                    lastSynced = "Adesso"
                                )
                                newRepoName = ""
                                newRepoUrl = ""
                                showAddDialog = false
                            }
                        },
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("Aggiungi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annulla")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
private fun RepositoryCard(
    repo: Repository,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (repo.isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            if (repo.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (repo.isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = null,
                            tint = if (repo.isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = repo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (repo.isEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = repo.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = repo.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Aggiorna ora") },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, null) },
                            onClick = { menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Modifica") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = { menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${repo.modulesCount} moduli") },
                        shape = RoundedCornerShape(100.dp)
                    )
                    if (repo.isEnabled) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Attivo") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }

                Text(
                    text = "Sincronizzato: ${repo.lastSynced}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}