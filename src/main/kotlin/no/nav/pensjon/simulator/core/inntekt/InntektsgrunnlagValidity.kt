package no.nav.pensjon.simulator.core.inntekt

import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.dateIsValid
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate
import java.util.function.Predicate

class InntektsgrunnlagValidity(
    private val sakType: SakType?,
    private val virkDatoFom: LocalDate?,
    private val virkDatoTom: LocalDate?,
    private val periodiserFomTomDatoUtenUnntak: Boolean
) : Predicate<Inntektsgrunnlag> {

    override fun test(inntektsgrunnlag: Inntektsgrunnlag): Boolean =
        inntektsgrunnlagIsValid(
            sakType,
            inntektsgrunnlag,
            virkDatoFom,
            virkDatoTom,
            periodiserFomTomDatoUtenUnntak
        )

    private companion object {
        private val INNTEKT_IS_RELEVANT_BEFORE_DATE = LocalDate.of(1968, 1, 1)

        private fun inntektsgrunnlagIsValid(
            sakType: SakType?,
            grunnlag: Inntektsgrunnlag,
            virkningFom: LocalDate?,
            virkningTom: LocalDate?,
            periodiserFomTomDatoUtenUnntak: Boolean
        ): Boolean {
            if (grunnlag.bruk != true) { // i.e. bruk = false or null
                return false
            }

            // If periodiserFomTomDatoUtenUnntak is true, only fom/tom dates shall be considered.
            if (periodiserFomTomDatoUtenUnntak) {
                return dateIsValid(grunnlag.fom, grunnlag.tom, virkningFom, virkningTom)
            }

            if (sakType == SakType.AFP || dateIsValid(grunnlag.fom, grunnlag.tom, virkningFom, virkningTom)) {
                return true
            }

            // ... or if they are type PGI and have been in use in this year and the two years before

            if (InntekttypeEnum.PGI == grunnlag.inntektTypeEnum && intersectsWithPossiblyOpenEndings(
                    o1Start = virkningFom?.year?.let { foersteDag(it - OPPTJENING_ETTERSLEP_ANTALL_AAR) },
                    o1End = virkningTom?.let { sisteDag(it.year) },
                    o2Start = grunnlag.fom,
                    o2End = grunnlag.tom,
                    considerContactByDayAsIntersection = true
                )
            ) {
                return true
            }

            return isValidInntektType(grunnlag.inntektTypeEnum) && isValidInntektFom(grunnlag.fom?.toNorwegianLocalDate())
        }

        private fun isValidInntektFom(fom: LocalDate?): Boolean =
            fom?.isBefore(INNTEKT_IS_RELEVANT_BEFORE_DATE) == true

        private fun isValidInntektType(type: InntekttypeEnum?) =
            InntekttypeEnum.ARBLIGN == type || InntekttypeEnum.AI == type
    }
}
