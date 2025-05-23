package com.example.messageapp

import android.app.Application
import com.example.messageapp.contact.ContactViewModel
import com.example.messageapp.roomdb.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private lateinit var contactDao: ContactDao
    private lateinit var messageDao: MessageDao
    private lateinit var viewModel: ContactViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val mockApp: Application = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contactDao = mock()
        messageDao = mock()
        viewModel = ContactViewModel(mockApp, contactDao, messageDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addContact should insert contact`() = runTest {
        val contact = Contact(name = "Test", phoneNumber = "+1234567890", priority = Priority.REGULAR)

        viewModel.addContact(contact)
        testScheduler.advanceUntilIdle()

        verify(contactDao).insert(eq(contact))
    }

    @Test
    fun `removeContact should delete contact`() = runTest {
        val contact = Contact(name = "DeleteMe", phoneNumber = "+1111111111")

        viewModel.removeContact(contact)
        testScheduler.advanceUntilIdle()

        verify(contactDao).delete(eq(contact))
    }

    @Test
    fun `updateContactGroup should update only if group is different`() = runTest {
        val contactId = "abc123"
        val oldContact = Contact(id = contactId, name = "Bob", phoneNumber = "+999999999", group = ContactGroup.BUSINESS)
        whenever(contactDao.getContactById(contactId)).thenReturn(oldContact)

        viewModel.updateContactGroup(contactId, ContactGroup.PERSONAL)
        testScheduler.advanceUntilIdle()

        verify(contactDao).insert(check {
            assert(it.group == ContactGroup.PERSONAL)
        })
    }
}
