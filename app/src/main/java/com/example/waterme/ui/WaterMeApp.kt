/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.waterme.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Intent
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import com.example.waterme.data.Reminder
import com.example.waterme.model.Plant
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.waterme.ui.theme.WaterMeTheme
import com.example.waterme.R
import com.example.waterme.THIRTY_SECONDS
import com.example.waterme.FIVE_MINUTES
import com.example.waterme.TEN_MINUTES
import com.example.waterme.ONE_HOUR
import com.example.waterme.SEVEN_DAYS
import com.example.waterme.FOURTEEN_DAYS
import com.example.waterme.THIRTY_DAYS

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import java.util.concurrent.TimeUnit

@Composable
fun WaterMeApp(waterViewModel: WaterViewModel = viewModel(factory = WaterViewModel.Factory)) {
    val layoutDirection = LocalLayoutDirection.current
    val navController = rememberNavController()
    val plants by waterViewModel.plants.collectAsState()

    WaterMeTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start = WindowInsets.safeDrawing.asPaddingValues()
                        .calculateStartPadding(layoutDirection),
                    end = WindowInsets.safeDrawing.asPaddingValues()
                        .calculateEndPadding(layoutDirection)
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "landing") {
                composable("landing") {
                    LandingScreen(onNextClicked = {
                        navController.navigate("plantList")
                    })
                }
                composable("plantList") {
                    PlantListContent(
                        plants = plants,
                        onScheduleReminder = { waterViewModel.scheduleReminder(it) },
                        onAddPlant = { name, type, desc, sched, uri ->
                             waterViewModel.addPlant(name, type, desc, sched, uri)
                        },
                        onDeletePlant = { waterViewModel.deletePlant(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun LandingScreen(onNextClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Intriguing graphic or title area
        Text(
            text = "Welcome to Water Me",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Keep your garden thriving perfectly on schedule.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Next", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}

@Composable
fun PlantListContent(
    plants: List<Plant>,
    onScheduleReminder: (Reminder) -> Unit,
    onAddPlant: (String, String, String, String, String?) -> Unit,
    onDeletePlant: (Plant) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlant by rememberSaveable { mutableStateOf<Plant?>(null) }
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add Plant", tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = modifier.padding(innerPadding).fillMaxSize()
        ) {
            items(items = plants) {
                PlantListItem(
                    plant = it,
                    onItemSelect = { plant ->
                        selectedPlant = plant
                        showReminderDialog = true
                    },
                    onDelete = { onDeletePlant(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showReminderDialog && selectedPlant != null) {
        ReminderDialogContent(
            onDialogDismiss = { showReminderDialog = false },
            plantName = selectedPlant!!.name,
            onScheduleReminder = onScheduleReminder
        )
    }

    if (showAddDialog) {
        AddPlantDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, desc, sched, uri ->
                onAddPlant(name, type, desc, sched, uri)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddPlantDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onAdd(name, type, description, "daily", imageUri) }) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") })
                
                Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
                    Text(if (imageUri == null) "Select Image" else "Image Selected")
                }
            }
        }
    )
}

@Composable
fun PlantListItem(
    plant: Plant,
    onItemSelect: (Plant) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier
        .clickable { onItemSelect(plant) }
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_medium))
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plant.name,
                    style = typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
            if (plant.imageUri != null) {
                AsyncImage(
                    model = Uri.parse(plant.imageUri),
                    contentDescription = plant.name,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Type", tint = MaterialTheme.colorScheme.secondary)
                Text(text = "   " + plant.type, style = typography.titleMedium)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Warning, contentDescription = "Description", tint = MaterialTheme.colorScheme.secondary)
                Text(text = "   " + plant.description, style = typography.titleMedium)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = "Schedule", tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "   ${stringResource(R.string.water)} ${plant.schedule}",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReminderDialogContent(
    onDialogDismiss: () -> Unit,
    plantName: String,
    onScheduleReminder: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders = listOf(
        Reminder(R.string.thirty_seconds, THIRTY_SECONDS, TimeUnit.SECONDS, plantName),
        Reminder(R.string.five_minutes, FIVE_MINUTES, TimeUnit.MINUTES, plantName),
        Reminder(R.string.ten_minutes, TEN_MINUTES, TimeUnit.MINUTES, plantName),
        Reminder(R.string.one_hour, ONE_HOUR, TimeUnit.HOURS, plantName),
        Reminder(R.string.one_week, SEVEN_DAYS, TimeUnit.DAYS, plantName),
        Reminder(R.string.two_weeks, FOURTEEN_DAYS, TimeUnit.DAYS, plantName),
        Reminder(R.string.one_month, THIRTY_DAYS, TimeUnit.DAYS, plantName)
    )

    AlertDialog(
        onDismissRequest = { onDialogDismiss() },
        containerColor = Color.White,
        titleContentColor = Color(0xFF2E7D32),
        textContentColor = Color(0xFF1B5E20),
        confirmButton = {},
        title = { Text(stringResource(R.string.remind_me, plantName), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                reminders.forEach {
                    Text(
                        text = stringResource(it.durationRes),
                        modifier = Modifier
                            .clickable {
                                onScheduleReminder(it)
                                onDialogDismiss()
                            }
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        modifier = modifier
    )
}
