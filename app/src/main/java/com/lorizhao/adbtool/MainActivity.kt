package com.lorizhao.adbtool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.lorizhao.adbtool.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取 ADB 状态
        val adbEnabled = Settings.Global.getInt(
            contentResolver,
            Settings.Global.ADB_ENABLED,
            adb_status_off_int
        )

        binding.switchADB.isChecked = adbEnabled == adb_status_on_int
        binding.switchADB.setOnCheckedChangeListener { compoundButton, b ->
            Settings.Global.putInt(contentResolver, Settings.Global.ADB_ENABLED, if (b){
                adb_status_on_int
            }else{
                adb_status_off_int
            })

            compoundButton.postDelayed({
                val temp = Settings.Global.getInt(
                    contentResolver,
                    Settings.Global.ADB_ENABLED,
                    adb_status_off_int
                )
                binding.switchADB.isChecked = temp == adb_status_on_int
            }, 3000)
        }


        // 获取 USB 设备角色
        val usbRole = getCommand(action_get_usb_status_string)
        binding.switchUSB.isChecked = usbRole == usb_status_gadget

        Timber.tag("usbRole").d("adbEnabled = $adbEnabled , usbRole = ${usbRole}")
        binding.switchUSB.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                runCommand(action_put_usb_status_gadget)
            }else{
                runCommand(action_put_usb_status_host)
            }
            compoundButton.postDelayed({
                val temp = getCommand(action_get_usb_status_string)
                binding.switchUSB.isChecked = temp == usb_status_gadget
            }, 3000)
        }

    }

    private fun getCommand(command: String): String {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


    private fun runCommand(command: String){
        Runtime.getRuntime().exec(command)
    }
}