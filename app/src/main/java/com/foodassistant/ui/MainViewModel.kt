package com.foodassistant.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodassistant.data.local.AppDatabase
import com.foodassistant.data.local.FoodHistoryEntity
import com.foodassistant.data.local.UserPreference
import com.foodassistant.data.model.Recommendation
import com.foodassistant.mnn.MNNInferenceManager
import com.foodassistant.prompt.PromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 主界面 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "MainViewModel"
    }
    
    // 依赖
    private val inferenceManager = MNNInferenceManager(application)
    private val promptBuilder = PromptBuilder(application)
    private val database = AppDatabase.getDatabase(application)
    
    // UI 状态
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // 推荐结果
    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()
    
    // 模型状态
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady.asStateFlow()
    
    // 用户偏好
    private val _userPreference = MutableStateFlow<UserPreference?>(null)
    val userPreference: StateFlow<UserPreference?> = _userPreference.asStateFlow()
    
    // 图片
    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage.asStateFlow()
    
    init {
        viewModelScope.launch {
            // 加载用户偏好
            _userPreference.value = database.preferenceDao().getPreference()
            
            // 初始化推理引擎（模拟模式）
            initializeModel()
        }
    }
    
    /**
     * 初始化模型
     */
    private suspend fun initializeModel() {
        _uiState.value = UiState.Loading("正在准备AI模型…")
        
        val success = withContext(Dispatchers.Default) {
            inferenceManager.initialize()
        }
        
        _isModelReady.value = success
        _uiState.value = if (success) UiState.Ready else UiState.Error("模型初始化失败")
    }
    
    /**
     * 设置选中图片
     */
    fun setSelectedImage(bitmap: Bitmap?) {
        _selectedImage.value = bitmap
    }
    
    /**
     * 获取餐食推荐
     */
    fun getRecommendation(textQuery: String = "") {
        if (!_isModelReady.value) {
            _uiState.value = UiState.Error("模型未就绪")
            return
        }
        
        val image = _selectedImage.value
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading("正在分析…")
            
            try {
                // 1. 获取最近用餐记录
                val recentFoods = database.historyDao().getRecentFoods(10).map { it.foodName }
                
                // 2. 构建提示词
                val prompt = promptBuilder.buildPrompt(
                    imageDescription = null, // 可以先用模型描述图片
                    userQuery = textQuery.takeIf { it.isNotBlank() },
                    preferences = _userPreference.value,
                    recentFoods = recentFoods
                )
                
                // 3. 执行推理
                _uiState.value = UiState.Loading("正在生成推荐…")
                val response = withContext(Dispatchers.Default) {
                    inferenceManager.inference(image, prompt)
                }
                
                // 4. 解析结果
                val parseResult = promptBuilder.parseRecommendation(response)
                
                when (parseResult) {
                    is PromptBuilder.RecommendationParseResult.Success -> {
                        val recommendation = Recommendation(
                            foodName = parseResult.foodName,
                            cuisine = parseResult.cuisine,
                            reason = parseResult.reason,
                            confidence = 0.85f
                        )
                        
                        _recommendations.value = listOf(recommendation)
                        
                        // 5. 保存到历史
                        saveToHistory(recommendation, image)
                        
                        _uiState.value = UiState.Success
                    }
                    is PromptBuilder.RecommendationParseResult.Failure -> {
                        _uiState.value = UiState.Error(parseResult.error)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Recommendation failed", e)
                _uiState.value = UiState.Error("推理出错：${e.message}")
            }
        }
    }
    
    /**
     * 快速推荐（无输入）
     */
    fun quickRecommend() {
        val timeContext = getTimeContext()
        getRecommendation(timeContext)
    }
    
    /**
     * 喜欢推荐
     */
    fun likeRecommendation(recommendation: Recommendation) {
        viewModelScope.launch {
            database.historyDao().updateFeedback(recommendation.id, liked = true)
            
            // 更新偏好
            updatePreference { pref ->
                pref.addFavoriteCuisine(recommendation.cuisine)
                    .copy(updatedAt = System.currentTimeMillis())
            }
        }
    }
    
    /**
     * 不喜欢推荐
     */
    fun dislikeRecommendation(recommendation: Recommendation) {
        viewModelScope.launch {
            database.historyDao().updateFeedback(recommendation.id, liked = false)
        }
    }
    
    /**
     * 更新用户偏好
     */
    fun updatePreference(update: (UserPreference) -> UserPreference) {
        viewModelScope.launch {
            val current = _userPreference.value ?: UserPreference()
            val updated = update(current)
            database.preferenceDao().insert(updated)
            _userPreference.value = updated
        }
    }
    
    /**
     * 清除当前推荐
     */
    fun clearRecommendations() {
        _recommendations.value = emptyList()
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        inferenceManager.release()
    }
    
    private suspend fun saveToHistory(recommendation: Recommendation, image: Bitmap?) {
        val history = FoodHistoryEntity(
            id = recommendation.id,
            foodName = recommendation.foodName,
            cuisine = recommendation.cuisine,
            timestamp = System.currentTimeMillis()
        )
        database.historyDao().insert(history)
    }
    
    private fun getTimeContext(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..10 -> "请为我推荐早餐"
            in 11..14 -> "请为我推荐午餐"
            in 17..21 -> "请为我推荐晚餐"
            else -> "请为我推荐夜宵"
        }
    }
    
    // UI 状态定义
    sealed class UiState {
        object Idle : UiState()
        data class Loading(val message: String) : UiState()
        object Ready : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
