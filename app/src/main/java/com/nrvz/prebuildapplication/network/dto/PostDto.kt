package com.nrvz.prebuildapplication.network.dto

/**
 * ============================================================================
 *  PostDto — the shape of ONE object as the SERVER sends it.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * "Dto" = Data Transfer Object. This class exists to mirror the JSON exactly and
 * nothing else. It is the ONLY class that should care what the API looks like.
 *
 * WHY SEPARATE FROM `models/Note`?
 *   The API here calls it a "Post" with a numeric `id` and no tags. Our app calls
 *   it a "Note" with tags and a created-at. If we used one class for both, a
 *   server-side rename would ripple through the adapter, the ViewModel and the
 *   layouts. Repositories translate Dto -> domain, so the rest of the app never
 *   notices.
 *
 * Gson maps JSON keys to these property names. A property with no matching JSON
 * key simply stays null/default. Never rename one of these without changing the
 * API contract.
 *
 * @SerializedName is how you decouple a Kotlin name from a weird JSON key, e.g.
 * @SerializedName("post_id") val postId: Long.
 */
data class PostDto(
    val id: Long,
    val userId: Long = 0L,
    val title: String = "",
    val body: String = "",
)
