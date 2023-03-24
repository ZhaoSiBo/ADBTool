package com.lorizhao.adbtool


const val usb_status_host = "host"
const val usb_status_gadget = "gadget"


const val action_adb_devices = "adb devices"


const val action_get_usb_status_string = "cat /sys/devices/platform/5b0d0000.usb/ci_hdrc.0/role"
const val action_put_usb_status_host = "echo host > /sys/devices/platform/5b0d0000.usb/ci_hdrc.0/role"
const val action_put_usb_status_gadget = "echo gadget > /sys/devices/platform/5b0d0000.usb/ci_hdrc.0/role"


const val adb_status_on_int = 1
const val adb_status_off_int = 0