package com.example.lucene.data.remote.service

import com.example.lucene.data.remote.service.TMDbService.TMDbConstants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TMDbApi {
    val retrofitService: TMDbService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TMDbService::class.java)
    }
}
