//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//
package edu.rice.autograder

import arrow.core.Try
import arrow.core.getOrElse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.NullPointerException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.zip.ZipEntry

private const val TAG = "IO"

private fun readdirPath(filePath: String): Try<Sequence<Path>> =
    Try {
        java.nio.file.Files.newDirectoryStream(Paths.get(filePath)).use {
            // we need to iterate the full list before closing the directory stream
            // so thus toList() then back to a sequence.
            it?.toList()?.asSequence() ?: Log.ethrow(TAG, "failed to get anything from $filePath")
        }
    }

/**
 * Given a directory path into the resources, returns a list of resource names suitable for then
 * passing to [.readResource], [.resourceToStream], etc.
 *
 * @return a Try.success of the list of resource names, or a Try.failure indicating what went
 * wrong
 */
fun readResourceDir(dirPath: String): Try<Sequence<String>> =
        Try { ClassLoader.getSystemResources(dirPath).asSequence() }
            .onFailure { err -> Log.e(TAG, "getSystemResources failed for path($dirPath)", err); }
        .map { dirUrls: Sequence<URL> ->
            dirUrls.flatMap { dirUrl: URL ->
                val rawUrlPath = dirUrl.path ?: Log.ethrow(TAG, "found null URL path?")

                when (dirUrl.protocol) {
                    "file" -> {

                        // On Windows, we get URL paths like file:/C:/Users/dwallach/....
                        // On Macs, we get URL paths like file:/Users/dwallach/...

                        // With those Windows URLs, getPath() will
                        // give us /C:/Users/... which doesn't work
                        // when we try to actually open the
                        // files. The solution? Match a regular
                        // expression and then remove the leading
                        // slash.

                        val urlPath = if (rawUrlPath.matches(Regex("^/\\p{Upper}:/.*$")))
                            rawUrlPath.substring(1)
                        else
                            rawUrlPath

                        // if the URLDecoder fails, for whatever
                        // reason, we'll just go with the original
                        // undecoded path
                        val decodedPath: Path = Paths.get(
                            Try {
                                URLDecoder.decode(urlPath, StandardCharsets.UTF_8)
                            }.getOrElse { urlPath })

                        readdirPath(decodedPath.toString())
                            .getOrElse { emptySequence() }
                            .map(decodedPath::relativize)
                            .map(Path::toString)
                            .map { "$dirPath/$it" }
                    }

                    "jar" -> {
                        // Solution adapted from here:
                        // http://www.uofr.net/~greg/java/get-resource-listing.html

                        val jarPath = rawUrlPath.substring(
                            5, rawUrlPath.indexOf("!")
                        ) // strip out only the JAR file

                        Try {
                            JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8)).use {

                                // This code is going to work, but could
                                // be slow for huge JAR files.

                                it.entries()
                                    .asSequence()
                                    .map(ZipEntry::getName)
                                    .filter { it.startsWith(dirPath) }
                            }
                        }.onFailure {
                            Log.e(TAG, "trouble reading $dirUrl, ignoring and marching onward", it)
                        }.fold({ emptySequence<String>() }, { it })
                    }

                    else -> {
                        Log.e(TAG, "unknown protocol in $dirUrl")
                        emptySequence()
                    }
                }
            }
        }

private fun resourceToStream(resourceName: String): Try<InputStream> {
    // If ClassLoader.getSystemResourceAsStream finds nothing, it
    // returns null, which we have to deal with.
    return Try {
        ClassLoader.getSystemResourceAsStream(resourceName)
            ?: throw NullPointerException("null result from ClassLoader?")
    }.onFailure {
        Log.e(TAG, "getSystemResources failed for resource($resourceName)", it)
    }
}

/**
 * Given a resource name, which typically maps to a file in the "resources" directory, read it in
 * and return a String. This method assumes that the resource file is encoded as a UTF-8 string.
 * If you want to get raw bytes rather than a string, use [.readResourceBytes]
 * instead.
 *
 * @return a Try.success of the file contents as a String, or a Try.failure indicating what went
 * wrong
 */
fun readResource(resourceName: String): Try<String> =
    readResourceBytes(resourceName).map { String(it, StandardCharsets.UTF_8) }

/**
 * Get the contents of an `InputStream` as an array of bytes. The stream is closed
 * after being read.
 */
private fun InputStream.toByteArray(): Try<ByteArray> =
    Try {
        use {
            val os = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var n = it.read(buf)
            while (n != -1) {
                os.write(buf, 0, n)
                n = it.read(buf)
            }
            os.toByteArray()
        }
    }

/**
 * Given a resource name, which typically maps to a file in the "resources" directory, read it in
 * and return an array of bytes. If you want the result as a String rather than an array of raw
 * bytes, use [.readResource] instead.
 *
 * @return a Try.success of the file contents as a byte array, or a Try.failure indicating what
 * went wrong
 */
fun readResourceBytes(resourceName: String): Try<ByteArray> =
    resourceToStream(resourceName).flatMap { it.toByteArray() }
