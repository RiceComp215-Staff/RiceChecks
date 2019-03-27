//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Seq
import io.vavr.collection.Stream
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import java.util.*

// This file contains helper functions to add features to VAVR that it doesn't include
// but that we need in various places.

/**
 * Converts from an old-school Java [Enumeration] to a VAVR [Stream], with any
 * possible null values removed.
 */
fun <T> Enumeration<T>.toVavrStream(): Stream<T> =
        Stream.iterate { if (hasMoreElements()) some<T>(nextElement()) else none() }
                .filter { it != null }

