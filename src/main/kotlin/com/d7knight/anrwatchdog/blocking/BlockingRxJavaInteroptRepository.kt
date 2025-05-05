package com.d7knight.anrwatchdog.blocking

import com.d7knight.anrwatchdog.rxjava.SlowRxExperimentEnabledRepository
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

object BlockingRxJavaInteroptRepository {
    suspend fun performBlockingRxOperation(index: Int) {
        withContext(Dispatchers.IO) {
            val result = SlowRxExperimentEnabledRepository.performSlowOperation(index).asFlow().toList()
            result.forEach { item: String -> println(item) }
        }
    }
}