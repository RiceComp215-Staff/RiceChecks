//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import arrow.core.Option
import arrow.core.Try
import arrow.core.failure

// Missing features from Arrow that we want/need

/** Runs the given lambda on the exception contained inside a [Try]'s failure. Returns the original Try. */
fun <T> Try<T>.onFailure(consumer: (Throwable) -> Unit): Try<T> = fold({ consumer(it); this}) { this }

/** Converts a [Try] to a [List] of one or zero elements */
fun <T> Try<T>.asList() = fold({ emptyList<T>() }, { listOf(it) })

/** Converts a [Try] to a [Sequence] of one or zero elements */
fun <T> Try<T>.asSequence() = fold({ emptySequence<T>() }, { sequenceOf(it) })

/** Extracts a [Try] success value or throws an exception. */
fun <T> Try<T>.getOrFail(): T = fold( { throw RuntimeException(it.message) }, { it } )
