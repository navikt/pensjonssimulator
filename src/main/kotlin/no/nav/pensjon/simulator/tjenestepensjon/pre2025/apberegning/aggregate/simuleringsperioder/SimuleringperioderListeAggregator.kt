package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsperiode
import java.time.LocalDate

object SimuleringperioderListeAggregator {

    fun aggregate(spec: SimuleringsperioderSpec): List<Simuleringsperiode> {
        val stillingsprosent = spec.stillingsprosentSpec.stillingsprosentOffHeltUttak

        return if (spec.simuleringType == SimuleringTypeEnum.AFP_ETTERF_ALDER) {
            createSimuleringsdataListeForAfpEtterfulgtAvAlderspensjon(spec)
        } else {
            createSimuleringsdataListeForAlderspensjon(
                spec,
                stillingsprosent ?: 0
            )
        }
    }

    private fun createSimuleringsdataListeForAfpEtterfulgtAvAlderspensjon(spec: SimuleringsperioderSpec)
    : MutableList<Simuleringsperiode> {
        val simuleringsperiodeList: MutableList<Simuleringsperiode> = ArrayList()

        simuleringsperiodeList.add(
            setSimuleringsperiodeForGradertUttak(
                spec,
                0
            )
        )

        simuleringsperiodeList.add(
            createSimuleringsperiode(
                fromAlder(foedselDato = spec.foedselsdato, alder = Alder(67, 0)),
                100,
                0,
                spec.etterfulgtAvALderListe
            )
        )

        return simuleringsperiodeList
    }

    private fun setSimuleringsperiodeForGradertUttak(spec: SimuleringsperioderSpec, uttaksgrad: Int): Simuleringsperiode {
        val stillingsprosentOffGradertUttak = spec.stillingsprosentSpec.stillingsprosentOffGradertUttak ?: 0

        return createSimuleringsperiode(
            spec.foersteUttakDato,
            uttaksgrad,
            stillingsprosentOffGradertUttak,
            spec.etterfulgtAvALderListe
        )
    }

    private fun createSimuleringsdataListeForAlderspensjon(spec: SimuleringsperioderSpec, stillingsprosent: Int): MutableList<Simuleringsperiode> {
        val simuleringsperiodeList: MutableList<Simuleringsperiode> = ArrayList()

        if (spec.uttaksgrad < 100) { //Gradert uttak
            simuleringsperiodeList.add(
                setSimuleringsperiodeForGradertUttak(
                    spec,
                    spec.uttaksgrad
                )
            )

            simuleringsperiodeList.add(
                createSimuleringsperiode(
                    spec.heltUttakDato!!,
                    100,
                    stillingsprosent,
                    spec.etterfulgtAvALderListe
                )
            )

            if (stillingsprosent != 0) {
                simuleringsperiodeList.add(
                    createSimuleringsperiode(
                        spec.heltUttakDato.plusYears(
                            spec.inntektEtterHeltUttakAntallAar
                        ),
                        100,
                        0,
                        spec.etterfulgtAvALderListe
                    )
                )
            }
        } else {
            simuleringsperiodeList.add(
                createSimuleringsperiode(
                    spec.foersteUttakDato,
                    spec.uttaksgrad,
                    stillingsprosent,
                    spec.etterfulgtAvALderListe
                )
            )

            if (stillingsprosent != 0) {
                simuleringsperiodeList.add(
                    createSimuleringsperiode(
                        spec.foersteUttakDato.plusYears(
                            spec.inntektEtterHeltUttakAntallAar
                        ),
                        spec.uttaksgrad,
                        0,
                        spec.etterfulgtAvALderListe
                    )
                )
            }
        }

        return simuleringsperiodeList
    }

    private fun createSimuleringsperiode(
        uttakDato: LocalDate,
        uttaksgrad: Int,
        stillingsprosent: Int,
        etterfulgtAvAlderListe: Boolean
    ): Simuleringsperiode = Simuleringsperiode(
        fom = uttakDato,
        folketrygdUttaksgrad = uttaksgrad,
        stillingsprosentOffentlig = stillingsprosent,
        simulerAFPOffentligEtterfulgtAvAlder = etterfulgtAvAlderListe
    )
}