package com.alteratom.dashboard

import com.alteratom.BuildConfig

class Settings {
    var version = BuildConfig.VERSION_CODE
    var notifyStack = true
    var hideNav = false
    var lastDashboardId = 0L
    var militaryTime = true
    var startFromLast = true
    var mqttTabShow = true
    var animateUpdate = true
    var pendingPurchase = false
    var fgEnabled = false
}