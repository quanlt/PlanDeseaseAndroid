package com.quanlt.plantdisease

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_detail.*
import java.io.IOException
import java.nio.charset.Charset

class DetailActivity : AppCompatActivity() {
    lateinit var imageClassifier: ImageClassifier
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        val diagnosis = intent?.getParcelableExtra<Diagnosis>(DIAGNOSIS)
        diagnosis?.let {
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .load(diagnosis.imagePath)
                    .into(image)
            try {
                val inputStream = assets.open("plant_detail.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                val json = String(buffer, Charset.defaultCharset())
                val gson = Gson()
                val type = object : TypeToken<Map<String, Disease>>() {}.type
                val data = gson.fromJson<Map<String, Disease>>(json, type)
                val disease = "${data[diagnosis.codeName]?.name}: ${diagnosis.percent}%"
                tvDiagnosis.text = disease
                tvDetail.text = data[diagnosis.codeName]?.detail
                toolbar.title = data[diagnosis.codeName]?.name

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DIAGNOSIS = "diagnosis"
    }
}
