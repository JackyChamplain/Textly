package com.example.messageapp

import com.example.messageapp.roomdb.Converters
import com.example.messageapp.roomdb.ContactGroup
import com.example.messageapp.roomdb.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `convert Priority to and from string`() {
        val original = Priority.HIGH
        val string = converters.fromPriority(original)
        val result = converters.toPriority(string)
        assertEquals(original, result)
    }

    @Test
    fun `convert ContactGroup to and from string`() {
        val original = ContactGroup.PERSONAL
        val string = converters.fromContactGroup(original)
        val result = converters.toContactGroup(string)
        assertEquals(original, result)
    }

    @Test
    fun `convert Priority LOW correctly`() {
        val original = Priority.LOW
        val string = converters.fromPriority(original)
        val result = converters.toPriority(string)
        assertEquals(original, result)
    }

    @Test
    fun `convert ContactGroup SPAM correctly`() {
        val original = ContactGroup.SPAM
        val string = converters.fromContactGroup(original)
        val result = converters.toContactGroup(string)
        assertEquals(original, result)
    }
}
