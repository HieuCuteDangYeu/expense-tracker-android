package com.example.expensetracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ProjectDao
import com.example.expensetracker.data.network.SupabaseClient
import com.example.expensetracker.data.network.dto.SupabaseExpense
import com.example.expensetracker.data.network.dto.SupabaseProject
import io.github.jan.supabase.postgrest.from
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.expensetracker.data.network.SyncPreferencesManager
import com.example.expensetracker.worker.SyncWorker
import com.example.expensetracker.data.network.NetworkConnectivityObserver
import com.example.expensetracker.data.network.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class SyncHistoryEntry(
    val timestamp: String, val description: String, val status: String // "Success" or "Error"
)

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

class SyncViewModel(
    application: Application, private val projectDao: ProjectDao
) : AndroidViewModel(application) {

    private val networkObserver = NetworkConnectivityObserver(application)
    val networkStatus: StateFlow<NetworkStatus> = networkObserver.networkStatus

    private val sharedPreferences =
        application.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private val syncPrefsManager = SyncPreferencesManager(application)

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSynced = MutableStateFlow<String?>(
        sharedPreferences.getString("last_synced", null)
    )
    val lastSynced: StateFlow<String?> = _lastSynced.asStateFlow()

    private val _autoSyncWifi = MutableStateFlow(true)
    val autoSyncWifi: StateFlow<Boolean> = _autoSyncWifi.asStateFlow()

    init {
        viewModelScope.launch {
            syncPrefsManager.autoSyncWifiFlow.collect { isEnabled ->
                _autoSyncWifi.value = isEnabled
                toggleAutoSync(isEnabled)
            }
        }

        viewModelScope.launch {
            networkStatus.collect { status ->
                if (status == NetworkStatus.Available_WiFi && _autoSyncWifi.value) {
                    syncNow()
                }
            }
        }
    }

    private val gson = Gson()

    private fun loadHistory(): List<SyncHistoryEntry> {
        val json = sharedPreferences.getString("sync_history", null) ?: return emptyList()
        val type = object : TypeToken<List<SyncHistoryEntry>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(history: List<SyncHistoryEntry>) {
        sharedPreferences.edit().putString("sync_history", gson.toJson(history)).apply()
    }

    private val _syncHistory = MutableStateFlow<List<SyncHistoryEntry>>(loadHistory())
    val syncHistory: StateFlow<List<SyncHistoryEntry>> = _syncHistory.asStateFlow()

    fun syncNow() {
        if (_syncStatus.value == SyncStatus.SYNCING) return

        val workManager = WorkManager.getInstance(getApplication())
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueue(syncRequest)

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(syncRequest.id).collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> {
                        _syncStatus.value = SyncStatus.SYNCING
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val now = sdf.format(Date())
                        val statusText = "just now"

                        _lastSynced.value = statusText
                        sharedPreferences.edit().putString("last_synced", statusText).apply()

                        _syncStatus.value = SyncStatus.SUCCESS

                        val entry = SyncHistoryEntry(
                            timestamp = "Today, $now",
                            description = "Data synchronized successfully via background worker",
                            status = "Success"
                        )
                        val newHistory = listOf(entry) + _syncHistory.value
                        _syncHistory.value = newHistory
                        saveHistory(newHistory)

                        kotlinx.coroutines.delay(3000)
                        _syncStatus.value = SyncStatus.IDLE
                    }

                    WorkInfo.State.FAILED -> {
                        val errorMsg =
                            workInfo.outputData.getString("error") ?: "Background sync failed"
                        handleSyncError(errorMsg)
                        kotlinx.coroutines.delay(3000)
                        _syncStatus.value = SyncStatus.IDLE
                    }

                    else -> {}
                }
            }
        }
    }

    private fun handleSyncError(message: String) {
        _syncStatus.value = SyncStatus.ERROR
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val now = sdf.format(Date())
        val entry = SyncHistoryEntry(
            timestamp = "Today, $now", description = message, status = "Error"
        )
        val newHistory = listOf(entry) + _syncHistory.value
        _syncHistory.value = newHistory
        saveHistory(newHistory)
    }

    fun toggleAutoSyncWifi() {
        viewModelScope.launch {
            val newValue = !_autoSyncWifi.value
            syncPrefsManager.setAutoSyncWifi(newValue)
            toggleAutoSync(newValue)
        }
    }

    private fun toggleAutoSync(isEnabled: Boolean) {
        val workManager = WorkManager.getInstance(getApplication())
        if (isEnabled) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()

            val syncRequest =
                PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).setConstraints(
                    constraints
                ).build()

            workManager.enqueueUniquePeriodicWork(
                "AutoSyncWork", ExistingPeriodicWorkPolicy.KEEP, syncRequest
            )
        } else {
            workManager.cancelUniqueWork("AutoSyncWork")
        }
    }
}

class SyncViewModelFactory(
    private val application: Application, private val projectDao: ProjectDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return SyncViewModel(application, projectDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
