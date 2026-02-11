package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.InnvilgetLivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * Maps between data transfer objects (DTOs) and domain objects.
 * The DTOs are specified by version 3 of the API offered to clients.
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
        val gradertUttak: SimuleringGradertUttak? = gradertUttak(source, foedselsdato)
        val heltUttak: SimuleringHeltUttak = heltUttak(source, foedselsdato)
        val utlandPeriodeListe: List<UtlandPeriode> = source.utenlandsperiodeListe.orEmpty().map(::utlandPeriode)

        return SimuleringSpec(
            type = source.simuleringstype.internalValue,
            foedselAar = 0, // only for anonym
            forventetInntektBeloep = source.sisteInntekt,
            inntektOver1GAntallAar = 0, // only for anonym
            foersteUttakDato = (gradertUttak?.uttakFom ?: heltUttak.uttakFom).toNorwegianLocalDate(),
            uttakGrad = gradertUttak?.grad ?: UttakGradKode.P_100,
            inntektUnderGradertUttakBeloep = gradertUttak?.aarligInntekt ?: 0,
            heltUttakDato = gradertUttak?.let { heltUttak.uttakFom.toNorwegianLocalDate() },
            inntektEtterHeltUttakBeloep = heltUttak.aarligInntekt,
            inntektEtterHeltUttakAntallAar = heltUttak.antallArInntektEtterHeltUttak,
            utlandAntallAar = 0, // only for anonym
            sivilstatus = source.sivilstatus.internalValue,
            epsHarPensjon = source.eps?.levende?.harPensjon == true,
            epsHarInntektOver2G = source.eps?.levende?.harInntektOver2G == true,
            erAnonym = false,
            ignoreAvslag = false,
            // Resten er kun for ikke-anonym simulering:
            pid = pid,
            foedselDato = foedselsdato,
            avdoed = source.eps?.avdoed?.let(::avdoed),
            isTpOrigSimulering = false,
            simulerForTp = false,
            flyktning = false,
            utlandPeriodeListe = utlandPeriodeListe.toMutableList(),
            // Inntekt angis før/etter gradert/helt uttak istedenfor via liste:
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            livsvarigOffentligAfp = source.offentligAfp?.innvilgetLivsvarigAfp?.let(::livsvarigOffentligAfp),

            // NB: For tidsbegrenset offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
            pre2025OffentligAfp = tidsbegrensetOffentligAfp(
                simuleringSpec = source,
                inntektAarligBeloep = gradertUttak?.aarligInntekt,
                inntektSisteMaanedOver1G =
                    source.offentligAfp?.harInntektMaanedenFoerUttak?.let(inntektService::hentSisteMaanedsInntektOver1G)
            ),

            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false // verdier fra vedtak vil dermed brukes istedenfor brukeroppgitte verdier
        )
    }

    private fun gradertUttak(
        source: SimuleringSpecDto,
        foedselsdato: LocalDate
    ): SimuleringGradertUttak? =
        source.gradertUttak?.let {
            val localUttakFom: LocalDate = uttakDato(
                foedselsdato,
                uttakAlder = alder(it.uttakFomAlder!!)
            )

            SimuleringGradertUttak(
                grad = it.grad!!.internalValue,
                uttakFom = validated(localUttakFom).toNorwegianDateAtNoon(),
                aarligInntekt = it.aarligInntekt ?: 0
            )
        }

    /**
     * 'Inntekt til og med'-dato beregnes på minimumsbasis for at ikke inntekt skal overvurderes i simuleringen.
     */
    private fun inntektTomDato(foedselsdato: LocalDate, tomAlder: Alder) =
        foedselsdato
            .plusYears(tomAlder.aar.toLong())
            .plusMonths(tomAlder.maaneder.toLong())

    private fun heltUttak(source: SimuleringSpecDto, foedselsdato: LocalDate): SimuleringHeltUttak {
        val heltUttak = source.heltUttak
        val uttakFomAlderSpec = heltUttak.uttakFomAlder
        val inntektTomAlderSpec = heltUttak.inntektTomAlder
        val localUttakFom: LocalDate = uttakDato(foedselsdato, alder(uttakFomAlderSpec))

        return SimuleringHeltUttak(
            uttakFom = validated(localUttakFom).toNorwegianDateAtNoon(),
            aarligInntekt = heltUttak.aarligInntekt,
            inntektTom = inntektTomDato(foedselsdato, alder(inntektTomAlderSpec)).toNorwegianDateAtNoon(),
            antallArInntektEtterHeltUttak = inntektTomAlderSpec.aar - uttakFomAlderSpec.aar + 1 // +1, siden fra/til OG MED
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

    private fun validated(uttakFom: LocalDate): LocalDate =
        if (uttakFom < LocalDate.now())
            throw RegelmotorValideringException("Uttaksdato ($uttakFom) kan ikke være i fortid")
        else
            uttakFom

    /**
     * Domain object for 'gradert uttak' in the context of 'simulering av alderspensjon'.
     */
    private data class SimuleringGradertUttak(
        val grad: UttakGradKode,
        val uttakFom: Date,
        val aarligInntekt: Int
    )

    /**
     * Domain object for 'helt uttak' in the context of 'simulering av alderspensjon'.
     */
    private data class SimuleringHeltUttak(
        val uttakFom: Date, //TODO LocalDate?
        val aarligInntekt: Int,
        val inntektTom: Date,
        val antallArInntektEtterHeltUttak: Int
    )
}
