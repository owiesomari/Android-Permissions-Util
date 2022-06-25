package com.example.permissionutil


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


class PermissionsUtil private constructor(private val context: Context, private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
    private lateinit var callBack: PermissionsListenerCallback
    private lateinit var getContent: ActivityResultLauncher<Array<String>>
    private var permissionMessages: HashMap<String, String> = HashMap()

    init {
        permissionMessages.apply {
            put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, String.format(context.getString(R.string.DIALOG_MESSAGE), "Write Storage"))
            put(android.Manifest.permission.READ_EXTERNAL_STORAGE, String.format(context.getString(R.string.DIALOG_MESSAGE), "Read Storage"))
            put(android.Manifest.permission.CAMERA, String.format(context.getString(R.string.DIALOG_MESSAGE), "Camera"))
            put(android.Manifest.permission.ACCESS_FINE_LOCATION, String.format(context.getString(R.string.DIALOG_MESSAGE), "Location"))
            put(android.Manifest.permission.READ_CONTACTS, String.format(context.getString(R.string.DIALOG_MESSAGE), "Contacts"))
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register(KEY, owner, ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                this.callBack.onPermissionGranted()
            } else permissions.forEach {
                if (!it.value) {
                    showDefaultDialog(it.key)
                    return@register
                }
            }
        }
    }

    fun requestPermission(@NonNull permission: Array<String>, callBack: PermissionsListenerCallback) {
        this.callBack = callBack
        if (checkForPermissions(permission)) this.callBack.onPermissionGranted()
        else getContent.launch(permission)
    }

    private fun checkForPermissions(permission: Array<String>): Boolean {
        permission.forEach { if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) return false }
        return true
    }

    private fun showDefaultDialog(permission: String) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.apply {
            setIcon(R.drawable.ic_info)
            setMessage(permissionMessages.getValue(permission))
            setPositiveButton(context.getString(R.string.DIALOG_POSITIVE_BUTTON)) { _, _ ->
                openSetting()
            }
            setNegativeButton(context.getString(R.string.DIALOG_CANCEL)) { _, _ ->
                callBack.onPermissionDialogCancel()
            }
        }.create().show()
    }

    private fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(KEY_PACKAGE, context.packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    interface PermissionsListenerCallback {
        fun onPermissionGranted()
        fun onPermissionDialogCancel()
    }

    companion object {
        private const val KEY = "key"
        private const val KEY_PACKAGE = "package"

        @JvmStatic
        fun getInstance(context: Context, registry: ActivityResultRegistry): PermissionsUtil {
            return PermissionsUtil(context, registry)
        }
    }
}