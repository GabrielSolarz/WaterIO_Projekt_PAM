package com.example.waterio.data

import androidx.room.*
import java.util.UUID

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeletedLocally: Boolean = false // Dla obsługi usuwania w trybie offline
)

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE isDeletedLocally = 0 ORDER BY timestamp DESC")
    suspend fun getAllEntries(): List<WaterEntry>

    @Query("SELECT * FROM water_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<WaterEntry>

    @Query("UPDATE water_entries SET isDeletedLocally = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: String)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deletePermanently(id: String)
}

@Database(entities = [WaterEntry::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
}