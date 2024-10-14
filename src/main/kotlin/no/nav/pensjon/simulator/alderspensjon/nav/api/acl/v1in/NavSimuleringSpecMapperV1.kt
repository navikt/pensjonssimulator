package no.nav.pensjon.simulator.alderspensjon.nav.api.acl.v1in

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import java.time.LocalDate
import java.util.*

/**
 * Maps between data transfer objects (DTOs) and domain objects related to 'anonym simulering'.
 * The DTOs are specified by version 1 of the API offered to clients.
 */
object NavSimuleringSpecMapperV1 {

    fun fromNavSimuleringSpecV1(
        source: NavSimuleringSpecV1,
        foedselDato: LocalDate
    ): SimuleringSpec {
        val gradertUttak: SimuleringGradertUttak? = gradertUttak(source, foedselDato)
        val heltUttak: SimuleringHeltUttak = heltUttak(source, foedselDato)
        val utenlandsperiodeListe: List<UtlandPeriode> = source.utenlandsperiodeListe.orEmpty().map(
            NavSimuleringSpecMapperV1::utlandPeriode
        )

        return SimuleringSpec(
            type = NavSimuleringTypeSpecV1.fromExternalValue(source.simuleringstype.name).internalValue,
            foedselAar = 0, // only for anonym
            forventetInntektBeloep = source.sisteInntekt,
            inntektOver1GAntallAar = 0, // only for anonym
            foersteUttakDato = (gradertUttak?.uttakFom ?: heltUttak.uttakFom).toLocalDate(),
            uttakGrad = gradertUttak?.let { NavUttakGradSpecV1.fromExternalValue(it.grad.value).internalValue }
                ?: UttakGradKode.P_100,
            inntektUnderGradertUttakBeloep = gradertUttak?.aarligInntekt ?: 0,
            heltUttakDato = heltUttak.uttakFom.toLocalDate(),
            inntektEtterHeltUttakBeloep = heltUttak.aarligInntekt,
            inntektEtterHeltUttakAntallAar = heltUttak.antallArInntektEtterHeltUttak,
            utlandAntallAar = 0, // only for anonym
            sivilstatus = NavSivilstandSpecV1.fromExternalValue(source.sivilstand.name).internalValue,
            epsHarPensjon = source.epsHarPensjon ?: false,
            epsHarInntektOver2G = source.epsHarInntektOver2G ?: false,
            erAnonym = false,
            // Resten er kun for ikke-anonym simulering:
            pid = Pid(source.pid),
            foedselDato = foedselDato,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            boddUtenlands = utenlandsperiodeListe.isNotEmpty(),
            flyktning = false,
            utlandPeriodeListe = utenlandsperiodeListe.toMutableList(),
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(NavSimuleringSpecMapperV1::fremtidigInntekt).toMutableList(),
            rettTilOffentligAfpFom = null
        )
    }

    private fun gradertUttak(
        source: NavSimuleringSpecV1,
        foedselDato: LocalDate
    ): SimuleringGradertUttak? =
        source.gradertUttak?.let {
            val localUttakFom: LocalDate =
            // if (source.isTpOriginatedSimulering)
            //     it.uttakFom.dato
            // else
                //TODO V4
                uttakDato(foedselDato, alder(it.uttakFomAlder!!))

            SimuleringGradertUttak(
                grad = it.grad!!,
                uttakFom = fromLocalDate(validated(localUttakFom))!!,
                aarligInntekt = it.aarligInntekt ?: 0
            )
        }

    /**
     * 'Inntekt til og med'-dato beregnes på minimumsbasis for at ikke inntekt skal overvurderes i simuleringen.
     */
    // SimulatorPensjonTidUtil
    private fun inntektTomDato(foedselDato: LocalDate, tomAlder: Alder) =
        foedselDato
            .plusYears(tomAlder.aar.toLong())
            .plusMonths(tomAlder.maaneder.toLong())

    private fun heltUttak(source: NavSimuleringSpecV1, foedselDato: LocalDate): SimuleringHeltUttak {
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
            uttakFom = fromLocalDate(validated(localUttakFom))!!,
            aarligInntekt = heltUttak.aarligInntekt,
            inntektTom = fromLocalDate(inntektTomDato(foedselDato, alder(inntektTomAlderSpec)))!!,
            antallArInntektEtterHeltUttak = inntektTomAlderSpec.aar - uttakFomAlderSpec.aar + 1 // +1, siden fra/til OG MED
        )
    }

    private fun fremtidigInntekt(source: NavSimuleringInntektSpecV1) =
        FremtidigInntekt(
            aarligInntektBeloep = source.aarligInntekt ?: 0,
            fom = source.fom.toLocalDate()!!
        )

    private fun utlandPeriode(source: NavSimuleringUtlandSpecV1) =
        UtlandPeriode(
            fom = source.fom.toLocalDate()!!,
            tom = source.tom.toLocalDate()!!,
            land = LandkodeEnum.valueOf(source.land),
            arbeidet = source.arbeidetUtenlands
        )

    private fun alder(source: NavSimuleringAlderSpecV1) = Alder(source.aar, source.maaneder)

    private fun validated(uttakFom: LocalDate): LocalDate =
        if (uttakFom < LocalDate.now())
            throw BeregningsmotorValidereException("Uttaksdato ($uttakFom) kan ikke være i fortid")
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
