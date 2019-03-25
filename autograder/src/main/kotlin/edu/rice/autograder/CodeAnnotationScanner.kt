//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import io.github.classgraph.AnnotationInfo
import io.github.classgraph.AnnotationParameterValueList
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult

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
        val coveragePoints: Double,
        val coverageMethod: String,
        val coverageRatio: Double,
        val coverageClasses: List<String>,
        val topics: List<GGradeTopic>)

data class GGradeTopic(
        val name: String,
        val maxPoints: Double,
        val tests: List<GGradeTest>)

data class GGradeTest(
        val points: Double,
        val maxPoints: Double,
        val className: String,
        val methodName: String,
        val testFactory: Boolean = false)

// Below are internal classes we use while parsing, we'll transform these to the G-classes above
// when sending output.

private data class AnnotationTuple(val className: String, val methodName: String?, val ai: AnnotationInfo) {
    constructor(className: String, ai: AnnotationInfo) : this(className, null, ai)
}

private typealias ProjectMap = Map<String, IGradeProject>
private data class IGradeProject(
        val name: String,
        val description: String,
        val maxPoints: Double,
        val warningPoints: Double,
        val coveragePoints: Double,
        val coverageMethod: String,
        val coverageRatio: Double)

private data class IGradeTopic(
        val project: IGradeProject,
        val topic: String,
        val maxPoints: Double)

private data class IGradeCoverage(
        val project: IGradeProject,
        val exclude: Boolean,
        val className: String)

private data class IGradeTest(
        val project: IGradeProject,
        val topic: String,
        val points: Double,
        val maxPoints: Double,
        val className: String,
        val methodName: String,
        val testFactory: Boolean = false)

private const val A_PREFIX = "edu.rice.autograder."
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

/**
 * Call whenever the scanner discovers an error. Prints the string, crashes the program.
 */
private fun failScanner(s: String): Nothing {
    System.err.println("Terminating Grade Annotation Scanner:\n  $s")
    throw RuntimeException(s)

//    exitProcess(1)
}

/**
 * Fetching a value from an [AnnotationParameterValueList] with a default value for
 * its absence is awful enough that it's worth having a helper method.
 */
private inline fun <reified T> AnnotationParameterValueList.lookup(key: String, default: T): T? {
    val o = this[key]
    if (o == null) {
        return default
    }

    val v = o.value
    return if (v == null) default else v as T
}

/**
 * Fetching a value from an [AnnotationParameterValueList] with a default value for
 * its absence is awful enough that it's worth having a helper method.
 */
private inline fun <reified T> AnnotationParameterValueList.lookupNoNull(key: String, default: T): T =
    lookup(key, default) ?: default

private fun AnnotationTuple.toIGradeProject(): IGradeProject {
    // the name parameter is *required* by the annotation, so this *shouldn't* fail, but we're being paranoid
    val pv = ai.parameterValues

    val name = pv.lookup("name", "")
    val description = pv.lookupNoNull("description", "")
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)
    val warningPoints = pv.lookupNoNull("warningPoints", 0.0)
    val coveragePoints = pv.lookupNoNull("coveragePoints", 0.0)
    val coverageMethod = pv.lookupNoNull("coverageMethod", "LINES")
    val coverageRatio = pv.lookupNoNull("coverageRatio", 0.7)

    return when {
        name == null ->
            failScanner("Malformed GradeProject: no name specified: {$pv}")

        maxPoints < 0.0 || !maxPoints.isFinite() ->
            failScanner("Malformed GradeProject: maxPoints must be zero or positive {${maxPoints}}")

        warningPoints < 0.0 || !warningPoints.isFinite() ->
            failScanner("Malformed GradeProject: warningPoints must be zero or positive {${warningPoints}}")

        coveragePoints < 0.0 || !coveragePoints.isFinite() ->
            failScanner("Malformed GradeProject: coveragePoints must be zero or positive {${coveragePoints}}")

        coverageRatio < 0.0 || coverageRatio > 1.0 || !coverageRatio.isFinite() ->
            failScanner("Malformed GradeProject: coverageRatio must be between 0 and 1 {${coverageRatio}}")

        else -> IGradeProject(name, description, maxPoints, warningPoints, coveragePoints, coverageMethod, coverageRatio)
    }
}

private fun AnnotationTuple.toIGradeTopic(pmap: ProjectMap): IGradeTopic {
    val pv = ai.parameterValues
    val projectStr = pv.lookup("project", "")
    val project = pmap[projectStr]
    val topic = pv.lookupNoNull("topic", "")
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)

    return when {
        project == null ->
            failScanner("Malformed GradeTopic: ${pv}: unknown project (${projectStr} not in ${pmap.keys})")
        topic == "" ->
            failScanner("Malformed GradeTopic: no topic specified: ${this}")
        else ->
            IGradeTopic(project, topic, maxPoints)
    }
}

private fun AnnotationTuple.toIGradeCoverage(pmap: ProjectMap): IGradeCoverage {
    val pv = ai.parameterValues
    val project = pmap[pv["project"].value as String?]

    if (project == null) {
        failScanner("Malformed GradeCoverage: unknown project name (${pv["project"]})")
    } else {
        return IGradeCoverage(project, pv["exclude"].value as Boolean? ?: false, className)
    }
}

private fun AnnotationTuple.toIGradeTest(pmap: ProjectMap,
                                 testAnnotations: Set<String>,
                                 testFactoryAnnotations: Set<String>): IGradeTest {
    val pv = ai.parameterValues
    val project = pmap[pv.lookup("project", "")]
    val topic = pv.lookupNoNull("topic", "")
    val points = pv.lookupNoNull("points", 0.0)
    val maxPoints = pv.lookupNoNull("maxPoints", 0.0)

    return when {
        project == null ->
            failScanner("Malformed GradeTest: unknown project name (${pv["project"]})")

        topic == "" ->
            failScanner("Malformed GradeTest, no topic: ${this}")

        points <= 0.0 || !points.isFinite() ->
            failScanner("Malformed GradeTest, points must be positive: ${this}")

        methodName == null ->
            failScanner("Internal error: no method name associated with annotation?! (${this})")

        testAnnotations.contains(methodName) && testFactoryAnnotations.contains(methodName) ->
            failScanner("Method ${methodName} has both @Test and @TestFactory! Pick one or the other.")

        !testAnnotations.contains(methodName) && !testFactoryAnnotations.contains(methodName) ->
            failScanner("Method ${methodName} has neither @Test nor @TestFactory! One is necessary.")

        testAnnotations.contains(methodName) ->
            IGradeTest(project, topic, points, maxPoints, className, methodName, false)

        maxPoints <= 0.0 || !maxPoints.isFinite() -> failScanner("Method ${methodName} has @TestFactory, but needs to have positive maxPoints specified")

        else -> IGradeTest(project, topic, points, maxPoints, className, methodName, true)
    }
}

private fun List<AnnotationTuple>.expandValueList(verbose: Boolean = false): List<AnnotationTuple> =
        // When there are multiple annotations of the same kind (e.g., "Grade"), they appear as a
        // different annotation (e.g., "Grades") which has a single parameter within called "value"
        // that has an array of the actual annotations we really want. This helper method takes
        // a list of annotation-tuples, some of which might have this weird array property, and
        // then expands them to the regular annotations within.

        flatMap {
            if (verbose) System.err.println("  Found: ${it}")
            val (className, methodName, ai) = it
            val vlist = ai.parameterValues.get("value")
            if (vlist != null && vlist is Array<*>) {
                vlist.mapNotNull { v ->
                    if (verbose) System.err.println("    Found: ${v}")
                    if (v == null) null else
                        AnnotationTuple(className, methodName, v as AnnotationInfo)
                }
            } else {
                listOf(it)
            }
        }


private fun ScanResult.packageAnnotations(annotationNames: List<String>, verbose: Boolean = false): List<AnnotationTuple> {
    if (verbose) System.err.println("Looking for packages with annotations: ${annotationNames}")
    return packageInfo
            .filterNotNull()
            .flatMap { it.annotationInfo.map { ai -> AnnotationTuple(it.name, ai) } }
            .filter {
                it.ai.name in annotationNames
            }
            .expandValueList(verbose)
            .also {
                if (verbose) System.err.println("Total: ${it.size} package annotations found")
                it.checkNoValueGroups()
            }
}

private fun ScanResult.methodAnnotations(annotationNames: List<String>, verbose: Boolean = false): List<AnnotationTuple> {
    if (verbose) System.err.println("================= Looking for methods with annotations: ${annotationNames} =================")
    return annotationNames.flatMap { aname ->
        if (verbose) System.err.println("Looking for: ${aname}")
        getClassesWithMethodAnnotation(aname)
                .filterNotNull()
                .flatMap { classInfo ->
                    val className = classInfo.name
                            ?: failScanner("Class with no name?! (${classInfo}")

                    (classInfo.declaredMethodAndConstructorInfo
                            ?: failScanner("Class with no methods?! (${classInfo}"))
                            .filterNotNull()
                            .flatMap { mi ->
                                val mname = mi.name ?: failScanner("Method with no name?! (${mi})")
                                mi.annotationInfo.mapNotNull { AnnotationTuple(className, mname, it) }
                            }
                            .filter {
                                it.ai.name == aname
                            }
                            .expandValueList(verbose)
                }
    }
    .also {
        if (verbose) System.err.println("Total: ${it.size} method annotations found")
        it.checkNoValueGroups()
    }
}

private fun ScanResult.classAnnotations(annotationNames: List<String>, verbose: Boolean = false): List<AnnotationTuple> {
    if (verbose) System.err.println("Looking for classes with annotations: ${annotationNames}")
    return annotationNames.flatMap { aname ->
        getClassesWithAnnotation(aname)
                .filterNotNull()
                .flatMap { cinfo ->
                    cinfo.annotationInfo.filterNotNull().map { ai ->
                        if (cinfo.name != null)
                            AnnotationTuple(cinfo.name, ai)
                        else
                            failScanner("class without a name?! (${cinfo})")
                    }
                }
                .filter {
                    it.ai.name == aname
                }
                .expandValueList(verbose)
    }
    .also {
        if (verbose) System.err.println("Total: ${it.size} class annotations found")
        it.checkNoValueGroups()
    }
}

private fun List<AnnotationTuple>.checkNoValueGroups() {
    forEach {
        if (it.ai.parameterValues["value"] != null) {
            System.err.println("=== Warning: found `value' in result tuple <${it}>")
        }
    }
}

private fun ScanResult.packageOrClassAnnotations(annotationNames: List<String>): List<AnnotationTuple> =
        packageAnnotations(annotationNames) + classAnnotations(annotationNames)

/**
 * We have lists of things that we don't want to have repeats. No repeated project names.
 * No repeated topics within a project. Etc. This method does all the work.
 */
private fun <T> List<T>.failRepeating(failMessage: String, stringExtractor: (T) -> String): List<T> {
    val repeatGroups =
            groupBy { stringExtractor(it) }
            .filter { it.value.size > 1 }

    return if (repeatGroups.isNotEmpty()) {
        failScanner("${failMessage}: ${repeatGroups.keys.joinToString(",")}")
    } else {
        this
    }
}

private fun List<IGradeCoverage>.toClassNames(scanResult: ScanResult): List<String> {
    val positives = filter { !it.exclude }.map { it.className }
    val negatives = filter { it.exclude }.map { it.className }

    return scanResult.allClasses
            .filter { !it.isAnnotation } // we only want classes and interfaces
            .mapNotNull { it.name } // we don't expect any nulls in here, but we're being paranoid
            .filter { c -> positives.filter { c.startsWith(it) }.isNotEmpty() } // it's something we want
            .filter { c -> negatives.filter { c.startsWith(it) }.isEmpty() } // but not something we exclude
}

/**
 * Given the name of a code package like "edu.rice", returns a mapping from project names to
 * [GGradeProject] containing everything we know about that project (i.e., its topics,
 * coverage requirements, and specific unit tests).
 */
fun scanEverything(codePackage: String = "edu.rice"): Map<String, GGradeProject> =
    ClassGraph()
//            .verbose()                   // Log to stderr
            .enableAllInfo()             // Scan classes, methods, fields, annotations
            .whitelistPackages(codePackage)      // Scan codePackage and subpackages
            .scan().use { scanResult: ScanResult? ->
                if (scanResult == null) {
                    emptyMap()
                } else {
                    val gradeProjectAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADEPROJECT, A_GRADEPROJECTS))
                                    .map { it.toIGradeProject() }
                                    .failRepeating("More than one project definition for") { it.name }

//                    System.out.println("Found ${gradeProjectAnnotations.size} GradeProject annotations:")
//                    gradeProjectAnnotations.forEach { System.out.println(it) }

                    val projectMap = gradeProjectAnnotations.associateBy { it.name }

                    val gradeCoverageAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADECOVERAGE, A_GRADECOVERAGES))
                                    .map { it.toIGradeCoverage(projectMap) }

                    val testAnnotations = scanResult.methodAnnotations(listOf(A_JUNIT4_TEST, A_JUNIT5_TEST))
                            .map { it.methodName }.filterNotNull().toSet()

                    val testFactoryAnnotations = scanResult.methodAnnotations(listOf(A_JUNIT5_TESTFACTORY))
                            .map { it.methodName }.filterNotNull().toSet()

                    val gradeTestAnnotations =
                            scanResult.methodAnnotations(listOf(A_GRADE, A_GRADES))
                                    .map { it.toIGradeTest(projectMap, testAnnotations, testFactoryAnnotations) }
                                    // sort only to make it easier to read when printed for debugging
                                    .sortedWith(compareBy({ it.project.name }, { it.topic }, { it.className }, { it.methodName }))

                    val gradeTopicAnnotations =
                            scanResult.packageOrClassAnnotations(listOf(A_GRADETOPIC, A_GRADETOPICS))
                                    .map { it.toIGradeTopic(projectMap) }
                                    // sort only to make it easier to read when printed for debugging
                                    .sortedWith(compareBy({ it.project.name }, {it.topic }))

                    gradeProjectAnnotations.associateBy({ it.name }) { project ->
                        val topics = gradeTopicAnnotations
                                .filter { it.project === project }
                                .failRepeating("More than one topic definition in project ${project.name} for") { it.topic }

                        val coverages = gradeCoverageAnnotations .filter { it.project === project }

                        val gtopics = topics.map { topic ->
                            val gtests = gradeTestAnnotations
                                    .filter { it.topic == topic.topic && it.project == topic.project }
                                    .failRepeating("More than one GradeTest definition on the same method for project ${project.name} ")
                                    { it.className + "." + it.methodName }
                            val maxPointsFromTests = gtests
                                    .map { if (it.testFactory) it.maxPoints else it.points }
                                    .fold(0.0) { a, b -> a + b }

//                            System.out.println("Project ${project.name}, Topic ${topic.topic}: internal maxPoints ${topic.maxPoints}, external maxPoints ${maxPointsFromTests}")
                            val actualMaxPoints = if (topic.maxPoints == 0.0) maxPointsFromTests else topic.maxPoints

                            if (actualMaxPoints == 0.0) {
                                failScanner("Project ${project.name}, Topic ${topic.topic}: no maxPoints specified and none on the tests either")
                            }

                            GGradeTopic(topic.topic, actualMaxPoints, gtests.map {
                                GGradeTest(it.points, it.maxPoints, it.className, it.methodName, it.testFactory)
                            })
                        }

                        val maxPointsFromTopics = gtopics.map { it.maxPoints }.fold(0.0) { a, b -> a + b }
                        val actualMaxPoints = if (project.maxPoints == 0.0) maxPointsFromTopics else project.maxPoints

                        GGradeProject(project.name, project.description, actualMaxPoints, project.warningPoints,
                                project.coveragePoints, project.coverageMethod, project.coverageRatio,
                                coverages.toClassNames(scanResult),
                                gtopics)
                    }
                }
            }

// DONE: Scream if a topic or a project is defined more than once
// DONE: add up the number of points, use as maxPoints for topics with none (in progress)
// DONE: GradeTest's topic, convert from string to GradeTopic ptr
// HALF-DONE: print YAML file
// TODO: switch over to kotlinx.serializetion, because it's portable across platforms, has the stuff that plants need
//       https://github.com/Kotlin/kotlinx.serialization

// Engineering note: You'll see lots of filterNotNull() in here. Even though we're pretty sure no nulls are
// coming back from the ClassGraph library, we're doing this anyway because it ensures the resulting lists
// are guaranteed to be null-free. ClassGraph has no annotations for nullity, and we would rather not have
// NullPointerExceptions in our Kotlin code.
