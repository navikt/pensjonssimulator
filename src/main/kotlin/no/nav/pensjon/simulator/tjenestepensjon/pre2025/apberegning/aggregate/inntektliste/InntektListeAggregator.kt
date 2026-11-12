package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.alderVedOvergangFraTidsbegrensetAfpTilAlderspensjon
import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Inntekt
import java.time.LocalDate

object InntektListeAggregator {

    fun aggregate(
        spec: InntektListeSpec,
        actualTime: LocalDate = LocalDate.now(),
        sisteGyldigeOpptjeningsaar: Int
    ): List<Inntekt> {
        val inntektListe: MutableList<Inntekt> = mutableListOf()
        val inntektFoerFoersteUttak = spec.inntektFoerFoersteUttak

        inntektListe.add(
            Inntekt(
                fom = actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1),
                beloep = inntektFoerFoersteUttak.toDouble()
            )
        )

        // inntekt from forsteUttaksDato
        val inntektUnderGradertEllerHeltUttak =
            if (spec.gradertUttak || spec.simuleringTypeErAfpEtterfAlder)
                spec.inntektUnderGradertUttakBeloep
            else
                spec.inntektEtterHeltUttakBeloep

        if (inntektUnderGradertEllerHeltUttak != inntektFoerFoersteUttak) {
            addInntektToListIfPreviuslyIsNotSameAmount(
                inntektListe, spec.foersteUttakDato, inntektUnderGradertEllerHeltUttak.toDouble()
            )
        }

        // Inntekt from heltUttak (optional)
        // is only sett if simuleringSpec.heltUttakDato is not null or simuleringSpec.type is AFP_ETTERF_ALDER
        datoForHeltUttak(spec)?.let {
            addInntektToListIfPreviuslyIsNotSameAmount(
                inntektListe,
                fom = it,
                beloep = spec.inntektEtterHeltUttakBeloep.toDouble()
            )
        }

        addInntektToListIfPreviuslyIsNotSameAmount(inntektListe, fom = spec.inntektTom(), beloep = 0.0)
        return inntektListe
    }

    /**
     * NB: Sammenlign denne med SimuleringEtter2011SpecV2.datoForHeltUttak
     */
    private fun datoForHeltUttak(spec: InntektListeSpec): LocalDate? =
        if (spec.simuleringTypeErAfpEtterfAlder)
            fromAlder(spec.foedselsdato, alder = alderVedOvergangFraTidsbegrensetAfpTilAlderspensjon)
        else
            spec.heltUttakDato

    // if the amount is the same as previous inntekt, it is continuation of the previous inntekt and there is no point in adding.
    private fun addInntektToListIfPreviuslyIsNotSameAmount(
        inntektListe: MutableList<Inntekt>,
        fom: LocalDate,
        beloep: Double
    ) {
        if (inntektListe[inntektListe.size - 1].beloep != beloep) {
            inntektListe.add(Inntekt(fom, beloep))
        }
    }
}