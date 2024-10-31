package com.itech.cdmrollhub

import java.io.Serializable

data class TimeInDBStructure(
    val session_id: String = "", // Primary key or unique identifier
    val time_in_stamp: String = ""
): Serializable
