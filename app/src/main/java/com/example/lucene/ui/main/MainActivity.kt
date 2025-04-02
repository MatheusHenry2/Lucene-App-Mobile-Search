package com.example.lucene.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lucene.R
import com.example.lucene.ui.search.SearchFragment
import com.example.lucene.worker.DownloadMoviesWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .commit()
        }
//        scheduleDownloadMoviesWorker()
    }

//    private fun scheduleDownloadMoviesWorker() {
//        val workerRequest = OneTimeWorkRequestBuilder<DownloadMoviesWorker>()
//            .setInitialDelay(15, TimeUnit.SECONDS)
//            .build()
//        WorkManager.getInstance(this).enqueue(workerRequest)
//    }
}
