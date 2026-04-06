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

package com.example.waterme.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.waterme.model.Plant
import com.example.waterme.worker.AlarmReceiver
import com.example.waterme.worker.WaterReminderWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class WorkManagerWaterRepository(private val context: Context, private val plantDao: PlantDao) : WaterRepository {
    
    override val plants: Flow<List<Plant>> = plantDao.getAllPlants()

    override suspend fun addPlant(plant: Plant) {
        plantDao.insertPlant(plant)
    }

    override suspend fun deletePlant(plant: Plant) {
        plantDao.deletePlant(plant)
    }

    override fun scheduleReminder(duration: Long, unit: TimeUnit, plantName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(WaterReminderWorker.nameKey, plantName)
        }
        
        // Ensure uniqueness for different plants
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plantName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + unit.toMillis(duration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
