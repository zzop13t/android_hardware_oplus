#
# SPDX-FileCopyrightText: The AbyssOS Project
# SPDX-License-Identifier: Apache-2.0
#
# 复制自 device/lineage/sepolicy/common，去掉了 lineage 相关的内容：
#   - 删除文件名带 lineage 的策略文件
#   - 删除所有含 "lineage" 的行（lineage_*_service 类型、ro.lineage.* 属性、hal_lineage_* 规则等）
# 保留的是平台/厂商策略真正依赖的部分（属性类型、文件类型、app 域等）。
#

ifeq ($(TARGET_COPY_OUT_VENDOR), vendor)
ifeq ($(BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE),)
TARGET_USES_PREBUILT_VENDOR_SEPOLICY ?= true
endif
endif

SYSTEM_EXT_PUBLIC_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/common/public

SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/common/private

ifeq ($(TARGET_USES_PREBUILT_VENDOR_SEPOLICY), true)
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/common/dynamic
else
BOARD_VENDOR_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/common/dynamic \
    hardware/oplus/sepolicy/common/vendor
endif
