package no.nav.pensjon.simulator.core.inntekt

import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.math.BigInteger

object InntektUtil {

    /**
     * Gitt en liste med årsinntekter summerer denne funksjonen de periodiserte inntektene.
     * Forutsetninger:
     * - Inntektene i listen gjelder for samme år (alle f.o.m.-datoene har samme år).
     * - Beløpene i listen er for et helt år (1.1.-31.12.).
     * - Inntekt i en del av en måned regnes som full månedsinntekt
     *   (dvs. dag-verdien i f.o.m.-datoene anses som 1 uansett hva som er angitt).
     * ---------------------------------------------------------------------------
     * Eksempel med tre inntekter i listen:
     *    fom1     fom2    fom3     31.12.
     *     |   m1   |   m2   |   m3   |
     *     |   b1   |   b2   |   b3   |
     *  Forklaring:
     *  - fomX er inntektenes angitte f.o.m.-dato
     *  - mX er antall måneder mellom to f.o.m.-datoer (delvis måned regnes som hel måned)
     *  - bX er angitt beløp (hva inntekten vil være om den opptjenes gjennom et helt år)
     *  Inntektsum = (m1 x b1 + m2 x b2 + m3 x b3) / 12
     */
    // PEN: OpprettKravHodeHelper.createArliginntekt
    fun faktiskAarligInntekt(inntekterInnenSammeAar: List<FremtidigInntekt>): BigInteger {
        val iterator = inntekterInnenSammeAar.listIterator()
        if (iterator.hasNext().not()) return BigInteger.ZERO

        var inntekt = iterator.next()
        var nextInntekt: FremtidigInntekt
        var aarligInntekt = BigInteger.ZERO

        while (iterator.hasNext()) {
            nextInntekt = iterator.next()
            val antallManeder = nextInntekt.fom.monthValue - inntekt.fom.monthValue
            aarligInntekt = aarligInntekt.add(periodevisInntekt(inntekt, antallManeder))
            inntekt = nextInntekt
        }

        val sistePeriodeAntallMaaneder = MAANEDER_PER_AAR - inntekt.fom.monthValue + 1
        return aarligInntekt.add(periodevisInntekt(inntekt, sistePeriodeAntallMaaneder))
    }

    // Extracted from PEN: OpprettKravHodeHelper.createArliginntekt
    private fun periodevisInntekt(inntekt: FremtidigInntekt, antallMaaneder: Int) =
        BigInteger.valueOf(inntekt.aarligInntektBeloep.toLong())
            .multiply(BigInteger.valueOf(antallMaaneder.toLong()))
            .divide(BigInteger.valueOf(MAANEDER_PER_AAR.toLong()))
}
