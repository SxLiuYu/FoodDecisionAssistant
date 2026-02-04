# 模型下载脚本 (Windows PowerShell)
# 用法: .\download_models.ps1 [模型保存路径]

param(
    [string]$ModelDir = "./models"
)

$ErrorActionPreference = "Stop"

# 创建目录
New-Item -ItemType Directory -Force -Path $ModelDir | Out-Null

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🍜 FoodDecisionAssistant 模型下载脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Python
if (!(Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "错误: 未找到 Python，请先安装" -ForegroundColor Red
    exit 1
}

Write-Host "📋 检查依赖..." -ForegroundColor Yellow
Write-Host "✓ Python 已安装" -ForegroundColor Green
Write-Host ""

Write-Host "🎯 需要下载的模型:" -ForegroundColor Cyan
Write-Host "  1. Qwen2-VL-2B-Instruct (INT4量化版) - ~1.5GB" -ForegroundColor White
Write-Host "  2. Whisper Tiny (可选) - ~75MB" -ForegroundColor White
Write-Host ""

Write-Host "📥 下载方式选择:" -ForegroundColor Yellow
Write-Host "  [1] 使用 ModelScope Python SDK 下载 (国内推荐)"
Write-Host "  [2] 使用 HuggingFace Hub 下载"
Write-Host "  [3] 跳过下载，显示手动下载说明"
Write-Host ""

$choice = Read-Host "请选择 [1-3]"

switch ($choice) {
    "1" {
        Write-Host "正在安装/检查 ModelScope..." -ForegroundColor Yellow
        pip install modelscope -q
        
        Write-Host ""
        Write-Host "正在下载模型..." -ForegroundColor Cyan
        
        $pythonScript = @"
from modelscope import snapshot_download
import sys

try:
    print("📥 下载 Qwen2-VL-2B-Instruct...")
    path = snapshot_download(
        'qwen/Qwen2-VL-2B-Instruct',
        cache_dir='$ModelDir'
    )
    print(f"✓ 下载完成: {path}")
except Exception as e:
    print(f"✗ 下载失败: {e}")
    sys.exit(1)
"@
        
        python -c $pythonScript
    }
    
    "2" {
        Write-Host "正在安装/检查 HuggingFace Hub..." -ForegroundColor Yellow
        pip install huggingface-hub -q
        
        Write-Host ""
        Write-Host "正在下载模型..." -ForegroundColor Cyan
        
        huggingface-cli download Qwen/Qwen2-VL-2B-Instruct --local-dir "$ModelDir/qwen2-vl-2b"
    }
    
    "3" {
        Write-Host ""
        Write-Host "📖 手动下载说明:" -ForegroundColor Cyan
        Write-Host "================"
        Write-Host ""
        Write-Host "1. 访问以下网址之一:" -ForegroundColor White
        Write-Host "   - ModelScope: https://modelscope.cn/models/qwen/Qwen2-VL-2B-Instruct" -ForegroundColor Yellow
        Write-Host "   - HuggingFace: https://huggingface.co/Qwen/Qwen2-VL-2B-Instruct" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "2. 下载 INT4 量化版 MNN 模型文件" -ForegroundColor White
        Write-Host "   文件名通常为: Qwen2-VL-2B-Instruct-MNN-Int4.mnn" -ForegroundColor Gray
        Write-Host ""
        Write-Host "3. 将下载的文件放入: $ModelDir\" -ForegroundColor White
        Write-Host ""
        Write-Host "4. 如需从 PyTorch/ONNX 自行转换，请参考 docs/02-模型准备.md" -ForegroundColor Gray
    }
    
    default {
        Write-Host "无效选择" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "📁 当前模型目录内容:" -ForegroundColor Cyan
Get-ChildItem $ModelDir -ErrorAction SilentlyContinue | ForEach-Object {
    $size = if ($_.Length -gt 1GB) { "{0:N2} GB" -f ($_.Length / 1GB) }
            elseif ($_.Length -gt 1MB) { "{0:N2} MB" -f ($_.Length / 1MB) }
            else { "{0:N2} KB" -f ($_.Length / 1KB) }
    Write-Host "  $($_.Name) - $size" -ForegroundColor White
}
Write-Host "==========================================" -ForegroundColor Cyan
