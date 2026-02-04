package com.foodassistant.mnn

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File

/**
 * MNN 推理管理器
 * 
 * 注意：这是一个适配器类，实际 MNN 推理需要集成 libMNN.so 库
 * 当前实现支持两种模式：
 * 1. MOCK_MODE: 模拟推理（无需 MNN 库，用于演示）
 * 2. REAL_MODE: 真实 MNN 推理（需要集成 MNN 库）
 */
class MNNInferenceManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MNNInference"
        private const val MODEL_NAME = "Qwen2-VL-2B-Int4.mnn"
        private const val MODEL_SIZE = 1572864000L // 1.5GB
        
        // 模式切换：true = 使用模拟，false = 使用真实 MNN（需要库）
        const val MOCK_MODE = true
    }
    
    private var isInitialized = false
    private var mockInference: MockInference? = null
    
    /**
     * 检查模型文件是否存在
     */
    fun isModelAvailable(): Boolean {
        val modelFile = File(context.filesDir, "models/$MODEL_NAME")
        return modelFile.exists() && modelFile.length() > 1000000000 // 至少 1GB
    }
    
    /**
     * 获取模型文件路径
     */
    fun getModelPath(): String {
        return File(context.filesDir, "models/$MODEL_NAME").absolutePath
    }
    
    /**
     * 初始化推理引擎
     */
    fun initialize(): Boolean {
        if (isInitialized) return true
        
        return try {
            if (MOCK_MODE) {
                // 模拟模式：不需要真实模型文件
                Log.i(TAG, "Initializing in MOCK mode")
                mockInference = MockInference()
                isInitialized = true
                true
            } else {
                // 真实 MNN 模式
                if (!isModelAvailable()) {
                    Log.e(TAG, "Model file not found")
                    return false
                }
                
                // TODO: 在这里初始化真实的 MNN Interpreter
                // 需要集成 libMNN.so 库
                // interpreter = Interpreter(getModelPath())
                
                Log.i(TAG, "Initializing with real MNN")
                isInitialized = true
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
            false
        }
    }
    
    /**
     * 执行推理
     */
    fun inference(image: Bitmap?, prompt: String): String {
        if (!isInitialized) {
            throw IllegalStateException("Inference manager not initialized")
        }
        
        return if (MOCK_MODE) {
            mockInference?.inference(image, prompt) ?: "模拟推理失败"
        } else {
            // TODO: 调用真实 MNN 推理
            // return realMNNInference(image, prompt)
            "真实推理未实现"
        }
    }
    
    /**
     * 描述图片内容（用于图片上传场景）
     */
    fun describeImage(image: Bitmap): String {
        val prompt = "描述这张图片中的食物："
        return inference(image, prompt)
    }
    
    /**
     * 释放资源
     */
    fun release() {
        mockInference = null
        isInitialized = false
        Log.i(TAG, "Released")
    }
    
    /**
     * 获取模型下载 URL
     */
    fun getModelDownloadUrl(): String {
        // 替换为实际的模型下载地址
        return "https://modelscope.cn/models/qwen/Qwen2-VL-2B-Instruct/files"
    }
    
    /**
     * 获取模型大小（MB）
     */
    fun getModelSizeMB(): Int {
        return (MODEL_SIZE / 1024 / 1024).toInt()
    }
}

/**
 * 模拟推理实现
 * 用于开发和演示，无需真实模型
 */
private class MockInference {
    
    private val sampleFoods = listOf(
        FoodRecommendation(
            name = "麻婆豆腐",
            cuisine = "川菜",
            reason = "经典川菜代表，麻辣鲜香，口感嫩滑。豆腐营养丰富，搭配肉末更有层次感。",
            price = "18-28元",
            nutrition = "约280卡 | 蛋白质18g | 碳水12g | 脂肪16g"
        ),
        FoodRecommendation(
            name = "清蒸鲈鱼",
            cuisine = "粤菜",
            reason = "清淡健康，鱼肉鲜嫩，富含优质蛋白和Omega-3脂肪酸。蒸制保留了食材原味。",
            price = "48-68元",
            nutrition = "约180卡 | 蛋白质35g | 碳水2g | 脂肪6g"
        ),
        FoodRecommendation(
            name = "番茄鸡蛋面",
            cuisine = "家常菜",
            reason = "简单美味，营养均衡。番茄富含维生素C，鸡蛋提供优质蛋白，面条提供能量。",
            price = "12-20元",
            nutrition = "约420卡 | 蛋白质16g | 碳水58g | 脂肪12g"
        ),
        FoodRecommendation(
            name = "宫保鸡丁",
            cuisine = "川菜",
            reason = "鸡肉嫩滑，花生酥脆，酸甜微辣。是一道开胃下饭的经典菜肴。",
            price = "28-38元",
            nutrition = "约320卡 | 蛋白质26g | 碳水18g | 脂肪15g"
        ),
        FoodRecommendation(
            name = "白灼虾",
            cuisine = "粤菜",
            reason = "原汁原味，虾肉鲜甜Q弹。低脂高蛋白，非常适合健康饮食。",
            price = "58-88元",
            nutrition = "约150卡 | 蛋白质30g | 碳水1g | 脂肪2g"
        ),
        FoodRecommendation(
            name = "牛肉面",
            cuisine = "西北菜",
            reason = "汤头浓郁，牛肉软烂，面条劲道。冬日暖身佳品，营养丰富。",
            price = "20-35元",
            nutrition = "约480卡 | 蛋白质22g | 碳水65g | 脂肪14g"
        )
    )
    
    private var lastRecommendation: FoodRecommendation? = null
    
    fun inference(image: Bitmap?, prompt: String): String {
        // 模拟推理延迟
        Thread.sleep(1500)
        
        // 根据 prompt 选择合适的推荐
        val recommendation = selectRecommendation(prompt)
        lastRecommendation = recommendation
        
        return buildResponse(recommendation)
    }
    
    private fun selectRecommendation(prompt: String): FoodRecommendation {
        // 简单的关键词匹配
        return when {
            prompt.contains("清淡") || prompt.contains("健康") || prompt.contains("减肥") ->
                sampleFoods.find { it.name == "清蒸鲈鱼" || it.name == "白灼虾" } ?: sampleFoods[1]
            
            prompt.contains("辣") || prompt.contains("川") || prompt.contains("麻辣") ->
                sampleFoods.find { it.cuisine == "川菜" } ?: sampleFoods[0]
            
            prompt.contains("简单") || prompt.contains("快") || prompt.contains("家常") ->
                sampleFoods.find { it.cuisine == "家常菜" } ?: sampleFoods[2]
            
            prompt.contains("牛肉") || prompt.contains("面") ->
                sampleFoods.find { it.name.contains("牛肉") } ?: sampleFoods[5]
            
            else -> sampleFoods.random()
        }
    }
    
    private fun buildResponse(food: FoodRecommendation): String {
        return """
【推荐菜品】${food.name}
【所属菜系】${food.cuisine}
【推荐理由】${food.reason}
【营养信息】${food.nutrition}
【参考价格】${food.price}
        """.trimIndent()
    }
    
    data class FoodRecommendation(
        val name: String,
        val cuisine: String,
        val reason: String,
        val price: String,
        val nutrition: String
    )
}
