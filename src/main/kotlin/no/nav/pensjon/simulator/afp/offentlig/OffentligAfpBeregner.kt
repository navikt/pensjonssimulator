package no.nav.pensjon.simulator.afp.offentlig

import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.OVERGANG_TIDSBEGRENSET_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSELSAAR
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpEndringBeregner
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpFoerstegangBeregner
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpTerminator.terminateTidsbegrensetOffentligAfp
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Beregner både tidsbegrenset og livsvarig AFP i offentlig sektor.
 */
@Component
class OffentligAfpBeregner(
    private val tidsbegrensetFoerstegangBeregner: TidsbegrensetOffentligAfpFoerstegangBeregner,
    private val tidsbegrensetEndringBeregner: TidsbegrensetOffentligAfpEndringBeregner,
    private val livsvarigBeregner: LivsvarigOffentligAfpService
) {
    fun beregnAfp(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        ytelser: LoependeYtelser,
        foedselsdato: LocalDate?,
        pid: Pid?
    ): OffentligAfpResult =
        when {
            spec.gjelderTidsbegrensetOffentligAfp() -> {
                val result = tidsbegrensetFoerstegangBeregner.beregnAfp(
                    spec,
                    kravhode,
                    ytelser.forrigeAlderspensjonBeregningResultat
                )
                OffentligAfpResult(tidsbegrenset = result, livsvarig = null, result.kravhode)
            }

            spec.gjelderEndringUtenLivsvarigOffentligAfp() -> //TODO Også sjekke mayHaveTidsbegrensetOffentligAfp her?
                OffentligAfpResult(
                    tidsbegrenset = spec.foersteUttakDato?.let { tidsbegrensetEndringBeregner.beregnAfp(kravhode, it) },
                    livsvarig = null,
                    kravhode
                )

            spec.kreverTermineringAvTidsbegrensetOffentligAfp() && mayHaveTidsbegrensetOffentligAfp(foedselsdato) ->
                OffentligAfpResult(
                    tidsbegrenset = terminateTidsbegrensetOffentligAfp(kravhode, spec.foersteUttakDato),
                    livsvarig = null,
                    kravhode
                )

            spec.gjelderLivsvarigOffentligAfp() ->
                OffentligAfpResult(
                    tidsbegrenset = null,
                    livsvarig = foedselsdato?.let {
                        livsvarigBeregner.beregnAfp(
                            pid!!,
                            foedselsdato = it,
                            forventetAarligInntektBeloep = spec.forventetInntektBeloep,
                            fremtidigeInntekter = spec.fremtidigInntektListe,
                            brukFremtidigInntekt = spec.brukFremtidigInntekt,
                            virkningDato = spec.livsvarigOffentligAfp?.rettTilAfpFom ?: spec.foersteUttakDato!!
                        )
                    },
                    kravhode
                )

            else -> OffentligAfpResult(tidsbegrenset = null, livsvarig = null, kravhode)
        }

    private companion object {
        private fun mayHaveTidsbegrensetOffentligAfp(foedselsdato: LocalDate?): Boolean =
            foedselsdato?.let { it.year < OVERGANG_TIDSBEGRENSET_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSELSAAR } == true
    }
}
