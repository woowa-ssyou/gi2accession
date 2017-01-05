package de.mpicbg.scicomp.bioinfo

import de.mpicbg.scicomp.kutils.evalBash
import de.mpicbg.scicomp.kutils.saveAs
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import org.springframework.data.rest.core.annotation.RepositoryRestResource as Resource


/**
 * @author Holger Brandl
 */

// value type to model pythonscript output
data class IdPair(val gi: Long, val accession: String, val seqLength: Long)

// installation dir of ncbi provided pyton script and database
val INSTALL_DIR = File(System.getProperty("user.home"), "projects/gi_acc")


@RestController
class IdConversionController {

    @RequestMapping("/gi2acc")
    fun mapGI(@RequestParam(value = "gi") giNumbers: String): List<IdPair> {
        val queryGis = giNumbers.split(',', ';').map(String::toInt).toList()

        val idListFile = createTempFile()

        queryGis.saveAs(idListFile)

        // run the python script over the ids
        val cmd = "cat ${idListFile.absolutePath} | ${INSTALL_DIR}/gi2accession.py"

        val convertedIds: List<IdPair> = evalBash(cmd, wd = INSTALL_DIR).stdout.
                filter(String::isNotBlank).
                map {
                    with(it.split('\t')) { IdPair(this[0].toLong(), this[1], this[2].toLong()) }
                }

        idListFile.delete()

        return convertedIds
    }

}

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    // http://stackoverflow.com/questions/21083170/spring-boot-how-to-configure-port
    System.getProperties().put("server.port", 7050);

    SpringApplication.run(Application::class.java, *args)
}
