package com.pam.waterio.data

import androidx.room.*
import java.util.UUID

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeletedLocally: Boolean = false,
    val userEmail: String = "" // Powiązanie wpisu z konkretnym użytkownikiem
)

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE userEmail = :email AND isDeletedLocally = 0 ORDER BY timestamp DESC")
    suspend fun getAllEntries(email: String): List<WaterEntry>

    @Query("SELECT * FROM water_entries WHERE userEmail = :email AND isSynced = 0")
    suspend fun getUnsyncedEntries(email: String): List<WaterEntry>

    @Query("UPDATE water_entries SET isDeletedLocally = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: String)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deletePermanently(id: String)
}

@Database(entities = [WaterEntry::class], version = 3) // Podniesienie wersji bazy danych
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
}
