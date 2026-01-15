package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.trygdetid.TrygdetidGrunnlagFactory.trygdetidPeriode

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.TrygdetidsgrunnlagForUtenlandsperioderMapper
object UtlandPeriodeTrygdetidMapper {

    fun utlandTrygdetidsgrunnlag(periodeListe: MutableList<UtlandPeriode>): List<TrygdetidOpphold> {
        val oppholdListe = mutableListOf<TrygdetidOpphold>()
        val sortedList = periodeListe.sortedBy { it.fom }

        sortedList.forEachIndexed { index, periode ->
            if (index + 1 < periodeListe.size)
                oppholdListe.add(trygdetidsgrunnlag(periode, sortedList[index + 1]))
            else
                oppholdListe.add(trygdetidsgrunnlag(periode))
        }

        return oppholdListe
    }

    // PEN: createTrygdetidsgrunnlagForUtenlandsperioder
    fun utlandTrygdetidsgrunnlag(
        utlandPeriodeListe: MutableList<UtlandPeriode>,
        trygdetidsgrunnlagMedPensjonspoengListe: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> =
        merge(
            outerList = utlandPeriodeListe.map(::trygdetidsgrunnlag).sortedBy { it.periode.fom },
            innerList = trygdetidsgrunnlagMedPensjonspoengListe.sortedBy { it.periode.fom }
        )

    // PEN: extract from createTrygdetidsgrunnlagForUtenlandsperioder
    private fun merge(
        outerList: List<TrygdetidOpphold>,
        innerList: List<TrygdetidOpphold>
    ): List<TrygdetidOpphold> {
        val resultList: MutableList<TrygdetidOpphold> = mutableListOf()
        var inner: TrygdetidOpphold
        var outer: TrygdetidOpphold
        var innerIndex = 0

        outerLoop@ for (outerIndex in outerList.indices) {
            outer = outerList[outerIndex]

            innerLoop@ while (innerIndex < innerList.size) {
                inner = innerList[innerIndex]

                if (outer.endsBefore(inner)) {
                    break@innerLoop
                } else if (outer.startsBeforeAndEndsIn(inner)) {
                    outer.periode.tom = inner.dayBefore()
                    break@innerLoop
                } else if (outer.startsAndEndsIn(inner)) {
                    continue@outerLoop
                } else if (outer.startsBeforeAndEndsAfter(inner)) {
                    resultList.add(outer.withPeriodeTom(dato = inner.dayBefore()))
                    outer.periode.fom = inner.dayAfter()
                } else if (inner.endsBefore(outer)) {
                    // No action
                } else if (outer.startsInAndEndsAfter(inner)) {
                    outer.periode.fom = inner.dayAfter()
                }

                innerIndex++
            }

            resultList.add(outer)
        }

        resultList.addAll(innerList)
        return resultList.sortedBy { it.periode.fom }
    }

    private fun trygdetidsgrunnlag(periode: UtlandPeriode, nestePeriode: UtlandPeriode) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = periode.tom?.let { if (periode.tom == nestePeriode.fom) nestePeriode.fom.minusDays(1L) else periode.tom },
                land = periode.land,
                ikkeProRata = false,
                bruk = true
            ),
            arbeidet = periode.arbeidet
        )

    private fun trygdetidsgrunnlag(periode: UtlandPeriode) =
        TrygdetidOpphold(
            periode = trygdetidPeriode(
                fom = periode.fom,
                tom = periode.tom,
                land = periode.land,
                ikkeProRata = false,
                bruk = true
            ),
            arbeidet = periode.arbeidet
        )
}
