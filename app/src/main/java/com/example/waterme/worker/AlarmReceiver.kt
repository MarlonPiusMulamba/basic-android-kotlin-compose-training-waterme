package com.example.waterme.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val plantName = intent.getStringExtra(WaterReminderWorker.nameKey) ?: "unknown plant"
        makePlantReminderNotification(
            message = "Time to water your $plantName!",
            context = context
        )
    }
}
