package com.example.waterio.sync

import android.content.Context
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

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "water-database").build()
        val dao = db.waterDao()

        val retrofit = Retrofit.Builder()
            // 10.0.2.2 to adres IP komputera hosta widoczny z poziomu emulatora Androida
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(WaterApiService::class.java)

        try {
            // Zsynchronizuj nieprzysłane wpisy lokalne do API
            val unsynced = dao.getUnsyncedEntries()
            unsynced.forEach { entry ->
                if (entry.isDeletedLocally) {
                    api.deleteWater(bearerToken, entry.id)
                    dao.deletePermanently(entry.id)
                } else {
                    api.addWater(bearerToken, WaterNetworkEntry(entry.id, entry.amountMl, entry.timestamp))
                    dao.insert(entry.copy(isSynced = true))
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}