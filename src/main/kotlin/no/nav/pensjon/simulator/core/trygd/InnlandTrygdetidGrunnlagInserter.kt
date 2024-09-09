package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.newTrygdetidPeriode
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
        val fom = grunnlagListe[0].periode.fom
        if (isSameDay(foedselDato, fom)) return

        grunnlagListe.add(0, trygdetidMedArbeid(foedselDato, fom.toLocalDate()!!.minusDays(1)))
    }

    private fun addGrunnlagAfterLastUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        val tom = grunnlagListe[grunnlagListe.size - 1].periode.tom ?: return
        grunnlagListe.add(trygdetidMedArbeid(tom.toLocalDate()?.plusDays(1), null))
    }

    private fun addGrunnlagBetweenUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        var index = 0

        while (index < grunnlagListe.size - 1) {
            val grunnlagBefore = grunnlagListe[index]
            val grunnlagAfter = grunnlagListe[index + 1]
            val firstGrunnlagTom = grunnlagBefore.periode.tom
            val secondGrunnlagFom = grunnlagAfter.periode.fom
            val dayAfterFirstGrunnlagTom: LocalDate? = firstGrunnlagTom.toLocalDate()?.plusDays(1)

            if (!isSameDay(secondGrunnlagFom, fromLocalDate(dayAfterFirstGrunnlagTom))) {
                // Add new TrygdetidsgrunnlagWithArbeid in the gap between trygdetidsgrunnlagWithArbeidBefore and trygdetidsgrunnlagWithArbeidAfter
                grunnlagListe.add(
                    index + 1,
                    trygdetidMedArbeid(
                        dayAfterFirstGrunnlagTom,
                        secondGrunnlagFom.toLocalDate()?.minusDays(1)
                    )
                )
                index++
            }

            index++
        }
    }

    private fun trygdetidMedArbeid(fom: LocalDate?, tom: LocalDate?) =
        TrygdetidOpphold(
            periode = newTrygdetidPeriode(fom, tom),
            arbeidet = true
        )
}
