/*
 * RiceChecks
 * Copyright (c) 2019, Dan S. Wallach, Rice University
 * Available subject to the Apache 2.0 License
 */

package edu.rice.autograder

import arrow.core.Try
import arrow.core.getOrElse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.NullPointerException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.zip.ZipEntry

private const val TAG = "IO"

//
// The code here provides a better interface for dealing with files and
// java.nio. Among other benefits, we're not worried about exceptions getting
// thrown when we're just trying to read a file or whatever. Instead, we
// take advantage of Arrow's Try class.
//

/**
 * Returns a [Try]-wrapped list of [Path] objects corresponding to files in the requested directory.
 */
fun readdirPath(filePath: String) = Try {
    Files.newDirectoryStream(Paths.get(filePath)).use {
        // We need to iterate the full list before closing the directory stream
        // so thus toList(). Also, note that "use" here is the Kotlin equivalent
        // of Java's try-with-resources.
        it?.toList() ?: Log.ethrow(TAG, "failed to get anything from $filePath")
    }
}

/** Given a [Path], returns a [Try]-wrapped String of the file's contents. */
fun Path.readFile() = Try {
    String(Files.readAllBytes(this))
}.onFailure {
    Log.e(TAG, "failed to read file(${this.fileName})", it)
}

/** Given a file name, returns a [Try]-wrapped String of the file's contents. */
fun readFile(fileName: String) = Paths.get(fileName).readFile()

/**
 * Deals with reading files that look like "foo/ * /bar", where we don't know
 * the proper value of the wildcard in the middle. For GoogleJavaFormat, it's
 * sometimes "0.8" and other times a weird two-line monstrosity caused by
 * Docker. Yuck, but this lets us work around that problem. If there is exactly
 * one matching directory, this works just like [readFile], above. Otherwise,
 * it fails with some sort of [RuntimeException] embedded in a [Try.Failure].
 */
fun readFileWildcardDir(pathPrefix: String, fileName: String): Try<String> =
    readdirPath(pathPrefix).flatMap {
        when (it.size) {
            1 -> it[0].resolve(fileName).readFile()
            0 -> Try.raiseError(
                RuntimeException("nothing inside $pathPrefix")
            )
            else -> Try.raiseError(
                RuntimeException("more than one matching directory inside $pathPrefix")
            )
        }
    }

/**
 * Given a [Path] and a string to write there, tries to do so, returning an empty [Try.Success]
 * if it works or a [Try.Failure] if something goes wrong.
 */
fun Path.writeFile(data: String) = Try {
    Files.write(this, data.toByteArray())
}

/**
 * Similar to [Path.writeFile] but if the requested path doesn't exist yet, this
 * variant will also create any necessary directories.
 */
fun Path.mkdirAndWriteFile(data: String) = Try {
    Files.createDirectories(parent)
}.map {
    writeFile(data)
}

/**
 * Given a file name and string data, will attempt to write the data to the file,
 * and create any directories, if necessary. As with [Path.writeFile], the result
 * is wrapped in a [Try], for error-handling.
 */
fun writeFile(fileName: String, data: String) = Paths.get(fileName).mkdirAndWriteFile(data)

/**
 * Given a directory path into the resources, returns a list of resource names suitable for then
 * passing to [readResource], [resourceToStream], etc.
 *
 * @return a [Try.Success] of the list of resource names, or a [Try.Failure] indicating what went
 * wrong
 */
fun readResourceDir(dirPath: String): Try<List<String>> =
    Try { ClassLoader.getSystemResources(dirPath).toList() }
        .onFailure { err ->
            Log.e(TAG, "getSystemResources failed for path($dirPath)", err)
        }
        .map { dirUrls: List<URL> ->
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

                        val urlPath = if (rawUrlPath.matches(Regex("^/\\p{Upper}:/.*$"))) {
                            rawUrlPath.substring(1)
                        } else {
                            rawUrlPath
                        }

                        // if the URLDecoder fails, for whatever
                        // reason, we'll just go with the original
                        // undecoded path
                        val decodedPath: Path = Paths.get(
                            Try {
                                URLDecoder.decode(urlPath, StandardCharsets.UTF_8.toString())
                            }.getOrElse { urlPath }
                        )

                        readdirPath(decodedPath.toString())
                            .getOrElse { emptyList() }
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
                            JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.toString()))
                                .use {
                                    // This code is going to work, but could
                                    // be slow for huge JAR files.

                                    it.entries()
                                        .toList()
                                        .map(ZipEntry::getName)
                                        .filter { it.startsWith(dirPath) }
                                }
                        }.onFailure {
                            Log.e(
                                TAG,
                                "trouble reading $dirUrl, " +
                                    "ignoring and marching onward",
                                it
                            )
                        }.fold({ emptyList<String>() }, { it })
                    }

                    else -> {
                        Log.e(TAG, "unknown protocol in $dirUrl")
                        emptyList()
                    }
                }
            }
        }

private fun resourceToStream(resourceName: String): Try<InputStream> =
    // If ClassLoader.getSystemResourceAsStream finds nothing, it
    // returns null, which we morph here into a Try.failure().
    Try {
        ClassLoader.getSystemResourceAsStream(resourceName)
            ?: throw NullPointerException("null result from ClassLoader?")
    }.onFailure {
        Log.e(TAG, "getSystemResources failed for resource($resourceName)", it)
    }

/**
 * Given a resource name, which typically maps to a file in the "resources" directory, read it in
 * and return a String. This method assumes that the resource file is encoded as a UTF-8 string.
 * If you want to get raw bytes rather than a string, use [readResourceBytes] instead.
 *
 * @return [Try.Success] of the file contents as a String, or [Try.Failure] indicating what went
 * wrong.
 */
fun readResource(resourceName: String): Try<String> =
    readResourceBytes(resourceName).map { String(it) }

/**
 * Get the contents of an [InputStream] as an array of bytes. The stream is closed
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
 * bytes, use [readResource] instead.
 *
 * @return [Try.Success] of the file contents as a byte array, or [Try.Failure] indicating what
 * went wrong.
 */
fun readResourceBytes(resourceName: String): Try<ByteArray> =
    resourceToStream(resourceName).flatMap { it.toByteArray() }
