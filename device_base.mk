# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# These is the hardware-specific overlay, which points to the location
# of hardware-specific resource overrides, typically the frameworks and
# application settings that are stored in resourced.
DEVICE_PACKAGE_OVERLAYS := device/samsung/p1-common/overlay

# These are the hardware-specific configuration files
PRODUCT_COPY_FILES := \
	device/samsung/p1-common/prebuilt/etc/asound.conf:system/etc/asound.conf \
	device/samsung/p1-common/prebuilt/lib/egl/egl.cfg:system/lib/egl/egl.cfg \
	device/samsung/p1-common/prebuilt/etc/bluetooth/main.conf:system/etc/bluetooth/main.conf

# Init files
PRODUCT_COPY_FILES += \
	device/samsung/p1-common/init.p1-common.rc:root/init.p1-common.rc \
	device/samsung/p1-common/usb.rc:recovery/root/usb.rc \
	device/samsung/p1-common/lpm.rc:root/lpm.rc

# Prebuilt kl keymaps
PRODUCT_COPY_FILES += \
	device/samsung/p1-common/prebuilt/usr/keylayout/sec_jack.kl:system/usr/keylayout/sec_jack.kl \
	device/samsung/p1-common/prebuilt/usr/keylayout/p1_keyboard.kl:system/usr/keylayout/p1_keyboard.kl \
	device/samsung/p1-common/prebuilt/usr/keylayout/AT42QT602240_Touchscreen.kl:system/usr/keylayout/AT42QT602240_Touchscreen.kl

# Filesystem management tools
PRODUCT_PACKAGES := \
	make_ext4fs \
	setup_fs \
	bml_over_mtd

# Lights
PRODUCT_PACKAGES += \
	lights.s5pc110

# Audio
PRODUCT_PACKAGES += \
	audio.a2dp.default \
	audio.primary.s5pc110 \
	audio_policy.s5pc110

# Camera
PRODUCT_PACKAGES += \
	camera.s5pc110 \
	libs3cjpeg

# These are the OpenMAX IL configuration files
PRODUCT_COPY_FILES += \
	device/samsung/p1-common/sec_mm/sec_omx/sec_omx_core/secomxregistry:system/etc/secomxregistry \
	device/samsung/p1-common/prebuilt/etc/media_profiles.xml:system/etc/media_profiles.xml

# These are the OpenMAX IL modules
PRODUCT_PACKAGES += \
	libSEC_OMX_Core.s5pc110 \
	libOMX.SEC.AVC.Decoder.s5pc110 \
	libOMX.SEC.M4V.Decoder.s5pc110 \
	libOMX.SEC.M4V.Encoder.s5pc110 \
	libOMX.SEC.AVC.Encoder.s5pc110

# Libs
PRODUCT_PACKAGES += \
	hwcomposer.s5pc110 \
	libstagefrighthw

# Device-specific packages
PRODUCT_PACKAGES += \
	P1Parts \
	tvouthack

# apns config file
PRODUCT_COPY_FILES += \
	development/data/etc/apns-conf.xml:system/etc/apns-conf.xml

# Touchscreen
PRODUCT_COPY_FILES += \
        device/samsung/p1-common/prebuilt/usr/idc/AT42QT602240_Touchscreen.idc:system/usr/idc/AT42QT602240_Touchscreen.idc

# These are the hardware-specific features
PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/handheld_core_hardware.xml:system/etc/permissions/handheld_core_hardware.xml \
	frameworks/base/data/etc/android.hardware.camera.flash-autofocus.xml:system/etc/permissions/android.hardware.camera.flash-autofocus.xml \
	frameworks/base/data/etc/android.hardware.camera.front.xml:system/etc/permissions/android.hardware.camera.front.xml \
        frameworks/base/data/etc/android.hardware.location.xml:system/etc/permissions/android.hardware.location.xml \
	frameworks/base/data/etc/android.hardware.location.gps.xml:system/etc/permissions/android.hardware.location.gps.xml \
	frameworks/base/data/etc/android.hardware.wifi.xml:system/etc/permissions/android.hardware.wifi.xml \
	frameworks/base/data/etc/android.hardware.sensor.proximity.xml:system/etc/permissions/android.hardware.sensor.proximity.xml \
	frameworks/base/data/etc/android.hardware.sensor.light.xml:system/etc/permissions/android.hardware.sensor.light.xml \
	frameworks/base/data/etc/android.hardware.touchscreen.multitouch.jazzhand.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.jazzhand.xml \
	frameworks/base/data/etc/android.software.sip.voip.xml:system/etc/permissions/android.software.sip.voip.xml \
	frameworks/base/data/etc/android.hardware.usb.accessory.xml:system/etc/permissions/android.hardware.usb.accessory.xml \
	frameworks/base/data/etc/android.hardware.usb.host.xml:system/etc/permissions/android.hardware.usb.host.xml \
	packages/wallpapers/LivePicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# The OpenGL ES API level that is natively supported by this device.
# This is a 16.16 fixed point number
PRODUCT_PROPERTY_OVERRIDES := \
       ro.opengles.version=131072

# rotation
PRODUCT_PROPERTY_OVERRIDES += \
       ro.sf.hwrotation=90

# radio
PRODUCT_PROPERTY_OVERRIDES += \
       ro.telephony.ril_class=SamsungRIL

# These are the hardware-specific settings that are stored in system properties.
# Note that the only such settings should be the ones that are too low-level to
# be reachable from resources or other mechanisms.
PRODUCT_PROPERTY_OVERRIDES += \
       wifi.interface=eth0 \
       wifi.supplicant_scan_interval=45

# enable Google-specific location features,
# like NetworkLocationProvider and LocationCollector
PRODUCT_PROPERTY_OVERRIDES += \
        ro.com.google.locationfeatures=1 \
        ro.com.google.networklocation=1

# Extended JNI checks
# The extended JNI checks will cause the system to run more slowly, but they can spot a variety of nasty bugs 
# before they have a chance to cause problems.
# Default=true for development builds, set by android buildsystem.
PRODUCT_PROPERTY_OVERRIDES += \
    ro.kernel.android.checkjni=0 \
    dalvik.vm.checkjni=false

# Override /proc/sys/vm/dirty_ratio on UMS
PRODUCT_PROPERTY_OVERRIDES += \
    ro.vold.umsdirtyratio=20

# we have enough storage space to hold precise GC data
PRODUCT_TAGS += dalvik.gc.type-precise

# dalvik
include frameworks/base/build/phone-hdpi-512-dalvik-heap.mk

# Set default USB interface
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mass_storage

# keep dalvik cache on /data
PRODUCT_PROPERTY_OVERRIDES += \
    dalvik.vm.dexopt-data-only=1

# installer
PRODUCT_COPY_FILES += \
	device/samsung/p1-common/updater.sh:updater.sh

# bml_over_mtd
PRODUCT_COPY_FILES += \
	device/samsung/p1-common/bml_over_mtd.sh:bml_over_mtd.sh

# Set product characteristic to tablet, needed for some ui elements
PRODUCT_CHARACTERISTICS := tablet
