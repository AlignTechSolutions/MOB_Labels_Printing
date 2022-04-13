package com.alignTech.labelsPrinting.di.modules

import android.content.Context
import androidx.room.Room
import com.alignTech.labelsPrinting.local.AppDatabase
import com.alignTech.labelsPrinting.local.DatabaseMigrations.MIGRATION_0_1
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "assets.database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_0_1)
            .build()
    }
}
