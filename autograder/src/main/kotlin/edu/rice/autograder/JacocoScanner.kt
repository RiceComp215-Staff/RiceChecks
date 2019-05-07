/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.module.kotlin.readValue

// This is easily the most complicated XML we'll have to deal with. The original XML
// is one giant line, so check out the more human-readable version in:
// src/test/resources/comp215-build/reports/jacoco/test/jacocoTestReport-Reformatted.xml

// The XML structure:
// - the top-level report has a list of packages
//   - each package has a list of classes
//     - each class has a list of methods
//       - each method has a list of counters
//     - each class also has a list of counters
//       - each counter has three attributes: type (string), missed (number), and covered (number)
//   - each package also has a list of sourcefiles
//     - each sourcefile has a list of "lines" (unclear what we're supposed to do with this)
//     - each sourcefile also has a list of counters
//   - each package also has a list of counters
//  - the report also has a list of counters

@JsonRootName("report")
data class JacocoReport(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var name: String? = null,

    @set:JsonProperty("sessioninfo")
    var session: JacocoSession? = null,

    @set:JsonProperty("package")
    var packages: List<JacocoPackage>? = null,

    @set:JsonProperty("counter")
    var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val packageMap by lazy {
        packages.associateNotNullBy { it.name }
    }

    val classesMap by lazy {
        packageMap.values
            .map { it.classMap }
            .fold(emptyMap<String, JacocoClass>()) { a, b -> a + b }
    }
}

@JsonRootName("sessioninfo")
data class JacocoSession(
    @set:JacksonXmlProperty(localName = "id", isAttribute = true)
    var id: String? = null,

    @set:JacksonXmlProperty(localName = "start", isAttribute = true)
    var start: Long = 0,

    @set:JacksonXmlProperty(localName = "dump", isAttribute = true)
    var dump: Long = 0
)

@JsonRootName("package")
data class JacocoPackage(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var name: String? = null,

    @set:JsonProperty("class")
    var classes: List<JacocoClass>? = null,

    @set:JsonProperty("counter")
    var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    // Note: we're standardizing class names in classMap to use dots rather than slashes
    // and dollar-signs, making them consistent with everything else in RiceChecks.
    val classMap: Map<String, JacocoClass> by lazy {
        classes.associateNotNullBy {
            it.name.fixClassName()
        }
    }
}

@JsonRootName("class")
data class JacocoClass(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var name: String? = null,

    @set:JacksonXmlProperty(localName = "sourcefilename", isAttribute = true)
    var sourceFileName: String? = null,

    @set:JsonProperty("method")
    var methods: List<JacocoMethod>? = null,

    @set:JsonProperty("counter")
    var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val methodMap by lazy {
        methods.associateNotNullBy { it.name }
    }
}

@JsonRootName("method")
data class JacocoMethod(
    @set:JacksonXmlProperty(localName = "name", isAttribute = true)
    var name: String? = null,

    @set:JacksonXmlProperty(localName = "desc", isAttribute = true)
    var desc: String? = null,

    @set:JacksonXmlProperty(localName = "line", isAttribute = true)
    var line: Int = 0,

    @set:JsonProperty("counter")
    var counters: List<JacocoCounter>? = null
) {

    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }
}

@JsonRootName("sourcefile")
data class JacocoSourceFile(
    @set:JsonProperty("line")
    var lines: List<JacocoLine>? = null,

    @set:JsonProperty("counter")
    var counters: List<JacocoCounter>? = null
) {
    val counterMap by lazy {
        counters.associateNotNullBy { it.type }
    }

    val lineMap by lazy {
        lines.associateNotNullBy { it.nr }
    }
}

enum class JacocoCounterType {
    INSTRUCTION, BRANCH, LINE, COMPLEXITY, METHOD, CLASS
}

@JsonRootName("counter")
data class JacocoCounter(
    @set:JacksonXmlProperty(localName = "type", isAttribute = true)
    var type: JacocoCounterType? = null,

    @set:JacksonXmlProperty(localName = "missed", isAttribute = true)
    var missed: Int = 0,

    @set:JacksonXmlProperty(localName = "covered", isAttribute = true)
    var covered: Int = 0
)

@JsonRootName("line")
data class JacocoLine(
    @set:JacksonXmlProperty(localName = "nr", isAttribute = true)
    var nr: Int = 0,

    @set:JacksonXmlProperty(localName = "mi", isAttribute = true)
    var mi: Int = 0,

    @set:JacksonXmlProperty(localName = "ci", isAttribute = true)
    var ci: Int = 0,

    @set:JacksonXmlProperty(localName = "mb", isAttribute = true)
    var mb: Int = 0,

    @set:JacksonXmlProperty(localName = "cb", isAttribute = true)
    var cb: Int = 0
)

/**
 * Given a string -- the rest of reading a Jacoco XML results file --
 * returns a [JacocoReport] data class, suitable for subsequent queries.
 */
fun jacocoParser(fileData: String): JacocoReport? {
    Log.i(TAG, "jacocoParser: ${fileData.length} bytes")

    return if (fileData.isEmpty()) null
    else jacksonXmlMapper.readValue(fileData)
}

fun GCoverageStyle.toJacocoCounterType() = when (this) {
    GCoverageStyle.INSTRUCTIONS -> JacocoCounterType.INSTRUCTION
    GCoverageStyle.LINES -> JacocoCounterType.LINE
}

fun GGradeProject.jacocoResultsMissing() =
    EvaluatorResult(false, 0.0, coveragePoints,
        "No test coverage results found", COVERAGE_CATEGORY, emptyList())

private const val TAG = "JacocoScanner"

private infix fun String.subClassOf(classOrPackageName: String): Boolean = when {
    this == classOrPackageName -> true
    this.startsWith("$classOrPackageName.") -> true
    else -> false
}

fun JacocoReport.matchingClassSpecs(coverages: List<GGradeCoverage>): List<String> {
    // In our GGradeProject structure, the names of classes are going to be in normal human
    // form (e.g., edu.rice.autograder.test.Project3) while they'll be in slashy form in
    // the JacocoReport, which we fixed in classesMap (see calls to String?.fixClassName).

    Log.i(TAG, "looking for classes covered by: $coverages")

    val packageSpecs = coverages
        .filter { it.scope == GCoverageScope.PACKAGE }
        .sortedBy { it.name }
    val classSpecs = coverages
        .filter { it.scope == GCoverageScope.CLASS }
        .sortedBy { it.name }

    Log.i(TAG, "possible packages: $packageSpecs")
    Log.i(TAG, "possible classes: $classSpecs")

    return classesMap.keys.filter { className ->
        // We're working our way down from the most general to the most specific package annotation
        // then the most general to the most specific class annotation (the sorting above is essential
        // to make this happen). The logic here is that the last relevant annotation wins, so an inner
        // "including" annotation overrides an external "excluding" annotation.

        val matchingPackages = packageSpecs.filter { className subClassOf it.name }
        val matchingClasses = classSpecs.filter { className subClassOf it.name }

        val allSpecs = matchingPackages + matchingClasses

        if (allSpecs.isNotEmpty()) {
            Log.i(TAG, "for class ($className), found possible specs: $allSpecs")
            allSpecs.fold(false) { _, next -> !next.excluded }
        } else false
    }
}

// literal dollar, one or more digits, end of string
private val anonymousInnerClassRegex = Regex("\\\$[0-9]+\$")
private val dollarsAndDigitsRegex = Regex("\\\$[$\\d]+")

/**
 * Determines if a class name in JaCoCo's syntax (with /'s and $'s) is
 * an anonymous inner class, based on ending with a dollar sign followed
 * by a number.
 */
fun String.isAnonymousInnerClass() = contains(anonymousInnerClassRegex)

/**
 * Determines if a class name in JaCoCo's syntax (with /'s and $'s) is
 * an anonymous inner class of another (also in JaCoCo's syntax). This
 * will also match *nested* anonymous inner classes.
 */
infix fun String?.isAnonymousInnerClassOf(name: String?) =
    this != null && name != null &&
        isAnonymousInnerClass() &&
        startsWith(name) &&
        drop(name.length).matches(dollarsAndDigitsRegex)

fun JacocoReport?.eval(project: GGradeProject): EvaluatorResult {
    if (this == null) {
        return project.jacocoResultsMissing()
    }

    if (project.coveragePoints == 0.0)
        return passingEvaluatorResult(0.0, "No test coverage requirement", COVERAGE_CATEGORY)

    val counterType = project.coverageStyle.toJacocoCounterType()

    val matchingClassNames = matchingClassSpecs(project.coverageAnnotations).sorted()

    val matchingClasses = matchingClassNames.map {
        val jacocoRecord = classesMap[it]
            ?: Log.ethrow(TAG, "internal failure: can't find $it")
        val name = classesMap[it]?.name
            ?: Log.ethrow(TAG, "internal failure: class $it is missing a name from JaCoCo")

        jacocoRecord to name.isAnonymousInnerClass()
    }

    if (matchingClasses.isEmpty()) {
        Log.e(TAG, "No classes found to test for coverage!")
        matchingClassNames.forEach {
            Log.e(TAG, " -- matchingClassName: $it")
        }
        classesMap.keys.forEach {
            Log.e(TAG, " -- classesMap[$it] = ${classesMap[it]}")
        }
        return EvaluatorResult(false, 0.0, project.coveragePoints,
            "Test coverage: no classes specified for coverage!", COVERAGE_CATEGORY, emptyList())
    }

    // Turns out, an interface with no default methods still appears in our data as a regular
    // class, but JaCoCo doesn't assign any counters to it. Since they have no code, there's
    // nothing to evaluate for coverage, and thus we filter them out here.

    val regularClasses = matchingClasses
        .filter { !it.second }
        .map { it.first }
        .filter { it.counters != null && it.counterMap[JacocoCounterType.LINE] != null }

//    regularClasses.forEach { Log.i(TAG, "Regular class: ${it.name} (with counters)") }

    val anonymousClasses = matchingClasses
        .filter { it.second }
        .map { it.first }
        .filter { it.counters != null && it.counterMap[JacocoCounterType.LINE] != null }

//    anonymousClasses.forEach { Log.i(TAG, "Anonymous class: ${it.name} (with counters)") }

    Log.i(TAG, "Matching classes: ${matchingClasses.size}")
    Log.i(TAG, "Regular classes with coverage counters: ${regularClasses.size}")
    Log.i(TAG, "Anonymous inner classes with coverage counters: ${anonymousClasses.size}")

    val coverageReport = regularClasses.flatMap { classRecord ->
        val name = classRecord.name ?: Log.ethrow(TAG, "no name found for $classRecord")

        val matchingAnonymousRecords = anonymousClasses.filter {
            it.name isAnonymousInnerClassOf name
        }

        val numAnonInner = matchingAnonymousRecords.size

        if (numAnonInner > 0) {
            Log.i(TAG, "Found $numAnonInner anonymous inner classes for $name")
        }

        val allClassRecords = matchingAnonymousRecords + classRecord

        val covered = allClassRecords.sumBy {
            it.counterMap[counterType]?.covered
                ?: Log.ethrow(TAG, "no coverage numbers found for $it")
        }

        val missed = allClassRecords.sumBy {
            it.counterMap[counterType]?.missed
                ?: Log.ethrow(TAG, "no missed numbers found for $it")
        }

        if (covered + missed == 0)
            emptyList()
        else {
            val percentage = 100.0 * covered / (covered + missed).toDouble()
            val classNameFixed = name.fixClassName()
            val coverageStr = "Coverage of $classNameFixed: %.1f%% ($covered/${covered + missed})%s"
                .format(percentage, when {
                    numAnonInner == 0 -> ""
                    numAnonInner == 1 -> "\n(including one anonymous inner class)"
                    else -> "\n(including $numAnonInner anonymous inner classes)"
                })
            Log.i(TAG, coverageStr)
            listOf(CoverageDeduction(coverageStr, 0.0, classNameFixed, percentage))
        }
    }

    val fails = coverageReport.filter {
        it.coveragePercentage < project.coveragePercentage
    }

    val wins = coverageReport.filter {
        it.coveragePercentage >= project.coveragePercentage
    }

    val passing = fails.isEmpty()
    val counterTypeStr = "(by ${counterType.toString().toLowerCase()})"

    return if (passing) {
        EvaluatorResult(true, project.coveragePoints, project.coveragePoints,
            "Test coverage meets %.0f%% %s requirement"
                .format(project.coveragePercentage, counterTypeStr),
            COVERAGE_CATEGORY,
            wins)
    } else {
        EvaluatorResult(false, 0.0, project.coveragePoints,
            "Classes with coverage below %.0f%% %s requirement"
                .format(project.coveragePercentage, counterTypeStr),
            COVERAGE_CATEGORY,
            fails +
                listOf(BasicDeduction("See the coverage report for details:\n" +
                    "${AutoGrader.buildDir}/reports/jacoco/index.html", 0.0)))
    }
}

fun String?.fixClassName() =
        this?.replace('/', '.')
            ?.replace('$', '.')
            ?: ""
