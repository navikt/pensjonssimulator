package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.JustertPeriodeEnum
import java.time.LocalDate

// 2026-04-23
/**
 * Objektet inneholder informasjon om perioder der person har institusjonsopphold som kan medføre reduksjon av pensjon.
 */
class InstOpphReduksjonsperiode {
    /**
     * Unik identifikasjon av objektet.
     */
    var instOpphReduksjonsperiodeId: Long = 0

    /**
     * Fra og med dato
     */
    var fomLd: LocalDate? = null

    /**
     * Til og med dato
     */
    var tomLd: LocalDate? = null

    /**
     * Angir om reduksjon er grunnet varighet.
     */
    var reduksjonGrunnetVarighet = false

    /**
     * Angir om institusjonsoppholdsperioden medfører en økning eller reduksjon av pensjonsytelsen.
     */
    var justertPeriodeTypeEnum: JustertPeriodeEnum? = null

    /**
     * Angir om bruker har forsørgeransvar ved institusjonsopphold
     */
    var forsorgeransvar = false

    constructor()

    constructor(source: InstOpphReduksjonsperiode) : this() {
        instOpphReduksjonsperiodeId = source.instOpphReduksjonsperiodeId
        fomLd = source.fomLd
        tomLd = source.tomLd
        reduksjonGrunnetVarighet = source.reduksjonGrunnetVarighet
        justertPeriodeTypeEnum =source.justertPeriodeTypeEnum
        forsorgeransvar = source.forsorgeransvar
    }
}
