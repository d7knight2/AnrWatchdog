import com.d7knight.anrwatchdog.network.FakeOkHttpRepository
import com.d7knight.anrwatchdog.graphics.FakeGlideRepository
import com.d7knight.anrwatchdog.experimental.ExperimentCheckRepository

val okhttpJob = launch(Dispatchers.Default + CoroutineName("OkHttp")) {
    println("OkHttp coroutine started")
    FakeOkHttpRepository.performBlockingOperation(2)
}

val glideJob = launch(Dispatchers.Default + CoroutineName("Glide")) {
    println("Glide coroutine started")
    FakeGlideRepository.performBlockingOperation(3)
}

val experimentJob = launch(Dispatchers.Default + CoroutineName("Experiment")) {
    println("Experiment coroutine started")
    ExperimentCheckRepository.performNonBlockingOperation(4)
}