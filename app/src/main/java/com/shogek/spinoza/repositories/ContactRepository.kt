package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.utils.PhoneUtils

object ContactRepository {
    /** [ContactsContract.CommonDataKinds.Phone.NUMBER] returns Contact */
    private val contacts: HashMap<String, Contact> = HashMap()

    /** This is bad, because it gets every single contact instead of filtering by phone numbers */
    fun getAll(resolver: ContentResolver, clearCache: Boolean = false): MutableCollection<Contact> {
        // TODO: [Refactor] Use state
        // TODO: [Refactor] Return a read-only collection
        if (this.contacts.isNotEmpty() && !clearCache)
            return this.contacts.values

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = null

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
            ?: return mutableListOf()

        while (cursor.moveToNext()) {
            val id      = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
            val number  = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            /*
                TODO: [Refactor] Figure out when it is set and use it instead of comparing 'Phone.NUMBER'
                To my understanding, the 'Phone.NUMBER' is separated to distinct parts,         ex.: "+372 512 4788"
                while 'Phone.NORMALIZED_NUMBER' matches what we get from 'Conversations',       ex.: "+3725124788"
             */
            val e164    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            val name    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val photo   = cursor.getStringOrNull(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val strippedPhone = PhoneUtils.getStrippedPhone(number)

            this.contacts[strippedPhone] = Contact(id, name, strippedPhone, number, e164, photo)
        }

        cursor.close()
        return this.contacts.values
    }
}