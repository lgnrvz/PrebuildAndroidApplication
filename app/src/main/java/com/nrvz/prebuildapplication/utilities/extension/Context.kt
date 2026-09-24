package com.nrvz.prebuildapplication.utilities.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.nrvz.prebuildapplication.R

/**
 * ============================================================================
 *  Context extensions — small helpers used everywhere.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * An extension function adds a method to a class you do not own. `fun
 * Context.showToast(...)` lets you write `context.showToast("hi")` and reads as
 * if Toast had always been a Context method.
 *
 * RULE: keep them SMALL and side-effect-named. `showToast`, `hasInternet` - you
 * can guess what they do. An extension that quietly writes to a database belongs
 * in a repository, not in an extension file.
 *
 * RockyGo equivalent: utilities/extension/Context.kt.
 */

/**
 * Is there a usable network right now?
 *
 * GOTCHA (this bites everybody): the check below does NOT verify that the network
 * is actually USABLE. A captive-portal wifi ("sign in to continue") reports
 * TRANSPORT_WIFI and returns true, and then every request fails. The complete
 * check also requires NET_CAPABILITY_VALIDATED. RockyGo's version has the same
 * limitation - if you need airtight detection, add the capability check here and
 * then tell your lead, because it affects every app at once.
 */
fun Context.hasInternet(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(
        connectivityManager.activeNetwork
    )
    return capabilities?.let {
        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } ?: false
}

fun Context.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.showToastLong(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.showToast(messageResId: Int) =
    Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()

/** Convenience for the "you are offline" path, so the wording stays consistent. */
fun Context.showNoInternetToast() =
    showToast(getString(R.string.check_internet_connection))
