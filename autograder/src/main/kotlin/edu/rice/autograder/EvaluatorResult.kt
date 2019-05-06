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
    val title: String, // meant for the human-readable report
    val category: String, // meant for the machine-readable report
    val deductions: List<Deduction>
)

/**
 * The pairs of string/double for the deductions field are for printing
 * to the user to explain what went right or wrong. For something that
 * went correctly, the [description] might be `googleJavaStyle: 35/35
 * files formatted correctly` with a [cost] of 0.0. For something that
 * went wrong, the description will give correspondingly useful information
 * and the **cost should have a positive number**. They'll be printed with
 * negative signs added later.
 *
 * The human-readable report generator only pays attention to the
 * [description] and [cost] fields, which are defined here in the Deduction
 * interface. All the concrete implementations of Deduction, below, are then
 * required to implement these two properties and then whatever else they
 * want, which is only used for the machine-readable reports. See the
 * [ResultsReport] class and its associated methods for details.
 */
interface Deduction {
    val description: String
    val cost: Double

    operator fun component1() = description
    operator fun component2() = cost
}

data class BasicDeduction(
    override val description: String,
    override val cost: Double
) : Deduction

data class UnitTestDeduction(
    override val description: String,
    override val cost: Double,
    val testName: String
) : Deduction

data class UnitTestFactoryDeduction(
    override val description: String,
    override val cost: Double,
    val testName: String,
    val numPassed: Int,
    val numChecked: Int
) : Deduction

data class CodeStyleDeduction(
    override val description: String,
    override val cost: Double,
    val toolName: String,
    val section: String,
    val passing: Boolean,
    val numPassed: Int,
    val numChecked: Int
) : Deduction

data class CoverageDeduction(
    override val description: String,
    override val cost: Double,
    val className: String,
    val coveragePercentage: Double
) : Deduction

/** When we have a passing result with no deductions to report, this simplifies things. */
fun passingEvaluatorResult(happyPoints: Double, happyTitle: String, happyCategory: String) =
    EvaluatorResult(true, happyPoints, happyPoints, happyTitle, happyCategory, emptyList())

// Engineering notes:
// There are many really cool things going on here. First is just Kotlin being great.
// We're defining the Deduction interface to have two "properties" which every
// implementing class is then required to override. This isn't class inheritance.
// If this were in Java, the interface would define getters for description and
// cost, and each class would override those two methods. But because it's Kotlin,
// those getters are coated in nice layer of syntactic sugar and become "properties".

// Also Kotlin being great are the "operator fun componentN()" methods, which
// allow for all of these classes to be "destructured" when they're the argument
// to a lambda or whatever else.

// Lastly, these data classes are directly read by Jackson (no doubt using Java
// reflection under the hood), so what you see here maps one-to-one with what's
// ultimately written out in our machine-readable reports.
