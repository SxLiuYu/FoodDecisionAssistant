package com.foodassistant.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户偏好 DAO
 */
@Dao
interface PreferenceDao {
    
    @Query("SELECT * FROM user_preferences LIMIT 1")
    suspend fun getPreference(): UserPreference?
    
    @Query("SELECT * FROM user_preferences LIMIT 1")
    fun getPreferenceFlow(): Flow<UserPreference?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: UserPreference): Long
    
    @Update
    suspend fun update(preference: UserPreference)
    
    @Query("UPDATE user_preferences SET favoriteCuisines = :cuisines, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateCuisines(id: Long, cuisines: String, timestamp: Long)
    
    @Query("UPDATE user_preferences SET dislikedFoods = :foods, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateDislikedFoods(id: Long, foods: String, timestamp: Long)
    
    @Query("UPDATE user_preferences SET spiceLevel = :level, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSpiceLevel(id: Long, level: Int, timestamp: Long)
}

/**
 * 用餐历史 DAO
 */
@Dao
interface FoodHistoryDao {
    
    @Query("SELECT * FROM food_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentFoods(limit: Int): List<FoodHistoryEntity>
    
    @Query("SELECT * FROM food_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFoodsFlow(limit: Int): Flow<List<FoodHistoryEntity>>
    
    @Query("SELECT * FROM food_history WHERE liked = 1 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLikedFoods(limit: Int): List<FoodHistoryEntity>
    
    @Insert
    suspend fun insert(history: FoodHistoryEntity)
    
    @Query("UPDATE food_history SET liked = :liked WHERE id = :id")
    suspend fun updateFeedback(id: String, liked: Boolean)
    
    @Delete
    suspend fun delete(history: FoodHistoryEntity)
    
    @Query("DELETE FROM food_history WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM food_history")
    suspend fun getCount(): Int
}
