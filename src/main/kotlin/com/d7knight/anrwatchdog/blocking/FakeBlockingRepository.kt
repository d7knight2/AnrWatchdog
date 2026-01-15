import com.d7knight.anrwatchdog.okhttp.FakeOkHttpRepository
import com.d7knight.anrwatchdog.glide.FakeGlideRepository
import com.d7knight.anrwatchdog.experiment.ExperimentCheckRepository

/**
 * Example demonstrating concurrent execution of multiple simulated blocking operations.
 *
 * This file shows how to properly coordinate multiple potentially blocking operations
 * from different repositories (OkHttp for network, Glide for images, experiments)
 * without causing ANR issues.
 *
 * The code launches three separate coroutines that run concurrently and waits
 * for all to complete using the join() pattern.
 */

// Launch OkHttp network simulation
val okhttpJob = launch(Dispatchers.Default + CoroutineName("OkHttp")) {
    println("OkHttp coroutine started")
    FakeOkHttpRepository.performBlockingOperation(2)
}

// Launch Glide image loading simulation
val glideJob = launch(Dispatchers.Default + CoroutineName("Glide")) {
    println("Glide coroutine started")
    FakeGlideRepository.performBlockingOperation(3)
}

// Launch experimental feature operation
val experimentJob = launch(Dispatchers.Default + CoroutineName("Experiment")) {
    println("Experiment coroutine started")
    ExperimentCheckRepository.performNonBlockingOperation(4)
}