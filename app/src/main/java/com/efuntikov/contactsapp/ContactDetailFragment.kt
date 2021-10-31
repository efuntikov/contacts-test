package com.efuntikov.contactsapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.efuntikov.contactsapp.domain.entity.EducationPeriod
import com.efuntikov.contactsapp.domain.entity.Temperament
import com.efuntikov.contactsapp.util.FragmentArgumentDelegate
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ContactDetailFragment : Fragment() {

    private var payload by FragmentArgumentDelegate<String>()

    private lateinit var contactNameView: TextView
    private lateinit var phoneNumberView: TextView
    private lateinit var temperamentView: TextView
    private lateinit var educationPeriodView: TextView
    private lateinit var biographyView: TextView

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactsViewModel

    companion object {
        fun newInstance(payload: String) = ContactDetailFragment().apply {
            this.payload = payload
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContactsApplication.appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.contact_detail_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)[ContactsViewModel::class.java]

        contactNameView = view.findViewById(R.id.contactNameView)
        phoneNumberView = view.findViewById(R.id.phoneNumberView)
        temperamentView = view.findViewById(R.id.temperamentView)
        educationPeriodView = view.findViewById(R.id.educationPeriodView)
        biographyView = view.findViewById(R.id.biographyView)

        initViews()
    }

    private fun initViews() {
        if (payload.isEmpty()) {
            return
        }

        val selectedContactIndex: Int
        try {
            selectedContactIndex = payload.toInt()
        } catch (ex: NumberFormatException) {
            Log.e(LOG_TAG, "Failed to get selected contact index, invalid incoming payload", ex)
            return
        }

        viewModel.getContacts().value?.let {
            val contact = it[selectedContactIndex]
            contactNameView.text = contact.name
            phoneNumberView.text = contact.phone
            temperamentView.text = when (contact.temperament) {
                Temperament.CHOLERIC -> "Choleric"
                Temperament.SANGUINE -> "Sanguine"
                Temperament.PHLEGMATIC -> "Phlegmatic"
                Temperament.MELANCHOLIC -> "Melancholic"
                null -> ""
            }
            educationPeriodView.text = getEducationPeriodString(contact.educationPeriod)
            biographyView.text = contact.biography
        }
    }

    private fun getEducationPeriodString(educationPeriod: EducationPeriod?) = educationPeriod?.let {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val noDate = "*"

        val startDateFormatted =
            educationPeriod.start?.let { startDate -> formatter.format(startDate) } ?: noDate
        val endDateFormatted =
            educationPeriod.end?.let { endDate -> formatter.format(endDate) } ?: noDate

        "$startDateFormatted - $endDateFormatted"
    } ?: ""
}