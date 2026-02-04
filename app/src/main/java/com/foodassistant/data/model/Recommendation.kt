package com.foodassistant.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 推荐结果数据类
 */
@Parcelize
data class Recommendation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String,
    val cuisine: String,
    val reason: String,
    val confidence: Float = 0.85f,
    val estimatedPrice: Int? = null,
    val nutrition: NutritionInfo? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    companion object {
        fun createSample() = Recommendation(
            foodName = "麻婆豆腐",
            cuisine = "川菜",
            reason = "根据您喜欢川菜的偏好，推荐这道经典川菜。麻辣鲜香，非常下饭。",
            confidence = 0.92f,
            estimatedPrice = 28,
            nutrition = NutritionInfo(320, 15f, 25f, 18f)
        )
    }
}

@Parcelize
data class NutritionInfo(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
) : Parcelable {
    fun toDisplayString(): String {
        return "约${calories}卡 | 蛋白质${protein.toInt()}g | 碳水${carbs.toInt()}g | 脂肪${fat.toInt()}g"
    }
}

/**
 * 食物图片数据类
 */
@Parcelize
data class FoodImage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

/**
 * 用餐历史记录
 */
@Parcelize
data class FoodHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodName: String,
    val cuisine: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val liked: Boolean? = null,
    val notes: String? = null
) : Parcelable
