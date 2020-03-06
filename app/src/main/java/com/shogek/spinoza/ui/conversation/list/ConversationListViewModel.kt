package com.shogek.spinoza.ui.conversation.list

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.contact.ContactRoomDatabase
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val contactDao = ContactRoomDatabase.getDatabase(application, viewModelScope).contactDao()
    private val conversationDao = ConversationRoomDatabase.getDatabase(application, viewModelScope).conversationDao()
    val conversations: LiveData<List<Conversation>> = conversationDao.getAll()

    // TODO: Combine 'Conversation' and 'Contact' streams
//    init {
//        conversationDao.getAll().combineWith(contactDao.getAll()) { conversations, contacts ->
//            this.conversations = conversations
//        }
//    }

    private fun <T, K, R> LiveData<T>.combineWith(
        liveData: LiveData<K>,
        block: (T?, K?) -> R
    ): LiveData<R> {
        val result = MediatorLiveData<R>()
        result.addSource(this) {
            result.value = block.invoke(this.value, liveData.value)
        }
        result.addSource(liveData) {
            result.value = block.invoke(this.value, liveData.value)
        }
        return result
    }

    fun insert(conversation: Conversation) = viewModelScope.launch {
        conversationDao.insert(conversation)
    }

    fun archiveConversation(id: Long) {
        // TODO: [Feature] Implement archive conversation functionality
        Toast.makeText(this.context, "Archive: $id", Toast.LENGTH_SHORT).show()
    }

    fun deleteConversation(id: Long) = viewModelScope.launch {
        val conversation = conversationDao.get(id)
        if (conversation.contact != null) {
            contactDao.delete(conversation.contact!!)
        }
        conversationDao.delete(conversation)
        Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
    }

    fun muteConversation(id: Long) {
        // TODO: [Feature] Implement mute conversation functionality
        Toast.makeText(this.context, "Mute: $id", Toast.LENGTH_SHORT).show()
    }

    fun markAsUnreadConversation(id: Long) {
        // TODO: [Feature] Implement mark conversation as unread functionality
        Toast.makeText(this.context, "Mark unread: $id", Toast.LENGTH_SHORT).show()
    }

    fun ignoreConversation(id: Long) {
        // TODO: [Feature] Implement ignore conversation functionality
        Toast.makeText(this.context, "Ignore: $id", Toast.LENGTH_SHORT).show()
    }

    fun blockConversation(id: Long) {
        // TODO: [Feature] Implement block conversation functionality
        Toast.makeText(this.context, "Block: $id", Toast.LENGTH_SHORT).show()
    }
}