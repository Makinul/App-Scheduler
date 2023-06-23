package com.makinu.app.scheduler.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    @PrimaryKey
    var packageName: String = "",
    var memberName: String = "",
    var call: String = "",
    var email: String = "",
    var memberImg: String? = null,
    var sex: Int = 0,
    var birthday: String? = null,
    var fatherId: String? = null,
    var motherId: String? = null,
    var spouseId: String? = null,
    var mothersId: String? = null,
    var fathersId: String? = null,
    var approved: Boolean = false,
    var creator: String? = null,
    var createDate: String? = null,
    var updateDate: String? = null
) {
    override fun toString(): String {
        return memberName
    }
}