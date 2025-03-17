package no.nav.pensjon.simulator.core.endring

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isFirstDayOfMonth
import no.nav.pensjon.simulator.core.spec.SimuleringSpec

/**
 * Validates the request (specification) for 'simuler endring av alderspensjon'.
 * Corresponds to SimulerEndringAvAPCommand (validation part) in PEN
 */
object EndringValidator {

    // SimulerEndringAvAPCommand.validateRequest
    // -> AbstraktSimulerAPFra2011Command.validateRequest + SimulerEndringAvAPCommandHelper.validateRequest
    fun validate(spec: SimuleringSpec) {
        val simuleringType = spec.type

        if (!spec.gjelderEndring()) {
            throw InvalidArgumentException("Invalid simuleringstype: $simuleringType")
        }

        /* Not relevant at this stage, but may be relevant closer to the API
        if (spec.forventetInntekt() == null && spec.fremtidigeInntekter() == null) {
            throw InvalidArgumentException("forventetInntekt must be set")
        }*/

        if (spec.foersteUttakDato == null || !isFirstDayOfMonth(spec.foersteUttakDato)) {
            throw InvalidArgumentException("forsteUttakDato must be set, and it must be the first day of the month")
        }

        if (spec.uttakGrad != UttakGradKode.P_100) {
            /*if (spec.inntektUnderGradertUttak == null && spec.fremtidigeInntekter == null) {
                throw InvalidArgumentException("When Uttaksgrad < 100 % then inntektUnderGradertUttak must be set")
            }*/

            if (spec.heltUttakDato == null || !isFirstDayOfMonth(spec.heltUttakDato)) {
                throw InvalidArgumentException("When uttaksgrad < 100% then heltUttakDato must be set, and it must be the first day of the month")
            }
        }

        /*if (spec.inntektEtterHeltUttak == null && spec.fremtidigeInntekter == null) {
            throw InvalidArgumentException("InntektEtterHeltUttak must be set")
        }

        if (spec.antallArInntektEtterHeltUttak == null && spec.fremtidigeInntekter == null) {
            throw InvalidArgumentException("AntallArInntektEtterHeltUttak must be set")
        }*/

        if (simuleringType == SimuleringType.ENDR_ALDER_M_GJEN) {
            if (spec.avdoed?.doedDato == null) {
                throw InvalidArgumentException("avdod.dodsdato must be set for simuleringstype $simuleringType")
            }
        }
    }

    // SimulerEndringAvAPCommand.validateRequestBasedOnLopendeYtelser
    fun validateRequestBasedOnLoependeYtelser(spec: SimuleringSpec, forrigeAlderspensjon: AbstraktBeregningsResultat?) {
        if (spec.type == SimuleringType.ENDR_ALDER_M_GJEN && !harAlderspensjonMedGjenlevenderett(forrigeAlderspensjon)) {
            validateBasedOnLoependeYtelser(spec)
        }
    }

    // SimulerEndringAvAPCommand.hasApWithGjenlevenderett
    private fun harAlderspensjonMedGjenlevenderett(resultat: AbstraktBeregningsResultat?): Boolean {
        return when (resultat) {
            is BeregningsResultatAlderspensjon2011 -> resultat.beregningsinformasjon?.harGjenlevenderett == true
            is BeregningsResultatAlderspensjon2016 -> resultat.beregningsResultat2011?.beregningsInformasjonKapittel19?.rettPaGjenlevenderett == true
            else -> false
        }
    }

    // SimulerEndringAvAPCommandHelper.validateRequestBasedOnLopendeYtelser
    private fun validateBasedOnLoependeYtelser(spec: SimuleringSpec) {
        val simuleringType = spec.type

        if (spec.avdoed?.pid == null) {
            throw InvalidArgumentException("avdoed.pid must be set for SimuleringType $simuleringType")
        }

        /* Not relevant at this stage, but may be relevant closer to the API
        if (spec.avdod.antallArUtenlands == null) {
            throw InvalidArgumentException("avdod.antallArUtenlands must be set for SimuleringType $simuleringType!")
        }

        if (spec.avdod.harInntektOver1G == null) {
            throw InvalidArgumentException("avdod.harInntektOver1G must be set for SimuleringType $simuleringType!")
        }

        if (spec.avdod.erMedlemAvFolketrygden == null) {
            throw InvalidArgumentException("avdod.erMedlemAvFolketrygden must be set for SimuleringType $simuleringType!")
        }

        if (spec.avdod.flyktning == null) {
            throw InvalidArgumentException("avdod.flyktning must be set for SimuleringType $simuleringType!")
        }
        */
    }
}
