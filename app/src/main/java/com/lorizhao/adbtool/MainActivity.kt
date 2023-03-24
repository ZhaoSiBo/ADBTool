package com.lorizhao.adbtool

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import com.lorizhao.adbtool.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
        }


        // 获取 USB 设备角色
        val usbRole = runCommand(action_get_usb_status_string)
        binding.switchUSB.isChecked = usbRole == usb_status_gadget
        binding.switchUSB.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                executeShellCommand(action_put_usb_status_gadget)
            }else{
                executeShellCommand(action_put_usb_status_host)
            }
            testResult()
        }

        testResult()

    }

    private fun runCommand(command: String): String {
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


    private fun executeShellCommand(command: String): String {
        var output = ""
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            val outputStreamWriter = OutputStreamWriter(outputStream)
            outputStreamWriter.write("$command\n")
            outputStreamWriter.flush()
            outputStreamWriter.write("exit\n")
            outputStreamWriter.flush()

            val inputStream = process.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuffer = StringBuffer()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
            output = stringBuffer.toString().trim()

            process.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return output
    }

    fun testResult(){
        val adbResult = Settings.Global.getInt(
            contentResolver,
            Settings.Global.ADB_ENABLED,
            adb_status_off_int
        )
        val usbRole = runCommand(action_get_usb_status_string)
        val isTestKey = isTestKeys()
        Timber.tag("testResult").d("adbResult = $adbResult , usbRole = $usbRole,isTestKey = ${isTestKey}")
    }

    fun isTestKeys(): Boolean {
        val pm = packageManager
        val packageName = packageName
        val flags = PackageManager.GET_SIGNATURES

        try {
            val packageInfo = pm.getPackageInfo(packageName, flags)
            val signatures = packageInfo.signatures
            for (signature in signatures) {
                val raw = signature.toByteArray()
                val md = MessageDigest.getInstance("SHA")
                md.update(raw)
                val signatureHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                if (signatureHash.contains("test-keys")) {
                    return true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return false
    }

}