package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.generelt.client.GenerelleDataClient
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Collections.synchronizedMap

@Component
class GenerelleDataHolder(val client: GenerelleDataClient) {

    private val personCache: MutableMap<Pid, Person> = synchronizedMap(mutableMapOf())
    private val delingstallCache: MutableMap<DatoAvhengighetCacheKey, DelingstallUtvalg> = synchronizedMap(mutableMapOf())
    private val forholdstallCache: MutableMap<DatoAvhengighetCacheKey, ForholdstallUtvalg> = synchronizedMap(mutableMapOf())
    private val privatAfpSatsCache: MutableMap<DatoAvhengighetCacheKey, PrivatAfpSatser> = synchronizedMap(mutableMapOf())
    private val grunnbeloepCache: MutableMap<GrunnbeloepCacheKey, List<VeietSatsResultat>> = synchronizedMap(mutableMapOf())

    fun getPerson(pid: Pid): Person =
        personCache[pid]
            ?: client.fetchGenerelleData(GenerelleDataSpec.forPerson(pid)).person.also {
                personCache[pid] = it
            }

    fun getDelingstallUtvalg(virkningFom: LocalDate, foedselDato: LocalDate): DelingstallUtvalg {
        val key = DatoAvhengighetCacheKey(virkningFom, foedselDato)

        return delingstallCache[key]
            ?: client.fetchGenerelleData(
                GenerelleDataSpec.forDelingstall(virkningFom, foedselDato)
            ).delingstallUtvalg.also {
                //delingstallCache[key] = it
            }
    }

    fun getForholdstallUtvalg(virkningFom: LocalDate, foedselDato: LocalDate): ForholdstallUtvalg {
        val key = DatoAvhengighetCacheKey(virkningFom, foedselDato)

        return forholdstallCache[key]
            ?: client.fetchGenerelleData(
                GenerelleDataSpec.forForholdstall(virkningFom, foedselDato)
            ).forholdstallUtvalg.also {
                //forholdstallCache[key] = it
            }
    }

    fun getPrivatAfpSatser(virkningFom: LocalDate, foedselDato: LocalDate): PrivatAfpSatser {
        val key = DatoAvhengighetCacheKey(virkningFom, foedselDato)

        return privatAfpSatsCache[key]
            ?: client.fetchGenerelleData(
                GenerelleDataSpec.forPrivatAfp(virkningFom, foedselDato)
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
        val foedselDato: LocalDate
    )

    private data class GrunnbeloepCacheKey(
        val fomAar: Int?,
        val tomAar: Int?
    )
}
