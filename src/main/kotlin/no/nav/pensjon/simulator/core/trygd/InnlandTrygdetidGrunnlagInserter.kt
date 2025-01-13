package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.trygd.TrygdetidGrunnlagFactory.trygdetidPeriode
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.InsertTrygdetidsgrunnlagForDomesticPeriods
object InnlandTrygdetidGrunnlagInserter {

    fun createTrygdetidGrunnlagForInnlandPerioder(
        trygdetidGrunnlagListe: List<TrygdetidOpphold>,
        foedselsdato: LocalDate?
    ): List<TrygdetidOpphold> {
        if (trygdetidGrunnlagListe.isEmpty()) return emptyList()

        val sortedGrunnlagListe = trygdetidGrunnlagListe.sortedBy { it.periode.fom }.toMutableList()
        addGrunnlagFraFoedselsdato(sortedGrunnlagListe, foedselsdato)
        addGrunnlagAfterLastUtlandTrygdetidGrunnlag(sortedGrunnlagListe)
        addGrunnlagBetweenUtlandTrygdetidGrunnlag(sortedGrunnlagListe)
        return sortedGrunnlagListe
    }

    private fun addGrunnlagFraFoedselsdato(grunnlagListe: MutableList<TrygdetidOpphold>, foedselsdato: LocalDate?) {
        val fom: LocalDate? = grunnlagListe.firstOrNull()?.periode?.fom?.toNorwegianLocalDate()
        if (fom == foedselsdato) return

        grunnlagListe.add(
            index = 0,
            element = trygdetidMedArbeid(
                fom = foedselsdato,
                tom = fom?.minusDays(1)
            )
        )
    }

    private fun addGrunnlagAfterLastUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        val tom = grunnlagListe[grunnlagListe.size - 1].periode.tom ?: return
        grunnlagListe.add(trygdetidMedArbeid(tom.toNorwegianLocalDate().plusDays(1), null))
    }

    private fun addGrunnlagBetweenUtlandTrygdetidGrunnlag(grunnlagListe: MutableList<TrygdetidOpphold>) {
        var index = 0

        while (index < grunnlagListe.size - 1) {
            val grunnlagBefore: TrygdetidOpphold = grunnlagListe[index]
            val grunnlagAfter: TrygdetidOpphold = grunnlagListe[index + 1]
            val secondGrunnlagFom: LocalDate? = grunnlagAfter.periode.fom?.toNorwegianLocalDate()
            val firstGrunnlagTom: LocalDate? =
                grunnlagBefore.periode.tom?.toNorwegianLocalDate() ?: secondGrunnlagFom?.minusDays(1)
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
