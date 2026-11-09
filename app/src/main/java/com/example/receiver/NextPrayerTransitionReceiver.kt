package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.NextPrayerTracker

class NextPrayerTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("NextPrayerTransition", "Transition alarm triggered! Synchronizing all interfaces to next prayer...")
        NextPrayerTracker.syncAll(context)
    }
}
