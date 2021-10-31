package com.efuntikov.contactsapp.domain.entity

import com.google.gson.annotations.SerializedName
import java.util.*

data class Contact(
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("biography")
    var biography: String? = null,
    @SerializedName("temperament")
    var temperament: Temperament? = null,
    @SerializedName("educationPeriod")
    var educationPeriod: EducationPeriod? = null
)

enum class Temperament {
    @SerializedName("melancholic")
    MELANCHOLIC,
    @SerializedName("phlegmatic")
    PHLEGMATIC,
    @SerializedName("sanguine")
    SANGUINE,
    @SerializedName("choleric")
    CHOLERIC
}

data class EducationPeriod(
    @SerializedName("start")
    var start: Date? = null,
    @SerializedName("end")
    var end: Date? = null
)
