package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode
import java.time.LocalDate

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.InsertTrygdetidsgrunnlagForDomesticPeriods
object InnlandTrygdetidUtil {

    fun addInnenlandsopphold(
        oppholdListe: List<TrygdetidOpphold>,
        foedselsdato: LocalDate?
    ): List<TrygdetidOpphold> {
        if (oppholdListe.isEmpty()) return emptyList()

        with(oppholdListe.sortedBy { it.periode.fom }.toMutableList()) {
            addGrunnlagFraFoedselsdato(this, foedselsdato)
            addGrunnlagAfterLastUtlandTrygdetidsgrunnlag(this)
            addGrunnlagBetweenUtlandTrygdetidsgrunnlag(this)
            return this
        }
    }

    // PEN: SettTrygdetidHelper.createTrygdetidsgrunnlagNorge
    fun norskTrygdetidPeriode(fom: LocalDate, tom: LocalDate?, ikkeProRata: Boolean) =
        trygdetidPeriode(
            fom = fom.toNorwegianDateAtNoon(),
            tom = tom?.toNorwegianDateAtNoon(),
            land = LandkodeEnum.NOR,
            ikkeProRata,
            bruk = null // bruk is not set in SettTrygdetidHelper.createTrygdetidsgrunnlagNorge in PEN
        )

    private fun addGrunnlagFraFoedselsdato(oppholdListe: MutableList<TrygdetidOpphold>, foedselsdato: LocalDate?) {
        val fom: LocalDate? = oppholdListe.firstOrNull()?.periode?.fom?.toNorwegianLocalDate()
        if (fom == foedselsdato) return

        oppholdListe.add(
            index = 0,
            element = trygdetidMedArbeid(
                fom = foedselsdato,
                tom = fom?.minusDays(1)
            )
        )
    }

    // PEN: createTrygdetidsgrunnlagAfterLastUtlandTrygdetidsgrunnlag
    private fun addGrunnlagAfterLastUtlandTrygdetidsgrunnlag(oppholdListe: MutableList<TrygdetidOpphold>) {
        val tom = oppholdListe[oppholdListe.size - 1].periode.tom ?: return
        oppholdListe.add(trygdetidMedArbeid(tom.toNorwegianLocalDate().plusDays(1), null))
    }

    private fun addGrunnlagBetweenUtlandTrygdetidsgrunnlag(oppholdListe: MutableList<TrygdetidOpphold>) {
        var index = 0

        while (index < oppholdListe.size - 1) {
            val grunnlagBefore: TrygdetidOpphold = oppholdListe[index]
            val grunnlagAfter: TrygdetidOpphold = oppholdListe[index + 1]
            val secondGrunnlagFom: LocalDate? = grunnlagAfter.periode.fom?.toNorwegianLocalDate()
            val firstGrunnlagTom: LocalDate? =
                grunnlagBefore.periode.tom?.toNorwegianLocalDate() ?: secondGrunnlagFom?.minusDays(1)
            val dayAfterFirstGrunnlagTom: LocalDate? = firstGrunnlagTom?.plusDays(1)

            if (secondGrunnlagFom != dayAfterFirstGrunnlagTom) {
                // Add new TrygdetidsgrunnlagWithArbeid in the gap between trygdetidsgrunnlagWithArbeidBefore and trygdetidsgrunnlagWithArbeidAfter
                oppholdListe.add(
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