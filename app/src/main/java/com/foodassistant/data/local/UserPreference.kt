package com.foodassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 用户偏好实体 - Room 数据库表
 */
@Entity(
    tableName = "user_preferences",
    indices = [Index(value = ["updatedAt"], descending = true)]
)
data class UserPreference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 饮食偏好
    val favoriteCuisines: String = "",
    val dislikedFoods: String = "",
    val dietaryRestrictions: String = "",
    val spiceLevel: Int = 2,
    
    // 用餐习惯
    val preferredMealTimes: String = "",
    val portionSize: String = "medium",
    
    // 历史记录（JSON 格式）
    val recentFoods: String = "",
    val favoriteRestaurants: String = "",
    
    // 元数据
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SPICE_NONE = 1
        const val SPICE_MILD = 2
        const val SPICE_MEDIUM = 3
        const val SPICE_HOT = 4
        const val SPICE_EXTRA_HOT = 5
        
        const val PORTION_SMALL = "small"
        const val PORTION_MEDIUM = "medium"
        const val PORTION_LARGE = "large"
    }
    
    /**
     * 获取喜欢的菜系列表
     */
    fun getFavoriteCuisinesList(): List<String> {
        return favoriteCuisines.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    /**
     * 添加喜欢的菜系
     */
    fun addFavoriteCuisine(cuisine: String): UserPreference {
        val current = getFavoriteCuisinesList()
        return if (cuisine !in current) {
            copy(favoriteCuisines = (current + cuisine).joinToString(","))
        } else this
    }
    
    /**
     * 获取辣度文本描述
     */
    fun getSpiceLevelText(): String {
        return when (spiceLevel) {
            SPICE_NONE -> "不辣"
            SPICE_MILD -> "微辣"
            SPICE_MEDIUM -> "中辣"
            SPICE_HOT -> "较辣"
            SPICE_EXTRA_HOT -> "特辣"
            else -> "适中"
        }
    }
}

/**
 * 用餐历史实体
 */
@Entity(
    tableName = "food_history",
    indices = [
        Index(value = ["timestamp"], descending = true),
        Index(value = ["foodName"])
    ]
)
data class FoodHistoryEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String,
    val cuisine: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val liked: Boolean? = null,
    val notes: String? = null
)
