package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.InnvilgetLivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Maps between data transfer objects (DTOs) and domain objects.
 */
@Component
class SimuleringSpecMapperForNav(
    private val personService: GeneralPersonService,
    private val inntektService: InntektService,
    private val grunnbeloepService: GrunnbeloepService
) {
    fun fromDto(source: SimuleringSpecDto): SimuleringSpec {
        val pid = Pid(source.pid)
        val foedselsdato = personService.foedselsdato(pid)
        val gradertUttak: GradertUttakSpec? = source.gradertUttak?.let { gradertUttak(it, foedselsdato) }
        val heltUttak: HeltUttakSpec = heltUttak(source.heltUttak, foedselsdato)
        val utlandPeriodeListe: List<UtlandPeriode> = source.utenlandsperiodeListe.orEmpty().map(::utlandPeriode)
        val eps: EpsSpecDto? = source.eps
        val levendeEps: LevendeEpsDto? = eps?.levende
        val offentligAfp: OffentligAfpSpecDto? = source.offentligAfp

        return SimuleringSpec(
            type = source.simuleringstype.internalValue,
            foedselAar = 0, // only for anonym
            forventetInntektBeloep = source.sisteInntekt,
            inntektOver1GAntallAar = 0, // only for anonym
            foersteUttakDato = gradertUttak?.uttakFom ?: heltUttak.uttakFom,
            uttakGrad = gradertUttak?.grad ?: UttakGradKode.P_100,
            inntektUnderGradertUttakBeloep = gradertUttak?.aarligInntekt ?: 0,
            heltUttakDato = gradertUttak?.let { heltUttak.uttakFom },
            inntektEtterHeltUttakBeloep = heltUttak.aarligInntekt,
            inntektEtterHeltUttakAntallAar = heltUttak.inntektEtterHeltUttakAntallAar,
            utlandAntallAar = 0, // only for anonym
            sivilstatus = source.sivilstatus.internalValue,
            epsHarPensjon = levendeEps?.harPensjon == true,
            epsHarInntektOver2G = levendeEps?.harInntektOver2G == true,
            erAnonym = false,
            ignoreAvslag = false,
            // Resten er kun for ikke-anonym simulering:
            pid = pid,
            foedselDato = foedselsdato,
            avdoed = eps?.avdoed?.let(::avdoed),
            isTpOrigSimulering = false,
            simulerForTp = false,
            flyktning = false,
            utlandPeriodeListe = utlandPeriodeListe.toMutableList(),
            // Inntekt angis før/etter gradert/helt uttak istedenfor via liste:
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            livsvarigOffentligAfp = offentligAfp?.innvilgetLivsvarigAfp?.let(::livsvarigOffentligAfp),

            // NB: For tidsbegrenset offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
            pre2025OffentligAfp = tidsbegrensetOffentligAfp(
                simuleringSpec = source,
                inntektAarligBeloep = gradertUttak?.aarligInntekt,
                inntektSisteMaanedOver1G =
                    offentligAfp?.harInntektMaanedenFoerUttak?.let(inntektService::hentSisteMaanedsInntektOver1G)
            ),

            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false // verdier fra vedtak vil dermed brukes istedenfor brukeroppgitte verdier
        )
    }

    private fun gradertUttak(source: GradertUttakSpecDto, foedselsdato: LocalDate) =
        GradertUttakSpec(
            grad = source.grad!!.internalValue,
            uttakFom = uttakDato(foedselsdato, uttakAlder = alder(source.uttakFomAlder!!)),
            aarligInntekt = source.aarligInntekt ?: 0
        )

    private fun heltUttak(source: HeltUttakSpecDto, foedselsdato: LocalDate): HeltUttakSpec {
        val fomAlder = alder(source.uttakFomAlder)
        val tomAlder = alder(source.inntektTomAlder)

        return HeltUttakSpec(
            uttakFom = uttakDato(foedselsdato, uttakAlder = fomAlder),
            aarligInntekt = source.aarligInntekt,
            inntektTom = inntektTomDato(foedselsdato, tomAlder),
            inntektEtterHeltUttakAntallAar = tomAlder.aar - fomAlder.aar + 1 // +1, siden fra/til OG MED
        )
    }

    private fun livsvarigOffentligAfp(source: InnvilgetLivsvarigOffentligAfpSpecDto) =
        LivsvarigOffentligAfpSpec(
            innvilgetAfp = InnvilgetLivsvarigOffentligAfpSpec(
                aarligBruttoBeloep = source.aarligBruttoBeloep,
                uttakFom = source.uttakFom,
                sistRegulertGrunnbeloep = source.sistRegulertGrunnbeloep ?: grunnbeloepService.naavaerendeGrunnbeloep()
            )
        )

    private companion object {

        /**
         * 'Inntekt til og med'-dato beregnes på minimumsbasis for at ikke inntekt skal overvurderes i simuleringen.
         */
        private fun inntektTomDato(foedselsdato: LocalDate, tomAlder: Alder) =
            foedselsdato
                .plusYears(tomAlder.aar.toLong())
                .plusMonths(tomAlder.maaneder.toLong())

        private fun tidsbegrensetOffentligAfp(
            simuleringSpec: SimuleringSpecDto,
            inntektAarligBeloep: Int?,
            inntektSisteMaanedOver1G: Int?
        ): Pre2025OffentligAfpSpec? =
            if (simuleringSpec.simuleringstype == SimuleringstypeSpecDto.ALDERSPENSJON_MED_TIDSBEGRENSET_OFFENTLIG_AFP)
                Pre2025OffentligAfpSpec(
                    afpOrdning = simuleringSpec.offentligAfp?.afpOrdning?.internalValue!!,
                    inntektMaanedenFoerAfpUttakBeloep = inntektSisteMaanedOver1G ?: 0,
                    inntektUnderAfpUttakBeloep = inntektAarligBeloep ?: 0
                )
            else
                null

        private fun utlandPeriode(source: UtlandSpecDto) =
            UtlandPeriode(
                fom = source.fom,
                tom = source.tom,
                land = LandkodeEnum.valueOf(source.land),
                arbeidet = source.arbeidetUtenlands
            )

        private fun avdoed(source: AvdoedEpsDto) =
            Avdoed(
                pid = Pid(source.pid),
                antallAarUtenlands = source.antallAarUtenlands ?: 0,
                inntektFoerDoed = source.inntektFoerDoedBeloep ?: 0,
                doedDato = source.doedsdato,
                erMedlemAvFolketrygden = source.medlemAvFolketrygden == true,
                harInntektOver1G = source.inntektErOverGrunnbeloepet == true
            )

        private fun alder(source: AlderSpecDto) =
            Alder(source.aar, source.maaneder)
    }

    private data class GradertUttakSpec(
        val grad: UttakGradKode,
        val uttakFom: LocalDate,
        val aarligInntekt: Int
    )

    private data class HeltUttakSpec(
        val uttakFom: LocalDate,
        val aarligInntekt: Int,
        val inntektTom: LocalDate,
        val inntektEtterHeltUttakAntallAar: Int
    )
}
