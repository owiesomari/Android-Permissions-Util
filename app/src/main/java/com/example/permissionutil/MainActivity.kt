package com.example.permissionutil

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var requestWritePermission: Button
    private lateinit var permissionUtils: PermissionsUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionUtils = PermissionsUtil.getInstance(this, this.activityResultRegistry)
        lifecycle.addObserver(permissionUtils)

        requestWritePermission = findViewById(R.id.request_Write_Permission)
        requestWritePermission.setOnClickListener {

            permissionUtils.requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionsUtil.PermissionsListenerCallback {
                override fun onPermissionGranted() {
                    Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDialogCancel() {
                    Toast.makeText(applicationContext, "Cancel", Toast.LENGTH_SHORT).show()

                }
            })
        }
    }
}