/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

/**
 * The expectation is that every grade "evaluator" will return one of these, or
 * maybe a list of them, indicating whether full marks were given and how many
 * points were awarded.
 *
 * The pairs of string/double for the deductions field are for printing
 * to the user to explain what went right or wrong. For something that
 * went correctly, the string might be "googleJavaStyle: 35/35 files formatted correctly"
 * with a deduction of 0.0. For something that went wrong, the string will
 * give correspondingly useful information and the **deduction should have
 * a positive number**. They'll be printed appropriately later.
 *
 * Note that this class doesn't do any arithmetic on the deductions or
 * points. The expectation is that the [deductions] listed here will be printed,
 * but not accumulated, and the [points] will be printed *and* accumulated.
 * Similarly, the [passes] fields will be subject to a boolean-and to discover
 * whether the entire project passes.
 */
data class EvaluatorResult(
    val passes: Boolean,
    val points: Double,
    val maxPoints: Double,
    val title: String,
    val deductions: List<Pair<String, Double>>
)

fun passingEvaluatorResult(happyPoints: Double = 0.0, happyString: String = "") =
    EvaluatorResult(true, happyPoints, happyPoints, happyString, emptyList())
