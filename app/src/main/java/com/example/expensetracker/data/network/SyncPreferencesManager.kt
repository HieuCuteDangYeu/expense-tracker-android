package com.example.expensetracker.data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

class SyncPreferencesManager(private val context: Context) {
    companion object {
        val AUTO_SYNC_WIFI = booleanPreferencesKey("auto_sync_wifi")
    }

    val autoSyncWifiFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_SYNC_WIFI] ?: true // Default to true
        }

    suspend fun setAutoSyncWifi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SYNC_WIFI] = enabled
        }
    }
}
