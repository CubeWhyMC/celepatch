package org.cubewhy.patch

import kotlinx.serialization.json.Json
import org.cubewhy.patch.api.PatchEntry
import org.cubewhy.patch.config.PatchConfig
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess


val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }

internal fun main(args: Array<String>) {
    if (args.size != 3) {
        println("celepatch.jar <in> <out> <script>")
        exitProcess(1)
    }
    val inFile = File(args[0])
    val outFile = File(args[1])
    val scriptFile = File(args[2])
    println("Input: $inFile")
    if (!(inFile.exists() && inFile.isFile)) {
        error("Input file $inFile does not exist")
    }
    val inJar = JarFile(inFile)
    val scriptJar = JarFile(scriptFile)
    val scriptUri = URL("file:${scriptFile.absolutePath}")

    val urlClassLoader = URLClassLoader(arrayOf(scriptUri), ClassLoader.getSystemClassLoader())
    Thread.currentThread().contextClassLoader = urlClassLoader

    val fileMap = mutableMapOf<String, ByteArray>()

    with(scriptJar.getInputStream(scriptJar.getEntry("patch.json"))) {
        val config: PatchConfig = JSON.decodeFromString(this.readAllBytes().decodeToString())
        val entrypointName = config.entrypoint
        val entrypoint = Class.forName(entrypointName).getDeclaredConstructor().newInstance() as PatchEntry

        inJar.entries().iterator().forEachRemaining { entry ->
            val out = if (!entry.isDirectory && entry.name.endsWith(".class")) {
                entrypoint.patchClass(entry.name, inJar.getInputStream(entry).readAllBytes())
            } else {
                entrypoint.patchFile(entry.name, inJar.getInputStream(entry).readAllBytes())
            }
            fileMap[entry.name] = out
        }
    }
    outFile.createNewFile()
    val zipOut = ZipOutputStream(outFile.outputStream())
    for ((filePath, content) in fileMap.entries) {
        val zipEntry = ZipEntry(filePath)
        zipOut.putNextEntry(zipEntry)
        zipOut.write(content)
        zipOut.closeEntry()
    }
    zipOut.close()
}