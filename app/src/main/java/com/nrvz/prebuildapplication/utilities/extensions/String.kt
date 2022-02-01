package com.nrvz.prebuildapplication.utilities.extensions

const val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"

fun String.isEmailValid(): Boolean {
    return EMAIL_REGEX.toRegex().matches(this)
}