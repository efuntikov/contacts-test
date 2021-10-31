package com.efuntikov.contactsapp

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.efuntikov.contactsapp.domain.entity.Contact
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ContactsListFragment : Fragment() {

    private lateinit var progressIndicator: ProgressBar
    private lateinit var contactsList: RecyclerView
    private lateinit var contactsListAdapter: ContactsAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var searchView: SearchView

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactsViewModel
    private val contactsQueryListener = ContactsQueryListener()

    companion object {
        fun newInstance() = ContactsListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContactsApplication.appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.contacts_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)[ContactsViewModel::class.java]

        progressIndicator = view.findViewById(R.id.progressIndicator)
        contactsList = view.findViewById<RecyclerView>(R.id.contactsList).apply {
            contactsListAdapter = ContactsAdapter()
            adapter = contactsListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh).apply {
            setOnRefreshListener { viewModel.fetchContacts(true) }
        }

        searchView = view.findViewById<SearchView>(R.id.searchView).apply {
            setOnQueryTextListener(contactsQueryListener)
        }

        viewModel.getContacts().observe(viewLifecycleOwner) {
            contactsListAdapter.onContactsUpdated()
        }
        viewModel.isLoadingVisible().observe(viewLifecycleOwner) {
            progressIndicator.visibility = if (it) View.VISIBLE else View.GONE
            if (!it) {
                swipeRefresh.isRefreshing = false
            }
        }
        viewModel.getError().observe(viewLifecycleOwner) {
            Snackbar.make(contactsList, it, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.fetchContacts()
    }

    private class ContactsViewHolder(
        private val view: ContactsListItemView
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(contact: Contact) {
            with(view) {
                setName(contact.name ?: "")
                setPhone(contact.phone ?: "")
            }
        }
    }

    private inner class ContactsAdapter : RecyclerView.Adapter<ContactsViewHolder>(), Filterable {

        private var contactsFilter: ContactsFilter? = null
        private var filteredResults: List<Contact>? = null

        fun onContactsUpdated() {
            filteredResults = viewModel.getContacts().value
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ContactsViewHolder(ContactsListItemView(requireContext()).apply {
                setOnClickListener {
                    val position = contactsList.getChildLayoutPosition(it)
                    if (position != NO_POSITION) {
                        viewModel.navigate(
                            ContactsViewModel.Navigation.CONTACT_DETAIL,
                            position.toString()
                        )
                    }
                }
            })

        override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
            filteredResults?.let { holder.bind(it[position]) }
        }

        override fun getItemCount() = filteredResults?.size ?: 0

        override fun getFilter(): Filter {
            if (contactsFilter == null) {
                contactsFilter = ContactsFilter()
            }

            return contactsFilter as ContactsFilter
        }

        private inner class ContactsFilter : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                if (TextUtils.isEmpty(constraint)) {
                    viewModel.getContacts().value?.let {
                        results.values = it
                        results.count = it.size
                    } ?: run {
                        results.values = emptyList<Contact>()
                        results.count = 0
                    }
                } else {
                    viewModel.getContacts().value?.let {
                        val filteredResult = arrayListOf<Contact>()
                        it.forEach { contact ->
                            var matches = false
                            contact.name?.let { name ->
                                matches = matches or name.contains(constraint, true)
                            }
                            contact.phone?.let { phone ->
                                matches = matches or phone.contains(constraint, true)
                            }

                            if (matches) {
                                filteredResult.add(contact)
                            }
                        }

                        results.values = filteredResult
                        results.count = filteredResult.size
                    } ?: run {
                        results.values = emptyList<Contact>()
                        results.count = 0
                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredResults = results.values as List<Contact>
                notifyDataSetChanged()
            }
        }
    }

    private inner class ContactsQueryListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            contactsListAdapter.filter.filter(newText);
            return false;
        }

    }
}
