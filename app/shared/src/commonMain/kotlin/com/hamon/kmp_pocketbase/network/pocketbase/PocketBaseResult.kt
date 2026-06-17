package com.hamon.kmp_pocketbase.network.pocketbase

import kotlinx.coroutines.CancellationException

sealed class PocketBaseResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : PocketBaseResult<T>()

    data class Failure(
        val exception: Throwable,
    ) : PocketBaseResult<Nothing>()

    fun getOrNull(): T? = (this as? Success)?.data

    fun exceptionOrNull(): Throwable? = (this as? Failure)?.exception

    @Generated
    inline fun onSuccess(action: (T) -> Unit): PocketBaseResult<T> {
        if (this is Success) action(data)
        return this
    }

    @Generated
    inline fun onFailure(action: (Throwable) -> Unit): PocketBaseResult<T> {
        if (this is Failure) action(exception)
        return this
    }

    @Generated
    inline fun <R> map(transform: (T) -> R): PocketBaseResult<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Failure -> this
        }
}

@PublishedApi
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal suspend fun <T> safeSuspend(block: suspend () -> T): PocketBaseResult<T> =
    try {
        PocketBaseResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        PocketBaseResult.Failure(e)
    }
