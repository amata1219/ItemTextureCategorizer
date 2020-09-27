package amata1219.item.texture.categorizer

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.lang.ArithmeticException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.streams.toList

fun main(args: Array<String>) {
    val chunks: MutableMap<Int, MutableList<Pair<Int, BufferedImage>>> = mutableMapOf()
    for (r in 0 until 4)
        for (g in 0 until 4)
            for(b in 0 until 4)
                chunks[r * 100 + g * 10 + b] = mutableListOf()

    val textures: MutableList<BufferedImage> = mutableListOf()
    val directory = "src/resources"
    for (i in 0..18) {
        val path: Path = Paths.get(directory, "$i.png")
        try {
            Files.newInputStream(path).use {
                val image: BufferedImage = ImageIO.read(it)
                for (line in 0..5) {
                    val y = 54 + line * 36
                    for (slot in 0..8) {
                        val x = 266 + slot * 36
                        val sub: BufferedImage = image.getSubimage(x, y, 32, 32)
                        textures.add(sub)
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    val materials: List<String> = try {
        Files.newInputStream(Paths.get("src/resources/materials.txt")).use {
            it.bufferedReader().lines().toList()
        }
    } catch (ex: IOException) {
        ex.printStackTrace()
        emptyList()
    }

    val backgroundColor = Color(139, 139, 139)
    for (i in 0 until textures.size - 51) {
        val texture = textures[i]
        var (r, g, b) = Triple(0, 0, 0)
        var skipCount = 0
        for (x in 0 until 32) {
            for (y in 0 until 32) {
                val color = Color(texture.getRGB(x, y))
                if (color == backgroundColor) {
                    skipCount++
                    continue
                }
                r += color.red
                g += color.green
                b += color.blue
            }
        }
        val all = 1024 - skipCount
        try {
            r /= all
            g /= all
            b /= all
        } catch (ex: ArithmeticException) {
            println(materials[i])
        }
        val hash = (r shr 6) * 100 + (g shr 6) * 10 + (b shr 6)
        chunks[hash]!!.add(i to texture)
    }

    chunks.keys.sorted().forEach {
        val chunk: List<Pair<Int, BufferedImage>> = chunks[it]!!
        val dirName: String = String.format("%03d", it).toList()
            .map { c -> "$c".toInt() * 64 }
            .joinToString(", ") { c -> c.toString() }
        val dir: Path = Paths.get("C:\\Users\\Admin\\Desktop\\Memo\\item material categories\\$dirName")
        Files.createDirectory(dir)
        val containingMaterials: MutableList<String> = mutableListOf()
        chunk.forEach { (order, image) ->
            val material: String = materials[order]
            containingMaterials.add(material)
            val path = Paths.get(dir.toString(), "$material.png")
            Files.createFile(path)
            ImageIO.write(image, "png", path.toFile())
        }
        val respath = Paths.get(dir.toString(), "result.txt")
        containingMaterials.add("sum: ${containingMaterials.size}")
        Files.createFile(respath)
        val writer = FileWriter(respath.toFile())
        val pwriter = PrintWriter(BufferedWriter(writer))
        containingMaterials.forEach(pwriter::println)
        pwriter.close()
    }
}
