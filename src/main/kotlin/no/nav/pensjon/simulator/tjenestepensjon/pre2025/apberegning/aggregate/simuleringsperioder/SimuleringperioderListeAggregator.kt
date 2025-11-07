package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsperiode
import java.time.LocalDate

object SimuleringperioderListeAggregator {

    fun aggregate(spec: SimuleringsperioderSpec): List<Simuleringsperiode> {
        return if (spec.simuleringType == SimuleringTypeEnum.AFP_ETTERF_ALDER) {
            createSimuleringsdataListeForAfpEtterfulgtAvAlderspensjon(spec)
        } else {
            createSimuleringsdataListeForAlderspensjon(spec)
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
                spec.afpEtterfulgtAvAlder
            )
        )

        return simuleringsperiodeList
    }

    private fun setSimuleringsperiodeForGradertUttak(spec: SimuleringsperioderSpec, folketrygdUttaksgrad: Int): Simuleringsperiode {
        val stillingsprosentOffGradertUttak = spec.stillingsprosentSpec.stillingsprosentOffGradertUttak ?: 0

        return createSimuleringsperiode(
            spec.foersteUttakDato,
            folketrygdUttaksgrad,
            stillingsprosentOffGradertUttak,
            spec.afpEtterfulgtAvAlder
        )
    }

    private fun createSimuleringsdataListeForAlderspensjon(spec: SimuleringsperioderSpec): MutableList<Simuleringsperiode> {
        val stillingsprosent = spec.stillingsprosentSpec.stillingsprosentOffHeltUttak ?: 0
        val simuleringsperiodeList: MutableList<Simuleringsperiode> = ArrayList()

        if (spec.folketrygdUttaksgrad < 100) { //Gradert uttak
            simuleringsperiodeList.add(
                setSimuleringsperiodeForGradertUttak(
                    spec,
                    spec.folketrygdUttaksgrad
                )
            )

            simuleringsperiodeList.add(
                createSimuleringsperiode(
                    spec.heltUttakDato!!, //utfylt ved gradert uttak
                    100,
                    stillingsprosent,
                    spec.afpEtterfulgtAvAlder
                )
            )

            if (stillingsprosent != 0) {
                simuleringsperiodeList.add(
                    createSimuleringsperiode(
                        spec.heltUttakDato.plusYears(
                            spec.inntektEtterHeltUttakAntallAar.toLong()
                        ),
                        100,
                        0,
                        spec.afpEtterfulgtAvAlder
                    )
                )
            }
        } else {
            simuleringsperiodeList.add(
                createSimuleringsperiode(
                    spec.foersteUttakDato,
                    spec.folketrygdUttaksgrad,
                    stillingsprosent,
                    spec.afpEtterfulgtAvAlder
                )
            )

            if (stillingsprosent != 0) {
                simuleringsperiodeList.add(
                    createSimuleringsperiode(
                        spec.foersteUttakDato.plusYears(
                            spec.inntektEtterHeltUttakAntallAar.toLong()
                        ),
                        spec.folketrygdUttaksgrad,
                        0,
                        spec.afpEtterfulgtAvAlder
                    )
                )
            }
        }

        return simuleringsperiodeList
    }

    private fun createSimuleringsperiode(
        uttakDato: LocalDate,
        folketrygdUttaksgrad: Int,
        stillingsprosent: Int,
        afpEtterfulgtAvAlder: Boolean
    ): Simuleringsperiode = Simuleringsperiode(
        fom = uttakDato,
        folketrygdUttaksgrad = folketrygdUttaksgrad,
        stillingsprosentOffentlig = stillingsprosent,
        simulerAFPOffentligEtterfulgtAvAlder = afpEtterfulgtAvAlder
    )
}