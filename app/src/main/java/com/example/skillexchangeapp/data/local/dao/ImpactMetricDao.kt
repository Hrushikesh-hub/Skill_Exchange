package com.example.skillexchangeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.skillexchangeapp.data.local.entity.ImpactMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface ImpactMetricDao {
    @Insert
    suspend fun insertMetric(metric: ImpactMetric): Long

    @Query("SELECT * FROM impact_metrics ORDER BY generatedAt DESC LIMIT 1")
    fun getLatestMetric(): Flow<ImpactMetric?>
}
