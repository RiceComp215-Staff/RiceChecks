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
    val title: String,
    val category: String,
    val deductions: List<Deduction>
)

/**
 * The pairs of string/double for the deductions field are for printing
 * to the user to explain what went right or wrong. For something that
 * went correctly, the [description] might be `googleJavaStyle: 35/35 files formatted correctly`
 * with a [cost] of 0.0. For something that went wrong, the description will
 * give correspondingly useful information and the **cost should have
 * a positive number**. They'll be printed appropriately later.
 *
 * All of the implementations of the Deduction interface allow additional
 * metadata that we'll (likely) ignore in our human-readable output, but
 * which will appear in the machine-readable output.
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

fun passingEvaluatorResult(happyPoints: Double = 0.0, happyTitle: String, happyCategory: String) =
    EvaluatorResult(true, happyPoints, happyPoints, happyTitle, happyCategory, emptyList())
