package com.nrvz.prebuildapplication.network

import com.nrvz.prebuildapplication.constants.AppConst
import com.nrvz.prebuildapplication.network.dto.PostDto
import retrofit2.http.GET

/**
 * ============================================================================
 *  ApiService — the HTTP API, declared as an interface.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * You never write the implementation. Retrofit builds it at runtime from these
 * annotations, and the instance is created once in utilities/di/NetworkModule.kt.
 *
 *   @GET("posts")  ->  GET {BuildConfig.BASE_API_URL} + posts
 *                      (the base URL ends with "/" and the path has no leading
 *                      "/", otherwise the last path segment of the base gets
 *                      replaced - a classic silent 404)
 *
 * `suspend` means the call is main-thread-safe: it parks the coroutine and
 * resumes on the caller's dispatcher. NO AsyncTask, NO callbacks, NO
 * `enqueue()`. If a call needs to be cancelled when the user leaves the screen,
 * that is free - it rides on viewModelScope.
 *
 * RockyGo's equivalent is network/AppService.kt (plus ChatService, GoogleService).
 *
 * YOUR TURN
 * ---------
 * Add a call that fetches ONE post by id and returns a single PostDto:
 *   @GET("posts/{id}")
 *   suspend fun getPost(@Path("id") id: Long): PostDto
 * Remember the import for @Path.
 */
interface ApiService {

    /** GET https://jsonplaceholder.typicode.com/posts -> a JSON array of posts. */
    @GET(AppConst.BASE_API)
    suspend fun getPosts(): List<PostDto>
}
