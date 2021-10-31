package com.efuntikov.contactsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        ContactsApplication.appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[ContactsViewModel::class.java]

        viewModel.getNavigation().observe(this) {
            navigate(it)
        }

        if (savedInstanceState == null) {
            navigate(Pair(ContactsViewModel.Navigation.CONTACT_LIST, ""))
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    private fun navigate(navigation: Pair<ContactsViewModel.Navigation, String>) {
        when (navigation.first) {
            ContactsViewModel.Navigation.CONTACT_DETAIL -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.container,
                        ContactDetailFragment.newInstance(navigation.second)
                    )
                    .addToBackStack(null).commit()
            }
            ContactsViewModel.Navigation.CONTACT_LIST -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ContactsListFragment.newInstance())
                    .commit()
            }
        }
    }
}