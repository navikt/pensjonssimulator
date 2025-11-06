package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Collections.synchronizedMap

@Component
class GenerelleDataHolder(val client: GenerelleDataClient) {

    private var opptjeningsaarCache: Int? = null
    private val personCache: MutableMap<Pid, Person> = synchronizedMap(mutableMapOf())
    private val privatAfpSatsCache: MutableMap<DatoAvhengighetCacheKey, PrivatAfpSatser> = synchronizedMap(mutableMapOf())
    private val grunnbeloepCache: MutableMap<GrunnbeloepCacheKey, List<VeietSatsResultat>> = synchronizedMap(mutableMapOf())

    fun getSisteGyldigeOpptjeningsaar(): Int =
        opptjeningsaarCache
            ?: client.fetchGenerelleData(GenerelleDataSpec.forOpptjeningsaar()).sisteGyldigeOpptjeningsaar.also {
                opptjeningsaarCache = it
            }

    fun getPerson(pid: Pid): Person =
        personCache[pid]
            ?: client.fetchGenerelleData(GenerelleDataSpec.forPerson(pid)).person.also {
                personCache[pid] = it
            }

    fun getPrivatAfpSatser(virkningFom: LocalDate, foedselsdato: LocalDate): PrivatAfpSatser {
        val key = DatoAvhengighetCacheKey(virkningFom, foedselsdato)

        return privatAfpSatsCache[key]
            ?: client.fetchGenerelleData(
                GenerelleDataSpec.forPrivatAfp(virkningFom, foedselsdato)
            ).privatAfpSatser.also {
                privatAfpSatsCache[key] = it
            }
    }

    fun getVeietGrunnbeloepListe(fomAar: Int?, tomAar: Int?): List<VeietSatsResultat> {
        val key = GrunnbeloepCacheKey(fomAar, tomAar)

        return grunnbeloepCache[key]
            ?: client.fetchGenerelleData(
                GenerelleDataSpec.forVeietGrunnbeloep(fomAar, tomAar)
            ).satsResultatListe.also {
                grunnbeloepCache[key] = it
            }
    }

    private data class DatoAvhengighetCacheKey(
        val virkningFom: LocalDate,
        val foedselsdato: LocalDate
    )

    private data class GrunnbeloepCacheKey(
        val fomAar: Int?,
        val tomAar: Int?
    )
}
