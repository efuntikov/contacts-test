package com.efuntikov.contactsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
//class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>,
//        @JvmSuppressWildcards Provider<ViewModel>>): ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
//        viewModels[modelClass]?.get() as T
//}
class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModelProvider = viewModels[modelClass]
            ?: throw IllegalArgumentException("model class $modelClass not found")
        return viewModelProvider.get() as T
    }

}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)