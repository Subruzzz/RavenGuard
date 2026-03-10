package com.ravenguard.app.di

import android.content.Context
import androidx.room.Room
import com.ravenguard.app.bluetooth.BleManager
import com.ravenguard.app.data.database.ContactDao
import com.ravenguard.app.data.database.RavenGuardDatabase
import com.ravenguard.app.data.repository.ContactRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RavenGuardDatabase =
        Room.databaseBuilder(context, RavenGuardDatabase::class.java, "ravenguard.db").build()

    @Provides
    fun provideContactDao(db: RavenGuardDatabase): ContactDao = db.contactDao()

    @Provides
    @Singleton
    fun provideContactRepository(dao: ContactDao): ContactRepository = ContactRepository(dao)

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext context: Context): BleManager = BleManager(context)
}
