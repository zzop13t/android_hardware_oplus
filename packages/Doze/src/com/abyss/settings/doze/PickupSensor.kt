/*
 * Copyright (C) 2021-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.abyss.settings.doze

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.Display
import java.util.concurrent.Executors

class PickupSensor(
    private val context: Context,
    sensorType: String,
    private val sensorValue: Float,
) : SensorEventListener {
    private val powerManager = context.getSystemService(PowerManager::class.java)!!
    private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)

    private val sensorManager = context.getSystemService(SensorManager::class.java)!!
    private val sensor = Utils.getSensor(sensorManager, sensorType)

    private val executorService = Executors.newSingleThreadExecutor()
    private var entryTimestamp = 0L

    // 用近距离传感器做"贴身/口袋"判断：贴脸时不唤醒（上游是靠 Lineage 给 framework 打的补丁做的）
    private val proximitySensor =
        Utils.getSensor(sensorManager, context.resources.getString(R.string.pocket_sensor_type))
    private val proximityValue = context.resources.getFloat(R.dimen.pocket_sensor_value)
    @Volatile private var proximityCovered = false

    private val proximityListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                proximityCovered = event.values[0] <= proximityValue
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

    override fun onSensorChanged(event: SensorEvent) {
        if (DEBUG) Log.d(TAG, "Got sensor event: ${event.values[0]}")
        val delta = SystemClock.elapsedRealtime() - entryTimestamp
        if (delta < MIN_PULSE_INTERVAL_MS) {
            return
        }
        entryTimestamp = SystemClock.elapsedRealtime()
        if (event.values[0] == sensorValue) {
            if (Utils.isPickUpSetToWake(context)) {
                if (proximityCovered) {
                    // 贴着东西（口袋/遮住）：不亮屏
                    if (DEBUG) Log.d(TAG, "Pick-up ignored: proximity covered")
                } else {
                    wakeLock.acquire(WAKELOCK_TIMEOUT_MS)
                    // 上游用的是 LineageOS 给 framework 加的 wakeUpWithProximityCheck()，
                    // AOSP 没有；这里自己查 proximity 后调用 AOSP 的 wakeUp()
                    powerManager.wakeUp(
                        SystemClock.uptimeMillis(),
                        PowerManager.WAKE_REASON_GESTURE,
                        TAG,
                        Display.DEFAULT_DISPLAY,
                    )
                }
            } else {
                Utils.launchDozePulse(context)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    fun enable() {
        if (sensor != null) {
            Log.d(TAG, "Enabling")
            executorService.submit {
                entryTimestamp = SystemClock.elapsedRealtime()
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (proximitySensor != null) {
            executorService.submit {
                sensorManager.registerListener(
                    proximityListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                )
            }
        }
    }

    fun disable() {
        if (sensor != null) {
            Log.d(TAG, "Disabling")
            executorService.submit { sensorManager.unregisterListener(this, sensor) }
        }
        if (proximitySensor != null) {
            executorService.submit { sensorManager.unregisterListener(proximityListener, proximitySensor) }
        }
    }

    companion object {
        private const val TAG = "PickupSensor"
        private const val DEBUG = false

        private const val MIN_PULSE_INTERVAL_MS = 2500L
        private const val WAKELOCK_TIMEOUT_MS = 300L
    }
}
