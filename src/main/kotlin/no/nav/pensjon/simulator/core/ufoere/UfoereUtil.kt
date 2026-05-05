package no.nav.pensjon.simulator.core.ufoere
/*
This may be used in SimulatorCore in the future

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException

object UfoereUtil {

    private const val MAX_SUM_AV_UFOEREGRAD_OG_UTTAKSGRAD = 100 // prosent

    fun validateUfoeregrad(person: PenPerson, spec: SimuleringSpec) {
        val ufoeregrad = maxUfoeregrad(person, spec.foersteUttakDato!!.year) ?: 0
        val uttaksgrad = spec.uttakGrad.value.toInt()

        if (ufoeregrad + uttaksgrad > MAX_SUM_AV_UFOEREGRAD_OG_UTTAKSGRAD) {
            throw BadSpecException("Sum av uføregrad ($ufoeregrad %) og uttaksgrad ($uttaksgrad %) er mer enn $MAX_SUM_AV_UFOEREGRAD_OG_UTTAKSGRAD %")
        }
    }

    // Adapted from PEN BeregnPensjonsPoengCommand.setMaxUforeGradForOpptjeningTypePPI
    private fun maxUfoeregrad(person: PenPerson, aar: Int): Int? =
        person.uforehistorikk?.uforeperiodeListe.orEmpty()
            .filter { it.isRealUforeperiode() && gjelderForAar(it, aar) }
            .maxByOrNull { it.ufg }?.ufg

    private fun gjelderForAar(periode: Uforeperiode, aar: Int): Boolean {
        val ufoereFomAar = periode.ufgFomLd!!.year
        val ufoereTomAar = periode.ufgTomLd?.year

        return (ufoereFomAar == aar
                || ufoereFomAar < aar && ufoereTomAar == null
                || ufoereTomAar != null && aar <= ufoereTomAar
                )
    }
}
*/