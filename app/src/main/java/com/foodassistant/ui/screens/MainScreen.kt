package com.foodassistant.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.foodassistant.data.model.Recommendation
import com.foodassistant.ui.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val isModelReady by viewModel.isModelReady.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    
    var textQuery by remember { mutableStateOf("") }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    
    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            viewModel.setSelectedImage(bitmap)
        }
    }
    
    // 相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            val bitmap = loadBitmapFromUri(context, tempPhotoUri!!)
            viewModel.setSelectedImage(bitmap)
        }
    }
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 创建临时文件 URI
            val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri?.let { cameraLauncher.launch(it) }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "餐食决策助手",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (isModelReady && recommendations.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.quickRecommend() },
                    icon = { Icon(Icons.Default.Restaurant, null) },
                    text = { Text("帮我选") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 状态显示区域
            when (val state = uiState) {
                is MainViewModel.UiState.Loading -> {
                    item {
                        LoadingCard(message = state.message)
                    }
                }
                is MainViewModel.UiState.Error -> {
                    item {
                        ErrorCard(message = state.message) {
                            viewModel.getRecommendation(textQuery)
                        }
                    }
                }
                else -> {}
            }
            
            // 图片输入区域
            item {
                ImageInputCard(
                    image = selectedImage,
                    onCameraClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                                tempPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                tempPhotoUri?.let { cameraLauncher.launch(it) }
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onClearClick = { viewModel.setSelectedImage(null) }
                )
            }
            
            // 文字输入
            item {
                OutlinedTextField(
                    value = textQuery,
                    onValueChange = { textQuery = it },
                    label = { Text("补充描述（可选）") },
                    placeholder = { Text("比如：想吃清淡一点的") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // 操作按钮
            item {
                ActionButtons(
                    isEnabled = isModelReady,
                    onRecommendClick = { viewModel.getRecommendation(textQuery) },
                    onQuickClick = { viewModel.quickRecommend() }
                )
            }
            
            // 推荐结果
            items(recommendations) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onLike = { viewModel.likeRecommendation(it) },
                    onDislike = { viewModel.dislikeRecommendation(it) }
                )
            }
            
            // 底部留白
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageInputCard(
    image: Bitmap?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* 点击卡片 */ },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "已选图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // 清除按钮
                FilledIconButton(
                    onClick = onClearClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(Icons.Default.Close, "清除")
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击拍照或选择图片",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onCameraClick) {
                            Icon(Icons.Default.CameraAlt, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("拍照")
                        }
                        OutlinedButton(onClick = onGalleryClick) {
                            Icon(Icons.Default.PhotoLibrary, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("相册")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isEnabled: Boolean,
    onRecommendClick: () -> Unit,
    onQuickClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onQuickClick,
            modifier = Modifier.weight(1f),
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("随机推荐")
        }
        
        Button(
            onClick = onRecommendClick,
            modifier = Modifier.weight(1.5f),
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("获取推荐")
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    onLike: (Recommendation) -> Unit,
    onDislike: (Recommendation) -> Unit
) {
    var isLiked by remember { mutableStateOf<Boolean?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.foodName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            recommendation.cuisine,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 推荐理由
            Text(
                "推荐理由",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 营养信息
            if (recommendation.nutrition != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "营养信息",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    recommendation.nutrition.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 价格
            if (recommendation.estimatedPrice != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Text(
                        "参考价格：",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "约${recommendation.estimatedPrice}元",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 反馈按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { 
                        isLiked = false
                        onDislike(recommendation) 
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isLiked == false) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        if (isLiked == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                        null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("不喜欢")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { 
                        isLiked = true
                        onLike(recommendation) 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLiked == true) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isLiked == true) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        if (isLiked == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("喜欢")
                }
            }
        }
    }
}

// 辅助函数
private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
