package com.alignTech.labelsPrinting.di.modules

import android.content.Context
import com.alfayedoficial.kotlinutils.BuildConfig
import com.alfayedoficial.kotlinutils.KUPreferences
import com.alignTech.labelsPrinting.core.util.AppConstants
import com.alignTech.labelsPrinting.core.util.AppConstants.BASE_URL
import com.alignTech.labelsPrinting.core.util.AppConstants.REQUEST_TIME_OUT
import com.alignTech.labelsPrinting.core.util.TokenUtil
import com.alignTech.labelsPrinting.domain.network.ApiService
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by ( Eng Ali Al Fayed)
 * Class do :
 * Date 9/27/2021 - 1:09 PM
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideHeadersInterceptor(appPreferences: KUPreferences) = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Platform", "api")
                .addHeader("sequence", "123")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("api-version", "1.0")
                .addHeader("ApiKey", "uqtfggwvbolwhphwjkhtewfqawyslnka")
                .addHeader("Source", "121")
                .addHeader("Source-Version", AppConstants.VERSION_NAME)
                .addHeader("Accept-Language", "ar-EG")
                //add token key value if exist
                .also {
                    if (TokenUtil.getTokenFromMemory().isNotEmpty()) {
                        it.addHeader("Authorization", "Bearer ${TokenUtil.getTokenFromMemory()}")
                    }
                }
                .build()
        )
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        headersInterceptor: Interceptor,
        logging: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient {
        return if (!BuildConfig.DEBUG) {

            OkHttpClient.Builder()
                .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(headersInterceptor)
                .addNetworkInterceptor(logging)
                .addInterceptor(chuckerInterceptor)
                .build()
        } else {
            OkHttpClient.Builder()
                .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(headersInterceptor)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls() // To allow sending null values
            .create()
    }

    @Provides
    @Singleton
    fun provideApiServices(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun getDynamicRetrofitClient(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val dateFormatter = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(dateFormatter))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun getChuckInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context))
            .maxContentLength(250000L)
            .alwaysReadResponseBody(true)
            .build()
    }
}