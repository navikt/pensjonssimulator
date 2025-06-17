package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.RegisterData
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.inntekt.InntektService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AfpEtterfulgtAvAlderspensjonSpecMapperV0(
    private val personService: GeneralPersonService,
    private val inntektService: InntektService
) {
    fun fromDto(source: AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0): SimuleringSpec {
        val pid = Pid(source.personId)

        val sisteLignetInntekt: Inntekt? =
            if (source.fremtidigAarligInntektTilAfpUttak == null) inntektService.hentSisteLignetInntekt(pid)
            else null

        return SimuleringSpec(
            type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            pid = pid,
            sivilstatus = AfpEtterfulgtAvAlderspensjonSivilstandSpecV0.fromExternalValue(source.sivilstandVedPensjonering).internalValue,
            epsHarPensjon = source.epsPensjon,
            epsHarInntektOver2G = source.eps2G,
            foersteUttakDato = LocalDate.parse(source.uttakFraOgMedDato),
            heltUttakDato = null, // alltid 67 år ved AFP etterfulgt av AP
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektEtterHeltUttakBeloep = 0,
            inntektOver1GAntallAar = 0, // only for anonym

            // NB: For AFP_ETTERF_ALDER er gradert uttak irrelevant, så denne tilordningen er trolig unødvendig:
            inntektUnderGradertUttakBeloep = source.fremtidigAarligInntektUnderAfpUttak,

            inntektEtterHeltUttakAntallAar = null, //TODO mangler sluttdato
            forventetInntektBeloep = source.fremtidigAarligInntektTilAfpUttak ?: sisteLignetInntekt!!.aarligBeloep,
            utlandAntallAar = source.aarIUtlandetEtter16,
            simulerForTp = false, // simulerer her ikke for tjenestepensjon
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
                inntektMaanedenFoerAfpUttakBeloep =
                    inntektService.hentSisteMaanedsInntektOver1G(source.inntektSisteMaanedOver1G),
                inntektUnderAfpUttakBeloep = source.fremtidigAarligInntektUnderAfpUttak
            ),
            foedselDato = personService.foedselsdato(pid),
            avdoed = null,
            isTpOrigSimulering = true,
            uttakGrad = UttakGradKode.P_100,
            registerData = RegisterData(sisteLignetInntektAar = sisteLignetInntekt?.fom?.year)
        )
    }
}
