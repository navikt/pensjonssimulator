package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSivilstandSpecV0.Companion.fromExternalValue
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

object AfpEtterfulgtAvAlderspensjonSpecMapperV0 {

    fun fromDto(
        source: AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0,
        hentGrunnbeloep: () -> Int,
        hentSisteInntektFraPOPP: (Pid) -> Int
    ): SimuleringSpec {

        val pid = Pid(source.personId)
        val uttakDato = LocalDate.parse(source.uttakFraOgMedDato)
        val sivilstatus = fromExternalValue(source.sivilstandVedPensjonering).internalValue
        val forventetInntektBeloep: Int = source.fremtidigAarligInntektTilAfpUttak ?: hentSisteInntektFraPOPP(pid)
        val inntektSisteMaanedOver1G = source.inntektSisteMaanedOver1G
        val epsHarPensjon = source.epsPensjon
        val epsHarInntektOver2G = source.eps2G
        val inntektUnderGradertUttakBeloep = source.fremtidigAarligInntektUnderAfpUttak
        val afpInntektMaanedFoerUttak = settMaanedsInntektOver1GFoerUttak(inntektSisteMaanedOver1G, hentGrunnbeloep)
        val utlandAntallAar = source.aarIUtlandetEtter16

        return SimuleringSpec(
            type = SimuleringType.AFP_ETTERF_ALDER,
            pid = pid,
            sivilstatus = sivilstatus,
            epsHarPensjon = epsHarPensjon,
            epsHarInntektOver2G = epsHarInntektOver2G,
            foersteUttakDato = uttakDato,
            heltUttakDato = uttakDato,
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektEtterHeltUttakBeloep = 0,
            inntektOver1GAntallAar = 0, // only for anonym
            inntektUnderGradertUttakBeloep = inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakAntallAar = null, //TODO mangler sluttdato
            forventetInntektBeloep = forventetInntektBeloep,
            utlandAntallAar = utlandAntallAar,
            simulerForTp = false, //simuler for tjenestepensjon
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            utlandPeriodeListe = mutableListOf(),
            epsKanOverskrives = false,
            foedselAar = 0, // only for anonym
            flyktning = false,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                afpOrdning = AfpOrdningType.AFPSTAT, // ingen praktisk betydning i regelmotoren
                inntektMaanedenFoerAfpUttakBeloep = afpInntektMaanedFoerUttak,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = inntektUnderGradertUttakBeloep
            ),
            foedselDato = null, // only for anonym
            avdoed = null,
            isTpOrigSimulering = true,
            uttakGrad = UttakGradKode.P_100,
        )
    }

    private fun settMaanedsInntektOver1GFoerUttak(inntektSisteMaanedOver1G: Boolean, hentGrunnbeloep: () -> Int): Int {
        return if (inntektSisteMaanedOver1G) {
            hentGrunnbeloep.invoke() * TILFELDIG_ANTALL_G_STOERRE_ENN_EN / MAANEDER_I_AAR
        } else {
            TILFELDIG_MAANEDS_INNTEKT_STOERRE_ENN_NULL
        }
    }

    const val MAANEDER_I_AAR = 12
    const val TILFELDIG_MAANEDS_INNTEKT_STOERRE_ENN_NULL = 42
    const val TILFELDIG_ANTALL_G_STOERRE_ENN_EN = 2
}
