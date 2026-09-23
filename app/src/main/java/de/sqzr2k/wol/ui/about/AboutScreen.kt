package de.sqzr2k.wol.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.sqzr2k.wol.BuildConfig
import de.sqzr2k.wol.R

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    val projectUrl = stringResource(R.string.project_url)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
        Text(stringResource(R.string.about_description))
        TextButton(onClick = { uriHandler.openUri(projectUrl) }) {
            Text(stringResource(R.string.project_on_github))
        }
        Text(stringResource(R.string.about_privacy_summary), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.about_local_data))
        Text(stringResource(R.string.about_license))
    }
}
