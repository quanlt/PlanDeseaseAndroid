package com.quanlt.plantdisease

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import jp.wasabeef.glide.transformations.CropSquareTransformation
import kotlinx.android.synthetic.main.activity_detail.*
import java.io.IOException
import java.nio.charset.Charset

class DetailActivity : AppCompatActivity() {
    lateinit var imageClassifier: ImageClassifier
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        val path = intent?.getStringExtra(PATH)
        Glide.with(this)
                .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .load(path)
                .into(image)

        imageClassifier = ImageClassifier(this)
        Glide.with(this)
                .asBitmap()
                .apply(RequestOptions().transform(CropSquareTransformation()))
                .load(path)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        resource?.let {
                            val result = imageClassifier.classifyFrame(resource)
                            try {
                                val inputStream = assets.open("plant_detail.json")
                                val size = inputStream.available()
                                val buffer = ByteArray(size)
                                inputStream.read(buffer)
                                inputStream.close()
                                val json = String(buffer, Charset.defaultCharset())
                                val gson = Gson()
                                val type = object : TypeToken<Map<String, String>>() {}.type
                                val data = gson.fromJson<Map<String, String>>(json, type)
                                result.forEach {
                                    Log.d("Data", "${data[it.first]} ${it.second}")
                                }

                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        return false
                    }

                }).submit(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y)

    }

    override fun onDestroy() {
        super.onDestroy()
        imageClassifier.close()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val PATH = "path"
    }
}
