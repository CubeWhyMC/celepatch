package org.cubewhy.patch

import kotlinx.serialization.json.Json
import org.cubewhy.patch.api.PatchEntry
import org.cubewhy.patch.config.PatchConfig
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess


val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }

class Main

// WARNING: DO NOT modify this file if everything works.

internal fun main(args: Array<String>) {
    if (args.size != 2) {
        println("celepatch.jar <in> <out>")
        exitProcess(1)
    }
    val inFile = File(args[0])
    val outFile = File(args[1])
    println("Input: $inFile")
    if (!(inFile.exists() && inFile.isFile)) {
        error("Input file $inFile does not exist")
    }
    val inJar = JarFile(inFile)

    val fileMap = mutableMapOf<String, ByteArray>()

    val config: PatchConfig = JSON.decodeFromString(Main::class.java.getResourceAsStream("/patch.json")!!.readAllBytes().decodeToString())

    val entrypoint = Main::class.java.classLoader.loadClass(config.entrypoint).getDeclaredConstructor().newInstance() as PatchEntry
    inJar.entries().iterator().forEachRemaining { entry ->
        val out = if (!entry.isDirectory && entry.name.endsWith(".class")) {
            entrypoint.patchClass(entry.name, inJar.getInputStream(entry).readAllBytes())
        } else {
            entrypoint.patchFile(entry.name, inJar.getInputStream(entry).readAllBytes())
        }
        // if out equals null, drop the file.
        if (out != null) {
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