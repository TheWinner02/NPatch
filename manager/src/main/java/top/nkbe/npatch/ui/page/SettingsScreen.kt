package top.nkbe.npatch.ui.page

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Ballot
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import top.nkbe.npatch.R
import top.nkbe.npatch.config.Configs
import top.nkbe.npatch.config.MyKeyStore
import top.nkbe.npatch.ui.component.AnywhereDropdown
import top.nkbe.npatch.ui.component.CenterTopBar
import top.nkbe.npatch.ui.component.settings.SettingsItem
import top.nkbe.npatch.ui.component.settings.SettingsSwitch
import top.nkbe.npatch.ui.util.LocalSnackbarHost
import top.nkbe.npatch.ui.util.bouncyClickable
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore

private const val TAG = "SettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Settings.label), scrollBehavior = scrollBehavior) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // Category 1: Keystore and Storage
            SettingsCategoryCard(title = "Firma & Firma RVXFE (Keystore)") {
                KeyStore()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                StorageDirectory()
            }

            // Category 2: Patch Compiler & Logs
            SettingsCategoryCard(title = "Compilatore & Patching") {
                DetailPatchLogs()
            }

            // Category 3: Visual Engine & Animations
            SettingsCategoryCard(title = "Motore Grafico & Animazioni") {
                CustomAccentColorSetting()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                BouncyAnimationsSetting()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                BlurIntensitySlider()
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun BlurIntensitySlider() {
    var sliderValue by remember { mutableStateOf(Configs.blurRadius.toFloat()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Grain,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    Text(
                        text = "Sfocatura Sfondo",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intensità del vetro satinato",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${sliderValue.toInt()} dp",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                Configs.blurRadius = it.toInt()
            },
            valueRange = 0f..48f,
            steps = 24
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyStore() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    AnywhereDropdown(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onClick = { expanded = true },
        surface = {
            SettingsItem(
                icon = Icons.Outlined.Ballot,
                title = stringResource(R.string.settings_keystore),
                desc = stringResource(if (MyKeyStore.useDefault) R.string.settings_keystore_default else R.string.settings_keystore_custom),
                modifier = Modifier.bouncyClickable { expanded = true }
            )
        }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_keystore_default)) },
            onClick = {
                scope.launch { MyKeyStore.reset() }
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_keystore_custom)) },
            onClick = {
                expanded = false
                showDialog = true
            }
        )
    }

    if (showDialog) {
        var wrongKeystore by rememberSaveable { mutableStateOf(false) }
        var wrongPassword by rememberSaveable { mutableStateOf(false) }
        var wrongAliasName by rememberSaveable { mutableStateOf(false) }
        var wrongAliasPassword by rememberSaveable { mutableStateOf(false) }

        var path by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var alias by rememberSaveable { mutableStateOf("") }
        var aliasPassword by rememberSaveable { mutableStateOf("") }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            context.contentResolver.openInputStream(uri).use { input ->
                MyKeyStore.tmpFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            path = uri.path ?: ""
        }

        AlertDialog(
            onDismissRequest = { expanded = false; showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        wrongKeystore = false
                        wrongPassword = false
                        wrongAliasName = false
                        wrongAliasPassword = false

                        if (path.isEmpty()) {
                            wrongKeystore = true
                            return@TextButton
                        }
                        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                        try {
                            MyKeyStore.tmpFile.inputStream().use { input ->
                                keyStore.load(input, password.toCharArray())
                            }
                        } catch (e: IOException) {
                            wrongKeystore = true
                            if (e.message == "KeyStore integrity check failed.") {
                                wrongPassword = true
                            }
                            return@TextButton
                        }
                        if (!keyStore.containsAlias(alias)) {
                            wrongAliasName = true
                            return@TextButton
                        }
                        try {
                            keyStore.getKey(alias, aliasPassword.toCharArray())
                        } catch (e: GeneralSecurityException) {
                            wrongAliasPassword = true
                            return@TextButton
                        }

                        scope.launch { MyKeyStore.setCustom(password, alias, aliasPassword) }
                        expanded = false
                        showDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { expanded = false; showDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.settings_keystore_dialog_title),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                launcher.launch("*/*")
                            }
                        }
                    }

                    val wrongText = when {
                        wrongAliasPassword -> stringResource(R.string.settings_keystore_wrong_alias_password)
                        wrongAliasName -> stringResource(R.string.settings_keystore_wrong_alias)
                        wrongPassword -> stringResource(R.string.settings_keystore_wrong_password)
                        wrongKeystore -> stringResource(R.string.settings_keystore_wrong_keystore)
                        else -> null
                    }

                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = wrongText ?: stringResource(R.string.settings_keystore_desc),
                        color = if (wrongText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_keystore_file)) },
                        placeholder = { Text(stringResource(R.string.settings_keystore_file)) },
                        singleLine = true,
                        isError = wrongKeystore,
                        interactionSource = interactionSource,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.settings_keystore_password)) },
                        singleLine = true,
                        isError = wrongPassword,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text(stringResource(R.string.settings_keystore_alias)) },
                        singleLine = true,
                        isError = wrongAliasName,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = aliasPassword,
                        onValueChange = { aliasPassword = it },
                        label = { Text(stringResource(R.string.settings_keystore_alias_password)) },
                        singleLine = true,
                        isError = wrongAliasPassword,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun DetailPatchLogs() {
    SettingsSwitch(
        modifier = Modifier.bouncyClickable { Configs.detailPatchLogs = !Configs.detailPatchLogs },
        checked = Configs.detailPatchLogs,
        icon = Icons.Outlined.BugReport,
        title = stringResource(R.string.settings_detail_patch_logs)
    )
}

@Composable
private fun StorageDirectory() {
    val context = LocalContext.current
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val errorText = stringResource(R.string.patch_select_dir_error)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try {
            if (it.resultCode == Activity.RESULT_CANCELED) return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: throw IOException("No data")
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            Configs.storageDirectory = uri.toString()
            Log.i(TAG, "Storage directory: ${uri.path}")
        } catch (e: Exception) {
            Log.e(TAG, "Error when requesting saving directory", e)
            scope.launch { snackbarHost.showSnackbar(errorText) }
        }
    }
    SettingsItem(
        title = stringResource(R.string.settings_storage_directory),
        desc = Configs.storageDirectory ?: "undefined",
        icon = Icons.Outlined.Folder,
        modifier = Modifier.bouncyClickable { launcher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)) }
    )
}

@Composable
private fun BouncyAnimationsSetting() {
    SettingsSwitch(
        modifier = Modifier.bouncyClickable { Configs.bouncyAnimations = !Configs.bouncyAnimations },
        checked = Configs.bouncyAnimations,
        icon = Icons.Outlined.TouchApp,
        title = "Bouncy Animations",
        desc = "Physics-based scale feedback when tapping items"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAccentColorSetting() {
    var expanded by remember { mutableStateOf(false) }

    val colorNameMap = mapOf(
        "default" to "Dynamic Color (System)",
        "pixel_blue" to "Pixel Blue",
        "mint_green" to "Mint Green",
        "lavender" to "Lavender Purple",
        "peach" to "Peach Cozy",
        "coral" to "Coral Orange"
    )

    AnywhereDropdown(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onClick = { expanded = true },
        surface = {
            SettingsItem(
                icon = Icons.Outlined.Palette,
                title = "Theme Color Scheme",
                desc = colorNameMap[Configs.customAccentColor] ?: "Dynamic Color (System)",
                modifier = Modifier.bouncyClickable { expanded = true }
            )
        }
    ) {
        colorNameMap.forEach { (key, name) ->
            DropdownMenuItem(
                text = { Text(name) },
                onClick = {
                    Configs.customAccentColor = key
                    expanded = false
                }
            )
        }
    }
}
