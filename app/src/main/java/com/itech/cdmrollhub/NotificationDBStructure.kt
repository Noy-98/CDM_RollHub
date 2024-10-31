package com.itech.cdmrollhub

import java.io.Serializable

data class NotificationDBStructure(
    val id: String = "", // Primary key or unique identifier
    val title: String = "",
    val message: String = ""
): Serializable
