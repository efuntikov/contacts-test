package com.efuntikov.contactsapp

import com.efuntikov.contactsapp.di.component.ApplicationComponent
import com.efuntikov.contactsapp.di.component.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ContactsApplication : DaggerApplication() {

    companion object {
        lateinit var appComponent: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        appComponent = DaggerApplicationComponent.builder().application(this).build()
        return appComponent
    }
}