package com.umbiko.app.ui.nav

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_POST = "create_post"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    fun postDetail(postId: String) = "post/$postId"
}
