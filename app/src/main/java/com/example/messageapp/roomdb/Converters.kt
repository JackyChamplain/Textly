package com.example.messageapp.roomdb

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromContactGroup(group: ContactGroup): String {
        return group.name
    }

    @TypeConverter
    fun toContactGroup(value: String): ContactGroup {
        return ContactGroup.valueOf(value)
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
}
