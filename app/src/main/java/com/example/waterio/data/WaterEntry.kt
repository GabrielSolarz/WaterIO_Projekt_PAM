package com.example.waterio.data

import androidx.room.*

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(entry: WaterEntry)

    @Query("SELECT SUM(amountMl) FROM water_entries")
    suspend fun getTodayTotal(): Int?
}

@Database(entities = [WaterEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
}