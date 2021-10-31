package com.efuntikov.contactsapp.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.efuntikov.contactsapp.ViewModelFactory
import com.efuntikov.contactsapp.ViewModelKey
import com.efuntikov.contactsapp.ContactsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    @Singleton
    abstract fun contactsViewModel(viewModel: ContactsViewModel): ViewModel
}