package com.itech.cdmrollhub

import java.io.Serializable

data class AttendanceDBStructure(
    val session_id: String = "", // Primary key or unique identifier
    val date_stamp: String = ""
): Serializable
