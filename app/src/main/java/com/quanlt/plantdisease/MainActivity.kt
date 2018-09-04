package com.quanlt.plantdisease

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.cameraview.CameraView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mCameraView: CameraView? = null

    private var mBackgroundHandler: Handler? = null

    private lateinit var mainHandler: Handler

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.take_picture -> if (mCameraView != null) {
                mCameraView!!.takePicture()
            }
        }
    }

    private val backgroundHandler: Handler
        get() {
            if (mBackgroundHandler == null) {
                val thread = HandlerThread("background")
                thread.start()
                mBackgroundHandler = Handler(thread.looper)
            }
            return mBackgroundHandler as Handler
        }

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView?) {
            Log.d(TAG, "onCameraOpened")
        }

        override fun onCameraClosed(cameraView: CameraView?) {
            Log.d(TAG, "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            Log.d(TAG, "onPictureTaken " + data.size)
            backgroundHandler.post {
                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "picture.jpg")
                var os: OutputStream? = null
                try {
                    os = FileOutputStream(file)
                    os.write(data)
                    os.close()
                    mainHandler.post {
                        openDetailActivity(file.path)
                    }
                } catch (e: IOException) {
                    Log.w(TAG, "Cannot write to $file", e)
                } finally {
                    if (os != null) {
                        try {
                            os.close()
                        } catch (e: IOException) {
                            // Ignore
                        }

                    }
                }
            }
        }

    }

    private fun openDetailActivity(path: String) {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.PATH, path)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCameraView = findViewById(R.id.camera)
        if (mCameraView != null) {
            mCameraView!!.addCallback(mCallback)
        }
        val fab = findViewById<View>(R.id.take_picture) as FloatingActionButton
        fab.setOnClickListener(mOnClickListener)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        mainHandler = Handler()
    }

    override fun onResume() {
        super.onResume()
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> mCameraView!!.start()
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) -> ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onPause() {
        mCameraView!!.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBackgroundHandler != null) {
            mBackgroundHandler?.looper?.quitSafely()
            mBackgroundHandler = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting camera permission.")
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show()
                }
            }
        }// No need to start camera here; it is handled by onResume
    }

    class ConfirmationDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = arguments
            return AlertDialog.Builder(activity)
                    .setMessage(args!!.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ ->
                        val permissions = args.getStringArray(ARG_PERMISSIONS)
                                ?: throw IllegalArgumentException()
                        activity?.let {
                            ActivityCompat.requestPermissions(activity as Activity,
                                    permissions, args.getInt(ARG_REQUEST_CODE))
                        }
                    }
                    .setNegativeButton(android.R.string.cancel
                    ) { _, _ ->
                        activity?.let {
                            Toast.makeText(activity,
                                    args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                    .create()
        }

        companion object {

            private const val ARG_MESSAGE = "message"
            private const val ARG_PERMISSIONS = "permissions"
            private const val ARG_REQUEST_CODE = "request_code"
            private const val ARG_NOT_GRANTED_MESSAGE = "not_granted_message"

            fun newInstance(@StringRes message: Int,
                            permissions: Array<String>, requestCode: Int, @StringRes notGrantedMessage: Int): ConfirmationDialogFragment {
                val fragment = ConfirmationDialogFragment()
                val args = Bundle()
                args.putInt(ARG_MESSAGE, message)
                args.putStringArray(ARG_PERMISSIONS, permissions)
                args.putInt(ARG_REQUEST_CODE, requestCode)
                args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage)
                fragment.arguments = args
                return fragment
            }
        }

    }

    companion object {

        private const val TAG = "MainActivity"

        private const val REQUEST_CAMERA_PERMISSION = 1

        private const val FRAGMENT_DIALOG = "dialog"
    }

}
