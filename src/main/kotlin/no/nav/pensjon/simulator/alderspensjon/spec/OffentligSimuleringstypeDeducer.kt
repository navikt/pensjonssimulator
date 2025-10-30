package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.VedtakService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Utleder simuleringstype i "offentlig" sammenheng, dvs. at f.eks. privat AFP ikke tas hensyn til.
 * Det tas heller ikke hensyn til "gammel" offentlig AFP, kun "ny" (livsvarig) AFP.
 */
@Component
class OffentligSimuleringstypeDeducer(private val vedtakService: VedtakService) {

    fun deduceSimuleringstype(
        pid: Pid,
        uttakFom: LocalDate,
        livsvarigOffentligAfpRettFom: LocalDate? = null
    ): SimuleringTypeEnum {
        val vedtakInfo = vedtakService.vedtakStatus(pid, uttakFom)
        // NB: I PEN ble simulering med gjenlevenderett hindret, mens her støttes det

        return deduceSimuleringstype(
            erFoerstegangsuttak = vedtakInfo.harGjeldendeVedtak.not(),
            livsvarigOffentligAfpRettDato = livsvarigOffentligAfpRettFom
        )
    }

    private companion object {

        private fun deduceSimuleringstype(
            erFoerstegangsuttak: Boolean,
            livsvarigOffentligAfpRettDato: LocalDate?
        ): SimuleringTypeEnum =
            livsvarigOffentligAfpRettDato?.let {
                if (erFoerstegangsuttak)
                    SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG
                else
                    SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
            } ?: if (erFoerstegangsuttak)
                SimuleringTypeEnum.ALDER
            else
                SimuleringTypeEnum.ENDR_ALDER
    }
}
