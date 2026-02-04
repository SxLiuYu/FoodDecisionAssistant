package com.foodassistant.prompt

import android.content.Context
import android.util.Log
import com.foodassistant.data.local.UserPreference
import java.util.Calendar

/**
 * 提示词构建器
 * 负责构建给 AI 模型的提示词
 */
class PromptBuilder(private val context: Context) {
    
    companion object {
        private const val TAG = "PromptBuilder"
        
        // 系统角色设定
        private const val SYSTEM_PROMPT = """你是一位专业的餐食推荐助手，精通各种菜系和营养搭配。
请根据用户的偏好和当前情况，给出贴心的餐食建议。"""
    }
    
    /**
     * 构建完整提示词
     */
    fun buildPrompt(
        imageDescription: String? = null,
        userQuery: String? = null,
        preferences: UserPreference? = null,
        recentFoods: List<String> = emptyList()
    ): String {
        val sb = StringBuilder()
        
        // 系统角色
        sb.appendLine(SYSTEM_PROMPT)
        sb.appendLine()
        
        // 用户偏好
        sb.appendLine("## 用户饮食偏好")
        sb.appendLine(buildPreferenceSection(preferences))
        sb.appendLine()
        
        // 最近用餐
        sb.appendLine("## 最近用餐记录")
        sb.appendLine(buildRecentFoodsSection(recentFoods))
        sb.appendLine()
        
        // 当前场景
        sb.appendLine("## 当前场景")
        sb.appendLine(buildContextSection(imageDescription, userQuery))
        sb.appendLine()
        
        // 输出要求
        sb.appendLine(buildOutputRequirements())
        
        val prompt = sb.toString()
        Log.d(TAG, "Built prompt length: ${prompt.length}")
        return prompt
    }
    
    /**
     * 构建偏好部分
     */
    private fun buildPreferenceSection(pref: UserPreference?): String {
        if (pref == null) return "- 暂无记录，请根据大众口味推荐"
        
        return buildString {
            appendLine("- 喜欢的菜系：${pref.favoriteCuisines.ifEmpty { "无特别偏好" }}")
            appendLine("- 不喜欢的食物：${pref.dislikedFoods.ifEmpty { "无" }}")
            appendLine("- 饮食限制：${pref.dietaryRestrictions.ifEmpty { "无" }}")
            appendLine("- 辣度偏好：${pref.getSpiceLevelText()}")
            appendLine("- 分量偏好：${pref.portionSize}")
        }.trimEnd()
    }
    
    /**
     * 构建最近用餐部分
     */
    private fun buildRecentFoodsSection(foods: List<String>): String {
        if (foods.isEmpty()) return "- 无记录"
        
        return buildString {
            foods.take(5).forEachIndexed { index, food ->
                appendLine("${index + 1}. $food")
            }
            if (foods.size > 5) {
                appendLine("... 还有 ${foods.size - 5} 条记录")
            }
            appendLine("（建议推荐不同种类以获得营养均衡）")
        }.trimEnd()
    }
    
    /**
     * 构建当前场景部分
     */
    private fun buildContextSection(
        imageDescription: String?,
        userQuery: String?
    ): String {
        val timeContext = getTimeContext()
        
        return buildString {
            appendLine("- 当前时间：$timeContext")
            
            if (imageDescription != null) {
                appendLine("- 图片内容：$imageDescription")
            }
            if (userQuery != null && userQuery.isNotBlank()) {
                appendLine("- 用户补充：\"$userQuery\"")
            }
            if (imageDescription == null && (userQuery == null || userQuery.isBlank())) {
                appendLine("- 用户未提供具体输入，请根据时间和偏好主动推荐")
            }
        }.trimEnd()
    }
    
    /**
     * 构建输出要求
     */
    private fun buildOutputRequirements(): String {
        return """## 推荐要求
请提供以下格式的推荐：

【推荐菜品】菜品名称（包含中文名）
【所属菜系】菜系名称
【推荐理由】2-3句话说明为什么推荐这道菜，结合用户偏好
【营养信息】预估卡路里和主要营养成分
【参考价格】预估价格区间（如有把握）

注意：
1. 考虑用户的口味偏好和饮食限制
2. 避免推荐用户最近吃过的类似食物
3. 如果用户上传了图片，优先考虑与图片相关的菜品或类似风格
4. 语气要亲切自然，像朋友给建议
5. 菜品要具体，不要给出笼统的类别"""
    }
    
    /**
     * 获取当前时间上下文
     */
    private fun getTimeContext(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val mealType = when (hour) {
            in 6..10 -> "早餐时间"
            in 11..14 -> "午餐时间"
            in 15..16 -> "下午茶时间"
            in 17..21 -> "晚餐时间"
            in 22..23, in 0..5 -> "夜宵时间"
            else -> "用餐时间"
        }
        return "$mealType (${hour}:00)"
    }
    
    /**
     * 从 AI 响应中解析推荐结果
     */
    fun parseRecommendation(response: String): RecommendationParseResult {
        Log.d(TAG, "Parsing response: ${response.take(200)}...")
        
        return try {
            val foodName = extractField(response, "推荐菜品")
            val cuisine = extractField(response, "所属菜系")
            val reason = extractField(response, "推荐理由")
            val nutrition = extractField(response, "营养信息")
            val price = extractField(response, "参考价格")
            
            if (foodName != null) {
                RecommendationParseResult.Success(
                    foodName = foodName.trim(),
                    cuisine = cuisine?.trim() ?: "未知",
                    reason = reason?.trim() ?: "",
                    nutrition = nutrition,
                    price = price
                )
            } else {
                RecommendationParseResult.Failure("无法解析推荐菜品")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
            RecommendationParseResult.Failure(e.message ?: "解析失败")
        }
    }
    
    /**
     * 提取字段
     */
    private fun extractField(text: String, fieldName: String): String? {
        // 支持多种格式：【字段】或 字段：
        val patterns = listOf(
            "【$fieldName】(.+?)(?=【|\z)".toRegex(RegexOption.DOT_MATCHES_ALL),
            "$fieldName[:：](.+?)(?=\n|\z)".toRegex(RegexOption.DOT_MATCHES_ALL)
        )
        
        for (pattern in patterns) {
            pattern.find(text)?.let {
                return it.groupValues[1].trim()
            }
        }
        return null
    }
    
    /**
     * 解析结果密封类
     */
    sealed class RecommendationParseResult {
        data class Success(
            val foodName: String,
            val cuisine: String,
            val reason: String,
            val nutrition: String? = null,
            val price: String? = null
        ) : RecommendationParseResult()
        
        data class Failure(val error: String) : RecommendationParseResult()
    }
}
