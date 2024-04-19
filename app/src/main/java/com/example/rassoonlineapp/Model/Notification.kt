package com.example.rassoonlineapp.Model

data class Notification(
    internal var userId: String = "",
    private var postId: String = "",
    private var ispost: Boolean = false,
    var userName: String? = "",
    var userProfileImage: String? = "",
    var postTitle: String? = ""
)
