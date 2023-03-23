package com.lorizhao.adbtool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.lorizhao.adbtool.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取 ADB 状态
        val adbEnabled = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0)
        // 获取 USB 设备角色
        val usbRoleCommand = "cat /sys/devices/platform/5b0d0000.usb/ci_hdrc.0/role"
        val usbRole = runCommand(usbRoleCommand)
        binding.switchADB.isChecked = adbEnabled == 1
        Timber.d("adbEnabled = $adbEnabled , usbRole = ${usbRole}")

    }



    private fun runCommand(command: String): String {
        val process = Runtime.getRuntime().exec(command)
        val inputStream = process.inputStream
        val buffer = ByteArray(1024)
        var read: Int
        val output = StringBuilder()
        while (inputStream.read(buffer).also { read = it } != -1) {
            output.append(String(buffer, 0, read))
        }
        inputStream.close()
        return output.toString().trim()
    }
}