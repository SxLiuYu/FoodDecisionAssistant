#!/bin/bash
# 模型下载脚本
# 用法: ./download_models.sh [模型保存路径]

set -e

MODEL_DIR="${1:-./models}"
mkdir -p "$MODEL_DIR"

echo "=========================================="
echo "🍜 FoodDecisionAssistant 模型下载脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查依赖
check_dependency() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}错误: 未找到 $1，请先安装${NC}"
        exit 1
    fi
}

echo "📋 检查依赖..."
check_dependency wget || check_dependency curl
check_dependency python3
echo -e "${GREEN}✓ 依赖检查通过${NC}"
echo ""

# 模型信息
QWEN_MODEL="Qwen2-VL-2B-Instruct"
QWEN_SIZE="~1.5GB"
QWEN_URLS=(
    "https://modelscope.cn/models/qwen/Qwen2-VL-2B-Instruct/files"
    "https://huggingface.co/Qwen/Qwen2-VL-2B-Instruct"
)

echo "🎯 需要下载的模型:"
echo "  1. $QWEN_MODEL (INT4量化版) - $QWEN_SIZE"
echo "  2. Whisper Tiny (可选) - ~75MB"
echo ""

echo "📥 下载方式选择:"
echo "  [1] 从 ModelScope 下载 (国内推荐)"
echo "  [2] 从 HuggingFace 下载"
echo "  [3] 使用 ModelScope Python SDK 下载"
echo "  [4] 跳过下载，显示手动下载说明"
echo ""
read -p "请选择 [1-4]: " choice

case $choice in
    1)
        echo "正在从 ModelScope 下载..."
        echo "模型将保存到: $MODEL_DIR"
        echo ""
        echo -e "${YELLOW}提示: ModelScope 需要登录令牌${NC}"
        echo "如果没有令牌，请访问 https://modelscope.cn 获取"
        read -p "请输入 ModelScope Token (直接回车跳过): " token
        
        if [ -n "$token" ]; then
            export MODELSCOPE_API_TOKEN="$token"
        fi
        
        # 使用 Python SDK 下载
        python3 << EOF
from modelscope import snapshot_download
import os

token = os.environ.get('MODELSCOPE_API_TOKEN')
model_dir = "$MODEL_DIR"

try:
    print("正在下载 Qwen2-VL-2B-Instruct...")
    downloaded_path = snapshot_download(
        'qwen/Qwen2-VL-2B-Instruct',
        cache_dir=model_dir,
        local_files_only=False
    )
    print(f"下载完成: {downloaded_path}")
except Exception as e:
    print(f"下载失败: {e}")
    exit(1)
EOF
        ;;
        
    2)
        echo "正在从 HuggingFace 下载..."
        echo -e "${YELLOW}注意: HuggingFace 可能需要代理${NC}"
        
        # 使用 huggingface-cli 或 wget
        if command -v huggingface-cli &> /dev/null; then
            huggingface-cli download Qwen/Qwen2-VL-2B-Instruct --local-dir "$MODEL_DIR/qwen2-vl-2b"
        else
            echo "请安装 huggingface-cli: pip install huggingface-hub"
            echo "或使用浏览器手动下载"
        fi
        ;;
        
    3)
        echo "使用 Python SDK 下载..."
        
        # 检查并安装依赖
        pip3 install modelscope -q
        
        python3 << 'EOF'
from modelscope import snapshot_download
import sys

model_dir = sys.argv[1] if len(sys.argv) > 1 else "./models"

try:
    print("📥 下载 Qwen2-VL-2B-Instruct...")
    path = snapshot_download(
        'qwen/Qwen2-VL-2B-Instruct',
        cache_dir=model_dir
    )
    print(f"✓ 下载完成: {path}")
except Exception as e:
    print(f"✗ 下载失败: {e}")
    sys.exit(1)
EOF
        "$MODEL_DIR"
        ;;
        
    4)
        echo ""
        echo "📖 手动下载说明:"
        echo "================"
        echo ""
        echo "1. 访问以下网址之一:"
        echo "   - ModelScope: https://modelscope.cn/models/qwen/Qwen2-VL-2B-Instruct"
        echo "   - HuggingFace: https://huggingface.co/Qwen/Qwen2-VL-2B-Instruct"
        echo ""
        echo "2. 下载 INT4 量化版 MNN 模型文件"
        echo "   文件名通常为: Qwen2-VL-2B-Instruct-MNN-Int4.mnn 或类似"
        echo ""
        echo "3. 将下载的文件放入: $MODEL_DIR/"
        echo ""
        echo "4. 如需从 PyTorch/ONNX 自行转换，请参考 docs/02-模型准备.md"
        echo ""
        ;;
        
    *)
        echo -e "${RED}无效选择${NC}"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "📁 当前模型目录内容:"
ls -lh "$MODEL_DIR" 2>/dev/null || echo "目录为空"
echo "=========================================="
