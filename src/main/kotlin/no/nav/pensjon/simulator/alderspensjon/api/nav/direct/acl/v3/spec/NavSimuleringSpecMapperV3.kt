package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import java.time.LocalDate
import java.util.*

/**
 * Maps between data transfer objects (DTOs) and domain objects related to 'anonym simulering'.
 * The DTOs are specified by version 1 of the API offered to clients.
 */
object NavSimuleringSpecMapperV3 {

    fun fromNavSimuleringSpecV3(
        source: NavSimuleringSpecV3,
        foedselsdato: LocalDate
    ): SimuleringSpec {
        val gradertUttak: SimuleringGradertUttak? = gradertUttak(source, foedselsdato)
        val heltUttak: SimuleringHeltUttak = heltUttak(source, foedselsdato)
        val utlandPeriodeListe: List<UtlandPeriode> =
            source.utenlandsperiodeListe.orEmpty().map(::utlandPeriode)

        return SimuleringSpec(
            type = NavSimuleringTypeSpecV3.fromExternalValue(source.simuleringstype.name).internalValue,
            foedselAar = 0, // only for anonym
            forventetInntektBeloep = source.sisteInntekt,
            inntektOver1GAntallAar = 0, // only for anonym
            foersteUttakDato = (gradertUttak?.uttakFom ?: heltUttak.uttakFom).toNorwegianLocalDate(),
            uttakGrad = gradertUttak?.let { NavUttakGradSpecV3.fromExternalValue(it.grad.value).internalValue }
                ?: UttakGradKode.P_100,
            inntektUnderGradertUttakBeloep = gradertUttak?.aarligInntekt ?: 0,
            heltUttakDato = heltUttak.uttakFom.toNorwegianLocalDate(),
            inntektEtterHeltUttakBeloep = heltUttak.aarligInntekt,
            inntektEtterHeltUttakAntallAar = heltUttak.antallArInntektEtterHeltUttak,
            utlandAntallAar = 0, // only for anonym
            sivilstatus = NavSivilstandSpecV3.fromExternalValue(source.sivilstand.name).internalValue,
            epsHarPensjon = source.epsHarPensjon == true,
            epsHarInntektOver2G = source.epsHarInntektOver2G == true,
            erAnonym = false,
            ignoreAvslag = false,
            // Resten er kun for ikke-anonym simulering:
            pid = Pid(source.pid),
            foedselDato = foedselsdato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            flyktning = false,
            utlandPeriodeListe = utlandPeriodeListe.toMutableList(),
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty()
                .map(::fremtidigInntekt).toMutableList(),
            rettTilOffentligAfpFom = null,
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false
        )
    }

    private fun gradertUttak(
        source: NavSimuleringSpecV3,
        foedselsdato: LocalDate
    ): SimuleringGradertUttak? =
        source.gradertUttak?.let {
            val localUttakFom: LocalDate =
            // if (source.isTpOriginatedSimulering)
            //     it.uttakFom.dato
            // else
                //TODO V4
                uttakDato(foedselsdato, alder(it.uttakFomAlder!!))

            SimuleringGradertUttak(
                grad = it.grad!!,
                uttakFom = validated(localUttakFom).toNorwegianDateAtNoon(),
                aarligInntekt = it.aarligInntekt ?: 0
            )
        }

    /**
     * 'Inntekt til og med'-dato beregnes på minimumsbasis for at ikke inntekt skal overvurderes i simuleringen.
     */
    // SimulatorPensjonTidUtil
    private fun inntektTomDato(foedselsdato: LocalDate, tomAlder: Alder) =
        foedselsdato
            .plusYears(tomAlder.aar.toLong())
            .plusMonths(tomAlder.maaneder.toLong())

    private fun heltUttak(source: NavSimuleringSpecV3, foedselDato: LocalDate): SimuleringHeltUttak {
        val heltUttak = source.heltUttak
        val uttakFomAlderSpec = heltUttak.uttakFomAlder
        val inntektTomAlderSpec = heltUttak.inntektTomAlder

        val localUttakFom: LocalDate =
        //if (source.isTpOriginatedSimulering)
        //    heltUttak.uttakFom.dato
        //else
            //TODO V4
            uttakDato(foedselDato, alder(uttakFomAlderSpec))

        return SimuleringHeltUttak(
            uttakFom = validated(localUttakFom).toNorwegianDateAtNoon(),
            aarligInntekt = heltUttak.aarligInntekt,
            inntektTom = inntektTomDato(foedselDato, alder(inntektTomAlderSpec)).toNorwegianDateAtNoon(),
            antallArInntektEtterHeltUttak = inntektTomAlderSpec.aar - uttakFomAlderSpec.aar + 1 // +1, siden fra/til OG MED
        )
    }

    private fun fremtidigInntekt(source: NavSimuleringInntektSpecV3) =
        FremtidigInntekt(
            aarligInntektBeloep = source.aarligInntekt ?: 0,
            fom = source.fom!!.toNorwegianLocalDate()
        )

    private fun utlandPeriode(source: NavSimuleringUtlandSpecV3) =
        UtlandPeriode(
            fom = source.fom.toNorwegianLocalDate(),
            tom = source.tom?.toNorwegianLocalDate(),
            land = LandkodeEnum.valueOf(source.land),
            arbeidet = source.arbeidetUtenlands
        )

    private fun alder(source: NavSimuleringAlderSpecV3) = Alder(source.aar, source.maaneder)

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
