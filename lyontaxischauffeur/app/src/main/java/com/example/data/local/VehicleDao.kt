package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicle_profile WHERE id = 1 LIMIT 1")
    fun getVehicleProfile(): Flow<VehicleProfileEntity?>

    @Query("SELECT * FROM vehicle_profile WHERE id = 1 LIMIT 1")
    suspend fun getVehicleProfileOnce(): VehicleProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(vehicle: VehicleProfileEntity)

    @Update
    suspend fun update(vehicle: VehicleProfileEntity)

    @Query("DELETE FROM vehicle_profile")
    suspend fun deleteAll()
}
