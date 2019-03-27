package edu.rice.autograder

import io.vavr.collection.LinearSeq
import io.vavr.collection.List
import io.vavr.collection.Stream
import io.vavr.control.Try
import io.vavr.kotlin.*
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry

private const val TAG = "IO"

fun <T> Enumeration<T>.toVavrStream(): Stream<T> =
    Stream.iterate { if (hasMoreElements()) some<T>(nextElement()) else none() }
            .filter { it != null }

private fun readdirPath(filePath: String): Try<LinearSeq<Path>> =
    try {
        java.nio.file.Files.newDirectoryStream(Paths.get(filePath)).use {
            if (it == null) {
                success(Stream.empty())
            } else {
                // some paranoia here: nulls shouldn't happen
                success(List.ofAll<Path>(it).filter { it != null })
            }
        }
    } catch (ioe: IOException) {
        Log.e(TAG, "failed to read directory($filePath)", ioe)
        failure(ioe)
    }

/**
 * Given a directory path into the resources, returns a list of resource names suitable for then
 * passing to [.readResource], [.resourceToStream], etc.
 *
 * @return a Try.success of the list of resource names, or a Try.failure indicating what went
 * wrong
 */
fun readResourceDir(dirPath: String): Try<LinearSeq<String>> {
    return Try { ClassLoader.getSystemResources(dirPath).toVavrStream() }
            .onFailure { err -> Log.e(TAG, "getSystemResources failed for path($dirPath)", err) }
            .map { dirUrls: Stream<URL> ->
                dirUrls.flatMap { dirUrl: URL ->
                    val rawUrlPath = dirUrl.path ?: Log.ethrow(TAG, "found null URL path?")

                    //          Log.i(TAG, () -> "rawUrlPath " + rawUrlPath);

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
                            val decodedPath = Paths.get(
                                    Try {
                                        URLDecoder.decode(urlPath, StandardCharsets.UTF_8)
                                                ?: urlPath
                                    }
                                            .getOrElse(urlPath))
                                    ?: Log.ethrow(TAG, "got null Path?")

                            val result = readdirPath(decodedPath.toString())
                                    .getOrElse(Stream.empty<Path>())
                                    .map(decodedPath::relativize)
                                    .map(Path::toString)
                                    .map { "$dirPath/$it" }

                            result
                        }

                        "jar" -> {
                            // Solution adapted from here:
                            // http://www.uofr.net/~greg/java/get-resource-listing.html

                            val jarPath = rawUrlPath.substring(
                                    5, rawUrlPath.indexOf("!")) // strip out only the JAR file

                            try {
                                JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8)).use {

                                    // This code is going to work, but could
                                    // be slow for huge JAR files.

                                    it.entries()
                                            .toVavrStream()
                                            .map(ZipEntry::getName)
                                            .filter { it.startsWith(dirPath) }
                                }
                            } catch (exception: IOException) {
                                Log.e(TAG,
                                        "trouble reading $dirUrl, ignoring and marching onward",
                                        exception)
                                Stream.empty<String>()
                            }

                            Log.e(TAG, "unknown protocol in $dirUrl")
                            Stream.empty<String>()
                        }

                        else -> {
                            Log.e(TAG, "unknown protocol in $dirUrl")
                            Stream.empty<String>()
                        }
                    }
                }
            }
}
