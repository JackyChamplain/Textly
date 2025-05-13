package com.example.messageapp.utilities

sealed class Routes(val route:String) { // This class is going to work with the controller
    // this is a enum type class, that works with Kotlin interface files
    data object Home: Routes("home")
    data object AddContact: Routes("addcontact")
    data object Settings: Routes("settings")
}