package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Inntekt
import java.time.LocalDate

object InntektListeAggregator {

    fun aggregate(spec: InntektListeSpec,
                  actualTime: LocalDate = LocalDate.now(),
                  sisteGyldigeOpptjeningsaar: Int = SISTE_GYLDIGE_OPPTJENING_AAR): List<Inntekt> {

        val inntektList: MutableList<Inntekt> = ArrayList()

        // First inntekt before uttak
        val inntektFoerFoersteUttak = spec.inntektFoerFoersteUttak
        inntektList.add(
            Inntekt(
                actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1),
                inntektFoerFoersteUttak.toDouble()
            )
        )

        // inntekt from forsteUttaksDato
        val inntektUnderGradertEllerHeltUttak =
            if (spec.gradertUttak || spec.simuleringTypeErAfpEtterfAlder) {
                spec.inntektUnderGradertUttakBeloep
            } else {
                spec.inntektEtterHeltUttakBeloep
            }

        if (inntektUnderGradertEllerHeltUttak != inntektFoerFoersteUttak) {
            addInntektToListIfPreviuslyIsNotSameAmount(
                inntektList, spec.foersteUttakDato, inntektUnderGradertEllerHeltUttak.toDouble()
            )
        }

        var date: LocalDate? = spec.heltUttakDato

        // Inntekt from heltUttak (optional)
        if (spec.simuleringTypeErAfpEtterfAlder) {
            date = fromAlder(foedselDato = spec.foedselsdato, alder = Alder(67, 0))
        }

        // is only sett if simuleringSpec.heltUttakDato is not null or simuleringSpec.type is AFP_ETTERF_ALDER
        if (date != null) {
            addInntektToListIfPreviuslyIsNotSameAmount(
                inntektList,
                date,
                spec.inntektEtterHeltUttakBeloep.toDouble()
            )
        }

        addInntektToListIfPreviuslyIsNotSameAmount(
            inntektList,
            getEndingDateForInntektList(spec),
            0.0
        )

        return inntektList
    }

    // if the amount is the same as previous inntekt, it is continuation of the previous inntekt and there is no point in adding.
    fun addInntektToListIfPreviuslyIsNotSameAmount(
        inntektList: MutableList<Inntekt>,
        date: LocalDate,
        inntekt: Double
    ) {
        if (inntektList[inntektList.size - 1].beloep != inntekt) {
            inntektList.add(Inntekt(date, inntekt))
        }
    }

    fun getEndingDateForInntektList(spec: InntektListeSpec): LocalDate {
        val date: LocalDate

        if (spec.simuleringTypeErAfpEtterfAlder) {
            // Inntekt from heltUttak (optional)
            // Compenstating for bad front-end data, should be unnecesary
            date = fromAlder(foedselDato = spec.foedselsdato, alder = Alder(67, 0))
        } else if (spec.heltUttakDato != null) {
            date = spec.heltUttakDato
        } else {
            date = spec.foersteUttakDato
        }

        // Two possible end date for Inntekt, on that is 75 or one that is optional for before 75.
        val dateWithUserAt75AndFirstDayAtNextMonth: LocalDate =
            fromAlder(foedselDato = spec.foedselsdato, alder = Alder(75, 0))
        val dateWithAddedAntallArInntektEtterHeltUttak: LocalDate =
            date.plusYears(spec.inntektEtterHeltUttakAntallAar?.toLong() ?: 0L)

        // Check the ending of the final Inntekt, added option to ending before 75 or sett to 75.
        return if (spec.inntektEtterHeltUttakBeloep > 0 && (spec.inntektEtterHeltUttakAntallAar ?: 0) > 0
            && dateWithAddedAntallArInntektEtterHeltUttak.isBefore(dateWithUserAt75AndFirstDayAtNextMonth)
        ) {
            dateWithAddedAntallArInntektEtterHeltUttak
        } else {
            dateWithUserAt75AndFirstDayAtNextMonth
        }
    }
}