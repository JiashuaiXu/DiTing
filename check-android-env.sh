#!/usr/bin/env bash
# check-android-env.sh
# 在 NixOS 上检测 Android 开发环境是否有效
# 使用前：chmod +x check-android-env.sh && ./check-android-env.sh

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!] $1${NC}"; }
error() { echo -e "${RED}[✗] $1${NC}"; }

# === 1. 查找 Android SDK 路径 ===
find_sdk() {
    # 优先级：环境变量 > 默认 Nix 路径 > Android Studio 安装路径
    if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
        echo "$ANDROID_SDK_ROOT"
    elif [[ -n "${ANDROID_HOME:-}" ]]; then
        echo "$ANDROID_HOME"
    elif command -v android-studio-full >/dev/null 2>&1; then
        # Android Studio 通常把 SDK 放在 ~/.android/Sdk
        local home_sdk="$HOME/.android/Sdk"
        [[ -d "$home_sdk" ]] && echo "$home_sdk"
    else
        # 尝试从 Nix store 查找 android-sdk
        local nix_sdk=$(nix-build '<nixpkgs>' --no-out-link -A androidsdk 2>/dev/null || true)
        [[ -d "$nix_sdk" ]] && echo "$nix_sdk"
    fi
}

SDK_ROOT=$(find_sdk || echo "")
if [[ -z "$SDK_ROOT" || ! -d "$SDK_ROOT" ]]; then
    error "Android SDK 未找到！"
    warn "请确保已安装 android-studio 或 androidsdk，并设置 ANDROID_SDK_ROOT"
    warn "  示例：在 ~/.bashrc 添加："
    warn "    export ANDROID_SDK_ROOT=\"\$HOME/.android/Sdk\""
    exit 1
fi

log "Android SDK 路径: $SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"

# === 2. 检查关键工具 ===
check_tool() {
    local tool=$1
    local path_var=$2
    if command -v "$tool" >/dev/null 2>&1; then
        log "$tool: $(command -v $tool)"
    else
        error "$tool 未找到！"
        warn "  尝试在 Nix 中安装：android-tools 或 android-studio"
        return 1
    fi
}

check_tool "sdkmanager" "SDKMANAGER"
check_tool "adb" "ADB"
check_tool "emulator" "EMULATOR"
check_tool "avdmanager" "AVDMANAGER"

# === 3. 检查 SDK 包（需接受许可证）===
if sdkmanager --list_installed >/dev/null 2>&1; then
    log "SDK 包已安装（部分）"
else
    warn "sdkmanager 无法列出包，可能未接受许可证"
    warn "  运行：sdkmanager --licenses 并全部输入 'y'"
fi

# === 4. 检查 ADB 服务器 ===
log "启动 ADB 服务器..."
if adb kill-server 2>/dev/null && adb start-server; then
    log "ADB 服务器启动成功"
else
    error "ADB 服务器启动失败"
    exit 1
fi

# 列出设备
log "检测设备..."
DEVICE_OUTPUT=$(adb devices)
echo "$DEVICE_OUTPUT"
if echo "$DEVICE_OUTPUT" | grep -q "device$"; then
    log "检测到真实设备或正在运行的模拟器"
else
    warn "未检测到设备（正常若无连接）"
fi

# === 5. 检查 AVD 虚拟设备 ===
log "列出 AVD 虚拟设备..."
if avdmanager list avd | grep -q "Name:"; then
    avdmanager list avd
    log "AVD 配置正常"
else
    warn "未创建任何 AVD 虚拟设备"
    warn "  使用 Android Studio 创建，或运行："
    warn "    avdmanager create avd -n test -k \"system-images;android-30;google_apis;x86\""
fi

# === 6. 可选：检查 NDK ===
if [[ -n "${ANDROID_NDK_ROOT:-}" ]] && [[ -d "$ANDROID_NDK_ROOT" ]]; then
    log "NDK 已配置: $ANDROID_NDK_ROOT"
    if command -v ndk-build >/dev/null 2>&1; then
        log "ndk-build 可用: $(ndk-build --version | head -1)"
    fi
else
    warn "NDK 未配置（原生开发可选）"
fi

# === 7. Flutter 检测（可选）===
if command -v flutter >/dev/null 2>&1; then
    log "Flutter 检测..."
    if flutter doctor --android-licenses 2>/dev/null | grep -q "All SDK package licenses accepted"; then
        log "Flutter Android 许可证已接受"
    else
        warn "Flutter 建议运行：flutter doctor --android-licenses"
    fi
    flutter doctor -v | grep -A 5 "Android toolchain" || true
fi

# === 最终结果 ===
echo -e "\n${GREEN}Android 环境检测完成！${NC}"
log "SDK、ADB、Emulator 基本可用"
warn "如需构建 APK，建议使用 Android Studio 或 Gradle + FHS 环境"
echo -e "${YELLOW}提示：NixOS 上推荐使用 'androidenv.buildFHSUserEnv' 运行 Gradle${NC}"

exit 0
