package com.efuntikov.contactsapp

inline val <reified T> T.LOG_TAG: String
    get() = if (T::class.java.simpleName.length > 23) {
        "+++" + T::class.java.simpleName.substring(0, 23)
    } else {
        "+++" + T::class.java.simpleName
    }