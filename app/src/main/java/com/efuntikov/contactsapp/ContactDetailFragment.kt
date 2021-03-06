package com.efuntikov.contactsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.efuntikov.contactsapp.domain.entity.EducationPeriod
import com.efuntikov.contactsapp.domain.entity.Temperament
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ContactDetailFragment : Fragment() {

    private lateinit var payload: String

    private lateinit var contactNameView: TextView
    private lateinit var phoneNumberView: TextView
    private lateinit var temperamentView: TextView
    private lateinit var educationPeriodView: TextView
    private lateinit var biographyView: TextView

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactsViewModel

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

        payload = arguments?.getString(ContactsViewModel.CONTACT_ID_ARG_KEY) ?: ""

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

        viewModel.getContactById(payload)?.let { contact ->
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