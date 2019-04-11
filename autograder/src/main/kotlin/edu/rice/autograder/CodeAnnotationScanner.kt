/*
 * AnnoAutoGrader
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.AnnotationParameterValueList
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import kotlin.system.exitProcess

/**
 * When you call [scanEverything], you get back a mapping from project names
 * to this data structure, which internally includes things like a list of
 * classes to test for coverage (might be empty), as well as a list of [GGradeTopic]
 * which breaks the individual grade tests down.
 */
data class GGradeProject(
    val name: String,
    val description: String,
    val maxPoints: Double,
    val warningPoints: Double,
    val useCheckStyle: Boolean,
    val useGoogleJavaFormat: Boolean,
    val useJavacWarnings: Boolean,
    val coveragePoints: Double,
    val coverageStyle: GCoverageStyle,
    val coveragePercentage: Double,
    val coverageAnnotations: List<GGradeCoverage>,
    val topics: List<GGradeTopic>
)

enum class GCoverageStyle { LINES, INSTRUCTIONS }

data class GGradeTopic(
    val name: String,
    val maxPoints: Double,
    val tests: List<GGradeTest>
)

data class GGradeTest(
    val points: Double,
    val maxPoints: Double,
    val className: String,
    val methodName: String,
    val testFactory: Boolean = false
)

data class GGradeCoverage(
    val scope: GCoverageScope,
    val excluded: Boolean,
    val name: String
)

enum class GCoverageScope { PACKAGE, CLASS }

// Below are internal classes we use while parsing, we'll transform these to the G-classes above
// when sending output.

private data class AnnotationTuple(
    val ai: AnnotationInfo,
    val isPackage: Boolean,
    val classOrPackageName: String,
    val methodName: String? = null
)

private typealias ProjectMap = Map<String, IGradeProject>
private data class IGradeProject(
    val name: String,
    val description: String,
    val maxPoints: Double,
    val warningPoints: Double,
    val useCheckStyle: Boolean,
    val useGoogleJavaFormat: Boolean,
    val useJavacWarnings: Boolean,
    val coveragePoints: Double,
    val coverageStyle: String,
    val coveragePercentage: Double
)

private data class IGradeTopic(
    val project: IGradeProject,
    val topic: String,
    val maxPoints: Double
)

private data class IGradeCoverage(
    val project: IGradeProject,
    val exclude: Boolean,
    val scope: GCoverageScope,
    val name: String
)

private data class IGradeTest(
    val project: IGradeProject,
    val topic: String,
    val points: Double,
    val maxPoints: Double,
    val className: String,
    val methodName: String,
    val testFactory: Boolean = false
)

private const val A_PREFIX = "edu.rice.autograder.annotations."
private const val A_GRADE = A_PREFIX + "Grade"
private const val A_GRADES = A_PREFIX + "Grades"
private const val A_GRADETOPIC = A_PREFIX + "GradeTopic"
private const val A_GRADETOPICS = A_PREFIX + "GradeTopics"
private const val A_GRADEPROJECT = A_PREFIX + "GradeProject"
private const val A_GRADEPROJECTS = A_PREFIX + "GradeProjects"
private const val A_GRADECOVERAGE = A_PREFIX + "GradeCoverage"
private const val A_GRADECOVERAGES = A_PREFIX + "GradeCoverages"
private const val A_JUNIT4_TEST = "org.junit.Test"
private const val A_JUNIT5_TEST = "org.junit.jupiter.api.Test"
private const val A_JUNIT5_TESTFACTORY = "org.junit.jupiter.api.TestFactory"

/** Call whenever the scanner discovers an error. Prints the string, crashes the program. */
private fun internalScannerErrorX(s: String): Nothing {
    Log.e(TAG, "Internal scanner failure: $s")
    System.err.println("$AutoGraderName: internal scanner failure:\n  $s\nPlease report this to <dwallach@rice.edu> so we can track down the bug! Thanks.")
    throw RuntimeException(s)
}

/** Call whenever the scanner discovers an error. Prints the string, crashes the program. */
private fun AnnotationParameterValueList?.internalScannerError(s: String): Nothing =
    internalScannerErrorX(s + if (this == null) "\nNull parameter context!!!" else "\nParameter context: $this")

/** Call whenever the scanner discovers an error. Prints the string, crashes the program. */
private fun failScannerX(s: String): Nothing {
    Log.e(TAG, "Terminating: $s")
    System.err.println("$AutoGraderName: $s")
    exitProcess(1)
}

/** Call whenever the scanner discovers an error. Prints the string, crashes the program. */
private fun AnnotationParameterValueList?.failScanner(s: String): Nothing =
        failScannerX(s + if (this == null) "\nNull parameter context!!!" else "\nParameter context: $this")

private val coverageMethodNames = enumValues<GCoverageStyle>().map { it.name }

/**
 * Fetching a value from an [AnnotationParameterValueList] with a default value for
 * its absence is awful enough that it's worth having a helper method. This version
 * returns _null_ if there's no parameter of the requested _key_. However, if the
 * parameter is there but has no value, then _default_ is returned.
 */
private inline fun <reified T> AnnotationParameterValueList.lookup(key: String, default: T): T? {
    // Kotlin FTW: the reified type parameter allows us to have the "is T" query below, which
    // we could never do as easily in Java. Also winning, the type parameter can almost always
    // get inferred from the default parameter, so you hardly ever have to have an explicit
    // type parameter to this method.

    val o = this[key] ?: return null
    val v = o.value
    return when (v) {
        null -> default
        is T -> v
        else -> {
            val desiredClassName = T::class.java.simpleName
            val actualClassName = v::class.java.simpleName
            failScanner("Expected parameter $key to be of type $desiredClassName, actually $actualClassName")
        }
    }
}

/**
 * Similar to [AnnotationParameterValueList.lookup], except if the requested key
 * is absent altogether, then the _default_ value is returned. Nulls are never returned.
 */
private inline fun <reified T> AnnotationParameterValueList.lookupNoNull(key: String, default: T): T =
    lookup(key, default) ?: default

private fun AnnotationParameterValueList.containsNonEmpty(key: String): Boolean {
    val o = this[key]
    return o != null && o.value != null
}

private fun AnnotationTuple.toIGradeProject(): IGradeProject {
    // the name parameter is *required* by the annotation, so this *shouldn't* fail, but we're being paranoid
    val pv = ai.parameterValues

    val name = pv.lookup("name", "")
    val description = pv.lookupNoNull("description", "")
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)
    val useCheckStyle = pv.lookupNoNull("useCheckStyle", true)
    val useGoogleJavaFormat = pv.lookupNoNull("useGoogleJavaFormat", true)
    val useJavacWarnings = pv.lookupNoNull("useJavacWarnings", true)
    val warningPoints = pv.lookupNoNull("warningPoints", 0.0)
    val coveragePoints = pv.lookupNoNull("coveragePoints", 0.0)
    val coverageMethod = pv.lookupNoNull("coverageStyle", "LINES")

    // it's an integer annotation but we'll treat it afterward as a double
    val coveragePercentage = pv.lookupNoNull("coveragePercentage", 70).toDouble()

    return with(pv) {
        when {
            this == null -> internalScannerErrorX("Unexpected null parameter values: $this")

            name == null ->
                failScanner("Malformed GradeProject: no name specified: {$pv}")

            !maxPoints.isFinite() ->
                internalScannerError("maxPoints $maxPoints isn't finite!")

            maxPoints < 0.0 ->
                failScanner("Malformed GradeProject: maxPoints must be zero or positive {$maxPoints}")

            !warningPoints.isFinite() ->
                internalScannerError("warningPoints $warningPoints isn't finite!")

            warningPoints < 0.0 ->
                failScanner("Malformed GradeProject: warningPoints must be zero or positive {$warningPoints}")

            !coveragePoints.isFinite() ->
                internalScannerError("coveragePoints $coveragePoints isn't finite!")

            coveragePoints < 0.0 ->
                failScanner("Malformed GradeProject: coveragePoints must be zero or positive {$coveragePoints}")

            !coveragePercentage.isFinite() ->
                internalScannerError("coveragePercentage $coveragePercentage isn't finite!")

            coveragePercentage < 0.0 || coveragePercentage > 100.0 ->
                failScanner("Malformed GradeProject: coveragePercentage must be between 0 and 100 {$coveragePercentage}")

            coverageMethod !in coverageMethodNames ->
                failScanner("Malformed GradeProject: coverageStyle {$coverageMethod} must be in ${coverageMethodNames.joinToString(", ")}")

            else -> IGradeProject(name, description, maxPoints, warningPoints, useCheckStyle, useGoogleJavaFormat,
                    useJavacWarnings, coveragePoints, coverageMethod, coveragePercentage)
        }
    }
}

private fun AnnotationTuple.toIGradeTopic(pmap: ProjectMap): IGradeTopic {
    val pv = ai.parameterValues
    val projectStr = pv.lookup("project", "")
    val project = pmap[projectStr]
    val topic = pv.lookupNoNull("topic", "")
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)

    return with(pv) {
        when {
            this == null -> internalScannerErrorX("Unexpected null parameter values: $this")

            project == null ->
                failScanner("Malformed GradeTopic: unknown project ($projectStr not in ${pmap.keys})")

            topic == "" ->
                failScanner("Malformed GradeTopic: no topic specified: $this")

            else ->
                IGradeTopic(project, topic, maxPoints)
        }
    }
}

private fun AnnotationTuple.toIGradeCoverage(pmap: ProjectMap): IGradeCoverage {
    val pv = ai.parameterValues
    val projectName = pv.lookup("project", "")
    val project = pmap[projectName]
    val pvExclude = pv.lookupNoNull("exclude", false)

    if (project == null) {
        pv.failScanner("Malformed GradeCoverage: unknown project name ($projectName)")
    } else {
        return IGradeCoverage(project,
                pvExclude,
                if (isPackage) GCoverageScope.PACKAGE else GCoverageScope.CLASS,
                classOrPackageName)
    }
}

private fun AnnotationTuple.toIGradeTest(
    pmap: ProjectMap,
    testAnnotations: Set<String>,
    testFactoryAnnotations: Set<String>
): IGradeTest {
    val pv = ai.parameterValues
    val project = pmap[pv.lookup("project", "")]
    val topic = pv.lookupNoNull("topic", "")
    val points = pv.lookupNoNull("points", 0.0)
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)

    return with(pv) {
        when {
            this == null -> internalScannerErrorX("Unexpected null parameter values: ${this@toIGradeTest}")

            project == null -> failScanner("Malformed GradeTest: unknown project name (${pv["project"]})")

            topic == "" -> failScanner("Malformed GradeTest, no topic: ${this@toIGradeTest}")

            !points.isFinite() -> internalScannerError("points isn't finite!")

            points <= 0.0 -> failScanner("Malformed GradeTest, points must be positive: ${this@toIGradeTest}")

            methodName == null -> internalScannerError("No method name associated with annotation?! (${this@toIGradeTest})")

            testAnnotations.contains(methodName) && testFactoryAnnotations.contains(methodName) ->
                failScanner("Method $methodName has both @Test and @TestFactory! Pick one or the other.")

            !testAnnotations.contains(methodName) && !testFactoryAnnotations.contains(methodName) ->
                failScanner("Method $methodName has neither @Test nor @TestFactory! One is necessary.")

            // Regular @Test, not a @TestFactory
            testAnnotations.contains(methodName) -> IGradeTest(project, topic, points, maxPoints, classOrPackageName, methodName, false)

            !maxPoints.isFinite() -> internalScannerError("maxPoints isn't finite!")

            maxPoints <= 0.0 -> failScanner("Method $methodName has @TestFactory, but needs to have positive maxPoints specified")

            else -> IGradeTest(project, topic, points, maxPoints, classOrPackageName, methodName, true)
        }
    }
}

/**
 * When there are multiple annotations of the same kind (e.g., "Grade"), they appear as a
 * different annotation (e.g., "Grades") which has a single parameter within called "value"
 * that has an array of the actual annotations we really want. This helper method takes
 * a list of annotation-tuples, some of which might have this weird array property, and
 * then expands them to the regular annotations within.
 */
private fun List<AnnotationTuple>.expandValueList(): List<AnnotationTuple> =
        flatMap {
            val (ai, isPackage, classOrPackageName, methodName) = it
            val pv = ai.parameterValues
            if (pv.containsNonEmpty("value")) {
                val emptyArray = Array<Any?>(0) { null }
                val vlist = pv.lookup<Array<*>>("value", emptyArray)
                    ?: pv.failScanner("    Unexpected empty array when `value' found")
                vlist.mapNotNull { v ->
                    Log.i(TAG, "    Found: $v")
                    when (v) {
                        null -> null
                        is AnnotationInfo -> AnnotationTuple(v, isPackage, classOrPackageName, methodName)
                        else -> pv.failScanner("    Unexpected class type found: $${v::class.java.simpleName}")
                    }
                }
            } else {
                listOf(it)
            }
        }

/**
 * After [expandValueList] has been called, there should be no more "value" items. This check
 * is an assertion that will print warnings if they're still there.
 */
private fun List<AnnotationTuple>.checkNoValueGroups() {
    forEach {
        val valueEntry = it.ai.parameterValues.lookupNoNull<Any?>("value", "")
        if (valueEntry != "") {
            System.err.println("=== Warning: found `value' in result tuple <$it>")
        }
    }
}

/**
 * Given a list of desired annotation names (without the @-symbols), returns a list of [AnnotationTuple]
 * describing every matching annotation found on a Java package (i.e., inside a package-info.java file).
 */
private fun ScanResult.packageAnnotations(annotationNames: List<String>): List<AnnotationTuple> {
    Log.i(TAG, "Looking for packages with annotations: $annotationNames")
    return packageInfo
            .filterNotNull()
            .flatMap { it.annotationInfo.map { ai -> AnnotationTuple(ai, true, it.name) } }
            .filter {
                it.ai.name in annotationNames
            }
            .expandValueList()
            .also {
                Log.i(TAG, "Total: ${it.size} package annotations found")
                it.checkNoValueGroups()
            }
}

/**
 * Given a list of desired annotation names (without the @-symbols), returns a list of [AnnotationTuple]
 * describing every matching annotation found on a Java method.
 */
private fun ScanResult.methodAnnotations(annotationNames: List<String>): List<AnnotationTuple> {
    Log.i(TAG, "================= Looking for methods with annotations: $annotationNames =================")
    return annotationNames.flatMap { aname ->
        Log.i(TAG, "Looking for: $aname")
        getClassesWithMethodAnnotation(aname)
                .filterNotNull()
                .flatMap { classInfo ->
                    val className = classInfo.name
                            ?: internalScannerErrorX("Class with no name?! ($classInfo")

                    (classInfo.declaredMethodAndConstructorInfo
                            ?: internalScannerErrorX("Class with no methods?! ($classInfo"))
                            .filterNotNull()
                            .flatMap { mi ->
                                val mname = mi.name ?: internalScannerErrorX("Method with no name?! ($mi)")
                                mi.annotationInfo.mapNotNull { AnnotationTuple(it, false, className, mname) }
                            }
                            .filter {
                                it.ai.name == aname
                            }
                            .also { mi ->
                                Log.i(TAG, "Pre-expansion annotations: ")
                                mi.forEach { Log.i(TAG, "===> $it") }
                            }
                            .expandValueList()
                            .also { mi ->
                                Log.i(TAG, "Post-expansion annotations: ")
                                mi.forEach { Log.i(TAG, "===> $it") }
                            }
                }
    }
    .also {
        Log.i(TAG, "Total: ${it.size} method annotations found")
        it.checkNoValueGroups()
    }
}

/**
 * Given a list of desired annotation names (without the @-symbols), returns a list of [AnnotationTuple]
 * describing every matching annotation found on a Java class (inner or outer) or interface.
 */
private fun ScanResult.classAnnotations(annotationNames: List<String>): List<AnnotationTuple> {
    Log.i(TAG, "Looking for classes with annotations: $annotationNames")
    return annotationNames.flatMap { aname ->
        getClassesWithAnnotation(aname)
                .filterNotNull()
                .flatMap { cinfo ->
                    cinfo.annotationInfo.filterNotNull().map { ai ->
                        if (cinfo.name != null)
                            AnnotationTuple(ai, false, cinfo.name)
                        else
                            internalScannerErrorX("class without a name?! ($cinfo)")
                    }
                }
                .filter {
                    it.ai.name == aname
                }
                .expandValueList()
    }
    .also {
        Log.i(TAG, "Total: ${it.size} class annotations found")
        it.checkNoValueGroups()
    }
}

/**
 * Given a list of desired annotation names (without the @-symbols), returns a list of [AnnotationTuple]
 * describing every matching annotation found on a Java package or class.
 */
private fun ScanResult.packageOrClassAnnotations(annotationNames: List<String>): List<AnnotationTuple> =
        packageAnnotations(annotationNames) + classAnnotations(annotationNames)

/**
 * We have lists of things that we don't want to have repeats. No repeated project names.
 * No repeated topics within a project. Etc. This method crashes the scanner if it finds repeats.
 */
private fun <T> List<T>.failRepeating(failMessage: String, stringExtractor: (T) -> String): List<T> {
    val repeatGroups =
            groupBy { stringExtractor(it) }
            .filter { it.value.size > 1 }

    return if (repeatGroups.isNotEmpty()) {
        failScannerX("$failMessage: ${repeatGroups.keys.joinToString(",")}")
    } else {
        this
    }
}

private fun List<IGradeCoverage>.toGCoverages(): List<GGradeCoverage> =
        map { GGradeCoverage(it.scope, it.exclude, it.name) }

private const val TAG = "CodeAnnotationScanner"

/**
 * Given the name of a code package like "edu.rice", returns a mapping from project names to
 * [GGradeProject] containing everything we know about that project (i.e., its topics,
 * coverage requirements, and specific unit tests).
 */
fun scanEverything(codePackage: String = "edu.rice"): Map<String, GGradeProject> {
    Log.i(TAG, "scanEverything: $codePackage")

    return ClassGraph()
//            .verbose() // Log to stderr
            .enableAllInfo() // Scan classes, methods, fields, annotations
            .whitelistPackages(codePackage) // Scan codePackage and subpackages
            .scan().use { scanResult: ScanResult? ->
                if (scanResult == null) {
                    emptyMap()
                } else {
                    val gradeProjectAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADEPROJECT, A_GRADEPROJECTS))
                                    .map { it.toIGradeProject() }
                                    .failRepeating("More than one project definition for") {
                                        it.name
                                    }

//                    Log.i(TAG, "Found ${gradeProjectAnnotations.size} GradeProject annotations:")
//                    gradeProjectAnnotations.forEach { Log.i(TAG, it) }

                    val projectMap = gradeProjectAnnotations.associateBy { it.name }

                    val gradeCoverageAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADECOVERAGE, A_GRADECOVERAGES))
                                    .map { it.toIGradeCoverage(projectMap) }

                    val testAnnotations = scanResult.methodAnnotations(listOf(A_JUNIT4_TEST, A_JUNIT5_TEST))
                            .mapNotNull { it.methodName }.toSet()

                    val testFactoryAnnotations = scanResult.methodAnnotations(listOf(A_JUNIT5_TESTFACTORY))
                            .mapNotNull { it.methodName }.toSet()

                    val gradeTestAnnotations =
                            scanResult.methodAnnotations(listOf(A_GRADE, A_GRADES))
                                    .map { it.toIGradeTest(projectMap, testAnnotations, testFactoryAnnotations) }
                                    // sort only to make it easier to read when printed for debugging
                                    .sortedWith(compareBy({ it.project.name }, { it.topic }, { it.className }, { it.methodName }))

                    val gradeTopicAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADETOPIC, A_GRADETOPICS))
                                    .map { it.toIGradeTopic(projectMap) }
                                    // sort only to make it easier to read when printed for debugging
                                    .sortedWith(compareBy({ it.project.name }, { it.topic }))

                    gradeProjectAnnotations.associateBy({ it.name }) { project ->
                        val topics = gradeTopicAnnotations
                                .filter { it.project === project }
                                .failRepeating("More than one topic definition in project ${project.name} for") { it.topic }

                        val coverages = gradeCoverageAnnotations.filter { it.project === project }

                        val gtopics = topics.map { topic ->
                            val gtests = gradeTestAnnotations
                                    .filter { it.topic == topic.topic && it.project == topic.project }
                                    .failRepeating("More than one GradeTest definition on the same method for project ${project.name} ") {
                                        it.className + "." + it.methodName
                                    }
                            val maxPointsFromTests = gtests
                                    .sumByDouble { if (it.testFactory) it.maxPoints else it.points }

//                            Log.i(TAG, "Project ${project.name}, Topic ${topic.topic}: internal maxPoints ${topic.maxPoints}, external maxPoints ${maxPointsFromTests}")
                            val actualMaxPoints = if (topic.maxPoints == 0.0) maxPointsFromTests else topic.maxPoints

                            if (actualMaxPoints == 0.0) {
                                failScannerX("Project ${project.name}, Topic ${topic.topic}: no maxPoints specified and none on the tests either")
                            }

                            GGradeTopic(topic.topic, actualMaxPoints, gtests.map {
                                GGradeTest(it.points, it.maxPoints, it.className, it.methodName, it.testFactory)
                            })
                        }

                        val maxPointsFromTopics = gtopics.sumByDouble { it.maxPoints }
                        val coverageAndWarningPoints = project.warningPoints + project.coveragePoints

                        val actualMaxPoints = when {
                            project.maxPoints == 0.0 -> maxPointsFromTopics + coverageAndWarningPoints
                            else -> project.maxPoints
                        }

                        val coverageMethod = enumValueOf<GCoverageStyle>(project.coverageStyle)
                        val gcoverage = coverages.toGCoverages()

                        if (project.coveragePoints != 0.0 && gcoverage.isEmpty()) {
                            failScannerX("Coverage points specified (${project.coveragePoints}) but no @GradeCoverage annotations found")
                        }

                        GGradeProject(project.name, project.description, actualMaxPoints,
                                project.warningPoints, project.useCheckStyle, project.useGoogleJavaFormat,
                                project.useJavacWarnings, project.coveragePoints, coverageMethod,
                                project.coveragePercentage, coverages.toGCoverages(), gtopics)
                    }
                }
            }
}

// TODO: print YAML file, make sure we can read it back in again
// TODO: switch over to kotlinx.serialization, because it's portable across platforms, has the stuff that plants need
//       https://github.com/Kotlin/kotlinx.serialization

// Engineering note: You'll see lots of filterNotNull() in here. Even though we're pretty sure no nulls are
// coming back from the ClassGraph library, we're doing this anyway because it ensures the resulting lists
// are guaranteed to be null-free. ClassGraph has no annotations for nullity, and this defensive coding practice
// is unlikely to change the outcome of our code, but it does ensure that, if ClassGraph does randomly include
// a null in one of the lists it hands us, we'll just quietly ignore it and move in rather than exploding
// with a NullPointerException. Alternate viewpoint: we're using filterNonNull() as a way of converting
// from types like List<T?> or List<T!> to List<T>.
