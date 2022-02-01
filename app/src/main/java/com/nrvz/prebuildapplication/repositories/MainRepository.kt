package com.nrvz.prebuildapplication.repositories

import com.nrvz.prebuildapplication.database.AppDatabase
import com.nrvz.prebuildapplication.utilities.preference.SharedPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Author: Name
 * Created on 1 Feb 2022 - 7:11 PM
 * Login User: L
 * Project: PrebuildApplication
 */
class MainRepositoryRepository(
    private val sharedPrefs: SharedPrefs,
    private val database: AppDatabase
) {

    suspend fun functionMainRepository() {
        withContext(Dispatchers.IO) {

        }
    }
}