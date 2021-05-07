package com.example.testrecorder

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeviceAdminSampleReceiver : DeviceAdminReceiver() {
    fun showToast(context: Context, msg: String?) {
        val status = context.getString(R.string.admin_receiver_status, msg)
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === ACTION_DEVICE_ADMIN_DISABLE_REQUESTED) {
            abortBroadcast()
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled))
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return context.getString(R.string.admin_receiver_status_disable_warning)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_disabled))
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_changed))
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_failed))
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded))
    }

    override fun onPasswordExpiring(context: Context, intent: Intent) {
        val dpm = context.getSystemService(
                Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val expr = dpm.getPasswordExpiration(
                ComponentName(context, DeviceAdminSampleReceiver::class.java))
        val delta = expr - System.currentTimeMillis()
        val expired = delta < 0L
        /*String message = context.getString(expired ?
                R.string.expiration_status_past : R.string.expiration_status_future);
        showToast(context, message);*/
//        Log.v(TAG, message);
    }
}