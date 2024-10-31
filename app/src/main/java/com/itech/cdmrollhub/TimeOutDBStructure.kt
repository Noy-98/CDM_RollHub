package com.itech.cdmrollhub

import java.io.Serializable

data class TimeOutDBStructure(
    val session_id: String = "", // Primary key or unique identifier
    val time_out_stamp: String = ""
): Serializable
