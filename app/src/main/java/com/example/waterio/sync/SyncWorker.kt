package com.example.waterio.sync

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterio.data.AppDatabase
import com.example.waterio.data.TokenManager
import com.example.waterio.network.WaterApiService
import com.example.waterio.network.WaterNetworkEntry
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val tokenManager = TokenManager(applicationContext)
        val token = tokenManager.getToken() ?: return Result.failure()
        val bearerToken = "Bearer $token"

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "water-database")
            .fallbackToDestructiveMigration()
            .build()
        val dao = db.waterDao()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(WaterApiService::class.java)

        return try {
            val unsynced = dao.getUnsyncedEntries()
            Log.d("WaterSync", "Found ${unsynced.size} unsynced entries")
            
            unsynced.forEach { entry ->
                if (entry.isDeletedLocally) {
                    api.deleteWater(bearerToken, entry.id)
                    dao.deletePermanently(entry.id)
                } else {
                    val result = api.addWater(bearerToken, WaterNetworkEntry(entry.id, entry.amountMl, entry.timestamp))
                    // Usuwamy stary wpis i wstawiamy nowy z ID z serwera (jeśli się różnią)
                    if (result.id != null && result.id != entry.id) {
                        dao.deletePermanently(entry.id)
                        dao.insert(entry.copy(id = result.id, isSynced = true))
                    } else {
                        dao.insert(entry.copy(isSynced = true))
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("WaterSync", "Sync failed: ${e.message}", e)
            Result.retry()
        } finally {
            db.close()
        }
    }
}