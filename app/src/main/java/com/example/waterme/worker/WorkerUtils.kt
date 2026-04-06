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

package com.example.waterme.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.waterme.MainActivity
import com.example.waterme.NOTIFICATION_ID
import com.example.waterme.NOTIFICATION_TITLE
import com.example.waterme.R
import com.example.waterme.REQUEST_CODE

const val CHANNEL_ID_ALARM = "ALARM_NOTIFICATION_CHANNEL"

fun makePlantReminderNotification(
    message: String,
    context: Context
) {
    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            CHANNEL_ID_ALARM,
            "Watering Alarms",
            importance
        )
        channel.description = "Rings loudly when plants need watering"
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        channel.setSound(alarmSound, audioAttributes)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    val pendingIntent: PendingIntent = createPendingIntent(context)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALARM)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setSound(alarmSound)
        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        .setFullScreenIntent(pendingIntent, true)
        .setAutoCancel(true)

    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    } catch(e: SecurityException) {
        // App lacks notification permission handling temporarily
        e.printStackTrace()
    }
}

fun createPendingIntent(appContext: Context): PendingIntent {
    val intent = Intent(appContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // Flag to detect unsafe launches of intents for Android 12 and higher
    // to improve platform security
    var flags = PendingIntent.FLAG_UPDATE_CURRENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flags = flags or PendingIntent.FLAG_IMMUTABLE
    }

    return PendingIntent.getActivity(
        appContext,
        REQUEST_CODE,
        intent,
        flags
    )
}
