/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.Option
import arrow.core.Try

// Missing features from Arrow that we want/need

/** Runs the given lambda on the exception contained inside a [Try]'s failure. Returns the original Try. */
fun <T> Try<T>.onFailure(consumer: (Throwable) -> Unit): Try<T> =
    fold({ consumer(it); this }) { this }

/** Runs the given lambda on the value contained inside a [Try]'s success. Returns the original Try. */
fun <T> Try<T>.onSuccess(consumer: (T) -> Unit): Try<T> =
    fold({ this }) { consumer(it); this }

/** Converts a [Try] to a [List] of one or zero elements */
fun <T> Try<T>.asList() = fold({ emptyList<T>() }) { listOf(it) }

/** Converts a [Try] to a [Sequence] of one or zero elements */
fun <T> Try<T>.asSequence() = fold({ emptySequence<T>() }) { sequenceOf(it) }

/** Extracts a [Try] success value or (re-throws) an exception. */
fun <T> Try<T>.getOrFail(): T = fold({ throw it }) { it }

/** Extracts an [Option] some()'s value or throws an exception on none(). */
fun <T> Option<T>.getOrFail(): T =
    fold({ throw IllegalStateException("getOrFail() on an Option.none") }) { it }

// Other random functional functions that we'd like

/** Like [Iterable.associateBy], but if the keySelector returns null, that's ignored and we move onward. */
fun <T, K : Any> Iterable<T>?.associateNotNullBy(keySelector: (T) -> K?): Map<K, T> =
    if (this == null)
        emptyMap()
    else
        flatMap {
            val tmp = keySelector(it)
            if (tmp == null) {
                emptyList()
            } else {
                listOf(tmp to it)
            }
        }.toMap()
