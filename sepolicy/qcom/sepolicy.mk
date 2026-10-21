#
# SPDX-FileCopyrightText: The AbyssOS Project
# SPDX-License-Identifier: Apache-2.0
#
# 复制自 device/lineage/sepolicy/qcom，去掉了 lineage 相关的内容
# （hal_lineage_* 策略、lineage 命名的 file_contexts / property_contexts 行）。
# 保留的是 qcom 平台真正需要的类型定义（如 sysfs_socinfo_sensitive）、genfs 标签和 m4 映射。
#

ifeq ($(TARGET_COPY_OUT_VENDOR), vendor)
ifeq ($(BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE),)
TARGET_USES_PREBUILT_VENDOR_SEPOLICY ?= true
endif
endif

SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/qcom/private

ifeq ($(TARGET_USES_PREBUILT_VENDOR_SEPOLICY), true)
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/qcom/dynamic
else
BOARD_VENDOR_SEPOLICY_DIRS += \
    hardware/oplus/sepolicy/qcom/dynamic \
    hardware/oplus/sepolicy/qcom/vendor
endif

ifeq (,$(filter device/qcom/sepolicy-legacy-um/legacy/vendor/common device/qcom/sepolicy_vndr/legacy-um/legacy/vendor/common, $(BOARD_VENDOR_SEPOLICY_DIRS)))
BOARD_SEPOLICY_M4DEFS += \
    custom_ab_block_device=vendor_custom_ab_block_device \
    display_vendor_data_file=vendor_display_vendor_data_file \
    hal_gnss_qti=vendor_hal_gnss_qti \
    hal_perf_default=vendor_hal_perf_default \
    location_domain=vendor_location \
    persist_block_device=vendor_persist_block_device \
    qdisplay_service=vendor_qdisplay_service \
    sysfs_battery_supply=vendor_sysfs_battery_supply \
    sysfs_devfreq=vendor_sysfs_devfreq \
    sysfs_graphics=vendor_sysfs_graphics \
    sysfs_kgsl=vendor_sysfs_kgsl \
    sysfs_scsi_host=vendor_sysfs_scsi_host \
    sysfs_socinfo_sensitive=vendor_sysfs_soc_sensitive \
    sysfs_usb_supply=vendor_sysfs_usb_supply
else
BOARD_SEPOLICY_M4DEFS += \
    location_domain=location
endif
