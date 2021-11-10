package com.efuntikov.contactsapp.di.component

import android.app.Application
import com.efuntikov.contactsapp.ContactDetailFragment
import com.efuntikov.contactsapp.ContactsApplication
import com.efuntikov.contactsapp.di.module.ApplicationModule
import com.efuntikov.contactsapp.di.module.NetworkModule
import com.efuntikov.contactsapp.di.module.RepositoryModule
import com.efuntikov.contactsapp.di.module.ViewModelModule
import com.efuntikov.contactsapp.di.module.repositories.ContactsRepositoryModule
import com.efuntikov.contactsapp.ContactsListFragment
import com.efuntikov.contactsapp.HomeActivity
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class,
        RepositoryModule::class,
        ViewModelModule::class,
        NetworkModule::class,
        ContactsRepositoryModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<ContactsApplication> {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): ApplicationComponent.Builder

        fun build(): ApplicationComponent
    }

    fun inject(contactsListFragment: ContactsListFragment)
    fun inject(contactsDetailFragment: ContactDetailFragment)
}