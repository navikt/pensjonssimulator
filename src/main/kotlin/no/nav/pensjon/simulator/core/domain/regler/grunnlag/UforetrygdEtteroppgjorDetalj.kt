package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagsrolleCti
import no.nav.pensjon.simulator.core.domain.regler.util.Copyable
import java.util.*

class UforetrygdEtteroppgjorDetalj(
    /**
     * Angir gyldighetsperioden for detaljen. Avgrenset av uføreperioden og året som etteroppgjørsgrunnlaget gjelder for.
     */
    var fomDato: Date? = null,

    /**
     * Liste over inntektsfratrekk for etteroppgjøret (for perioder uten uføretrygd og andre registrerte fradrag).
     */
    var fratrekk: MutableList<Inntektsgrunnlag> = mutableListOf(),

    /**
     * Kode som angir hvilken rolle personen har på kravet. De ulike rollene er definert i Kodeverk, ark K_GRNL_ROLLE_T.
     */
    var grunnlagsrolle: GrunnlagsrolleCti? = null,

    /**
     * Liste over alle inntekter som skal benyttes i etteroppgjøret.
     */
    var inntekter: MutableList<Inntektsgrunnlag> = mutableListOf(),

    /**
     * Angir gyldighetsperioden for detaljen. Avgrenset av uføreperioden og året som etteroppgjørsgrunnlaget gjelder for.
     */
    var tomDato: Date? = null
) : Copyable<UforetrygdEtteroppgjorDetalj> {

    constructor(uforetrygdEtteroppgjorDetalj: UforetrygdEtteroppgjorDetalj) : this() {
        if (uforetrygdEtteroppgjorDetalj.grunnlagsrolle != null) {
            this.grunnlagsrolle = GrunnlagsrolleCti(uforetrygdEtteroppgjorDetalj.grunnlagsrolle)
        }
        this.inntekter = uforetrygdEtteroppgjorDetalj.inntekter.map { Inntektsgrunnlag(it) }.toMutableList()
        this.fratrekk = uforetrygdEtteroppgjorDetalj.fratrekk.map { Inntektsgrunnlag(it) }.toMutableList()

        if (uforetrygdEtteroppgjorDetalj.fomDato != null) {
            this.fomDato = Date(uforetrygdEtteroppgjorDetalj.fomDato!!.time)
        }
        if (uforetrygdEtteroppgjorDetalj.tomDato != null) {
            this.tomDato = Date(uforetrygdEtteroppgjorDetalj.tomDato!!.time)
        }
    }

    override fun deepCopy(): UforetrygdEtteroppgjorDetalj {
        return UforetrygdEtteroppgjorDetalj(this)
    }
}
