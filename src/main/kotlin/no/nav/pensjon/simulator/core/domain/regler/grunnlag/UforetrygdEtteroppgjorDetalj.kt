package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import java.util.*

// 2025-03-10
class UforetrygdEtteroppgjorDetalj {
    /**
     * Angir gyldighetsperioden for detaljen. Avgrenset av Uføreperioden og året som etteroppgjørsgrunnlaget gjelder for.
     */
    var fomDato: Date? = null

    /**
     * Liste over inntektsfratrekk for etteroppgjøret (for perioder uten uføretrygd og andre registrerte fradrag).
     */
    var fratrekk: List<Inntektsgrunnlag> = ArrayList()

    /**
     * Kode som angir hvilken rolle personen har på kravet. De ulike rollene er definert i Kodeverk, ark K_GRNL_ROLLE_T.
     */
    var grunnlagsrolleEnum: GrunnlagsrolleEnum? = null

    /**
     * Liste over alle inntekter som skal benyttes i etteroppgjøret.
     */
    var inntekter: List<Inntektsgrunnlag> = ArrayList()

    /**
     * Angir gyldighetsperioden for detaljen. Avgrenset av Uføreperioden og året som etteroppgjørsgrunnlaget gjelder for.
     */
    var tomDato: Date? = null

    constructor()

    constructor(source: UforetrygdEtteroppgjorDetalj) : this() {
        grunnlagsrolleEnum = source.grunnlagsrolleEnum
        inntekter = source.inntekter.map(::Inntektsgrunnlag)
        fratrekk = source.fratrekk.map(::Inntektsgrunnlag)
        fomDato = source.fomDato?.time?.let(::Date)
        tomDato = source.tomDato?.time?.let(::Date)
    }
}
