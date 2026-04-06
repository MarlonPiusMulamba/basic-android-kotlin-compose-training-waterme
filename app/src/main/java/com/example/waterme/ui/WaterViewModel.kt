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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.waterme.WaterMeApplication
import com.example.waterme.data.Reminder
import com.example.waterme.data.WaterRepository

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.waterme.model.Plant

class WaterViewModel(private val waterRepository: WaterRepository) : ViewModel() {

    val plants: StateFlow<List<Plant>> = waterRepository.plants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val defaults = listOf("Carrot", "Sukuma", "Beans", "Okra", "Strawberry")
            waterRepository.plants.first().forEach { plant ->
                if (defaults.contains(plant.name) && plant.imageUri == null) {
                    waterRepository.deletePlant(plant)
                }
            }
        }
    }

    fun addPlant(name: String, type: String, description: String, schedule: String, imageUri: String?) {
        viewModelScope.launch {
            waterRepository.addPlant(Plant(name = name, type = type, description = description, schedule = schedule, imageUri = imageUri))
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            waterRepository.deletePlant(plant)
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        waterRepository.scheduleReminder(reminder.duration, reminder.unit, reminder.plantName)
    }

    /**
     * Factory for [WaterViewModel] that takes [WaterRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val waterRepository =
                    (this[APPLICATION_KEY] as WaterMeApplication).container.waterRepository
                WaterViewModel(
                    waterRepository = waterRepository
                )
            }
        }
    }
}
