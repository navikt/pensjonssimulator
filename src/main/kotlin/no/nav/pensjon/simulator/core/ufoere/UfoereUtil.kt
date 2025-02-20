package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import kotlin.collections.filter
import kotlin.collections.maxByOrNull
import kotlin.collections.orEmpty

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
        val ufoereFomAar = periode.ufgFom!!.toNorwegianLocalDate().year
        val ufoereTomAar = periode.ufgTom?.toNorwegianLocalDate()?.year

        return (ufoereFomAar == aar
                || ufoereFomAar < aar && ufoereTomAar == null
                || ufoereTomAar != null && aar <= ufoereTomAar
                )
    }
}
