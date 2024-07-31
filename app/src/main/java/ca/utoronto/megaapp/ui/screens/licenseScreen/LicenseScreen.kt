package ca.utoronto.megaapp.ui.screens.licenseScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirdPartyNotices(navController: NavHostController) {
    Scaffold(topBar = {
        TopAppBar(colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.surface,
        ), title = {
            Text("Third Party Notices")
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.surface,
                    contentDescription = "Back"
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "The following sets forth attribution notices for third party software that may be contained in portions of this product.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Apache License 2.0",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The following components are licensed under the Apache License 2.0 reproduced below:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = "• AOSP, Copyright 2021 The Android Open Source Project",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = "• AndroidX", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "• OkHttp, Copyright 2019 Square, Inc.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = "• kotlinx.serialization", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "• RSS-Parser, Copyright 2016-2023 Marco Gomiero",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Coil, Copyright 2023 Coil Contributors",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}