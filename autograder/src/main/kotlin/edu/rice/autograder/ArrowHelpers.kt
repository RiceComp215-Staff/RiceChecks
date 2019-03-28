//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import arrow.core.Try
import arrow.core.failure

// Missing features from Arrow that we want/need

/** Runs the given lambda on the exception contained inside a [Try]'s failure. Returns the original Try. */
fun <T> Try<T>.onFailure(consumer: (Throwable) -> Unit): Try<T> = fold({ consumer(it); this}) { this }
