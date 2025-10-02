package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Utleder simuleringstype.
 * NB: Det tas ikke hensyn til "gammel" offentlig AFP, kun "ny" (livsvarig) AFP.
 */
@Component
class SimuleringstypeDeducer(private val vedtakService: VedtakService) {

    fun deduceSimuleringstype(
        pid: Pid,
        uttakFom: LocalDate,
        inkluderPrivatAfp: Boolean,
        livsvarigOffentligAfpRettFom: LocalDate? = null
    ): SimuleringTypeEnum {
        if (inkluderPrivatAfp && livsvarigOffentligAfpRettFom != null)
            throw BadSpecException("kan ikke kombinere privat og offentlig AFP")

        val vedtakInfo = vedtakService.vedtakStatus(pid, uttakFom).also(::checkForGjenlevenderettighet)

        return deduceSimuleringstype(
            erFoerstegangsuttak = vedtakInfo.harGjeldendeVedtak.not(),
            inkluderPrivatAfp,
            livsvarigOffentligAfpRettDato = livsvarigOffentligAfpRettFom
        )
    }

    private companion object {
        // PEN: SimuleringServiceBase.checkForGjenlevenderettighet
        private fun checkForGjenlevenderettighet(vedtakInfo: VedtakStatus) {
            if (vedtakInfo.harGjenlevenderettighet)
                throw FeilISimuleringsgrunnlagetException("Kan ikke simulere bruker med gjenlevenderettigheter")
        }

        private fun deduceSimuleringstype(
            erFoerstegangsuttak: Boolean,
            inkluderPrivatAfp: Boolean,
            livsvarigOffentligAfpRettDato: LocalDate?
        ): SimuleringTypeEnum =
            livsvarigOffentligAfpRettDato?.let {
                if (erFoerstegangsuttak)
                    SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG
                else
                    SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG
            } ?: if (erFoerstegangsuttak)
                if (inkluderPrivatAfp) SimuleringTypeEnum.ALDER_M_AFP_PRIVAT else SimuleringTypeEnum.ALDER
            else
                if (inkluderPrivatAfp) SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT else SimuleringTypeEnum.ENDR_ALDER
    }
}
