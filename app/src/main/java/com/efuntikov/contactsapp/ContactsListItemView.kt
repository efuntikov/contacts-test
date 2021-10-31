package com.efuntikov.contactsapp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView

class ContactsListItemView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var nameView: TextView
    private var phoneView: TextView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.contact_list_item, this)

        nameView = findViewById(R.id.nameView)
        phoneView = findViewById(R.id.phoneView)
    }

    fun setName(name: String) {
        nameView.text = name
    }

    fun setPhone(phone: String) {
        phoneView.text = phone
    }
}