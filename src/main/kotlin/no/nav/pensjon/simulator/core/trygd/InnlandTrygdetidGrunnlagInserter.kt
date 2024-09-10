package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.trygdetidPeriode
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.InsertTrygdetidsgrunnlagForDomesticPeriods
object InnlandTrygdetidGrunnlagInserter {

    fun createTrygdetidGrunnlagForInnlandPerioder(
        trygdetidGrunnlagListe: List<TrygdetidOpphold>,
        foedselDato: LocalDate?
    ): List<TrygdetidOpphold> {
        if (trygdetidGrunnlagListe.isEmpty()) return emptyList()

        val sortedGrunnlagListe = trygdetidGrunnlagListe.sortedBy { it.periode.fom }.toMutableList()
        addGrunnlagFraFoedselDato(sortedGrunnlagListe, foedselDato)
        addGrunnlagAfterLastUtlandTrygdetidGrunnlag(sortedGrunnlagListe)
        addGrunnlagBetweenUtlandTrygdetidGrunnlag(sortedGrunnlagListe)
        return sortedGrunnlagListe
    }

    private fun addGrunnlagFraFoedselDato(grunnlagListe: MutableList<TrygdetidOpphold>, foedselDato: LocalDate?) {
        val fom: LocalDate? = grunnlagListe.firstOrNull()?.periode?.fom.toLocalDate()
        if (fom == foedselDato) return

        grunnlagListe.add(
            index = 0,
            element = trygdetidMedArbeid(
                fom = foedselDato,
                tom = fom?.minusDays(1)
            )
        )
    }

    private fun addGrunnlagAfterLastUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        val tom = grunnlagListe[grunnlagListe.size - 1].periode.tom ?: return
        grunnlagListe.add(trygdetidMedArbeid(tom.toLocalDate()?.plusDays(1), null))
    }

    private fun addGrunnlagBetweenUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        var index = 0

        while (index < grunnlagListe.size - 1) {
            val grunnlagBefore: TrygdetidOpphold = grunnlagListe[index]
            val grunnlagAfter: TrygdetidOpphold = grunnlagListe[index + 1]
            val secondGrunnlagFom: LocalDate? = grunnlagAfter.periode.fom.toLocalDate()
            val firstGrunnlagTom: LocalDate? =
                grunnlagBefore.periode.tom?.toLocalDate() ?: secondGrunnlagFom?.minusDays(1)
            val dayAfterFirstGrunnlagTom: LocalDate? = firstGrunnlagTom?.plusDays(1)

            if (secondGrunnlagFom != dayAfterFirstGrunnlagTom) {
                // Add new TrygdetidsgrunnlagWithArbeid in the gap between trygdetidsgrunnlagWithArbeidBefore and trygdetidsgrunnlagWithArbeidAfter
                grunnlagListe.add(
                    index = index + 1,
                    element = trygdetidMedArbeid(
                        fom = dayAfterFirstGrunnlagTom,
                        tom = secondGrunnlagFom?.minusDays(1)
                    )
                )

                index++
            }

            index++
        }
    }

    private fun trygdetidMedArbeid(fom: LocalDate?, tom: LocalDate?) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(fom, tom),
            arbeidet = true
        )
}
