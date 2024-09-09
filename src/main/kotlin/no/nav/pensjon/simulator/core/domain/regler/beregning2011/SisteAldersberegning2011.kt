package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdninger
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.*
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.io.Serializable
import java.util.*

/**
 * Beregning på nytt regelverk
 * Denne benyttes av 2011, 2016 og 2025
 * For 2016 vil alle feltene være aktuelle
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = SisteAldersberegning2016::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class SisteAldersberegning2011 : SisteBeregning, Serializable {
    var regelverkType: RegelverkTypeCti? = null // Dene benyttes for å avgjøre om det er på 2011, 2016 eller 2025.
    var basispensjon: Basispensjon? = null // 2011
    var restpensjon: Basispensjon? = null // 2011
    var pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null
    var pensjonUnderUtbetaling2025AltKonv: PensjonUnderUtbetaling? =
        null // PensjonUnderUtbetaling for tapende konvensjon i siste aldersberegning.
    var beholdninger: Beholdninger? = null // 2025
    var beholdningerAltKonv: Beholdninger? = null // Beholdninger for tapende konvensjon i siste aldersberegning.
    var eps: Persongrunnlag? = null
    var epsMottarPensjon: Boolean = false
    var vilkarsvedtakEktefelletillegg: VilkarsVedtak? = null
    var avdodesPersongrunnlag: Persongrunnlag? = null
    var gjenlevenderettAnvendt: Boolean = false
    var anvendtGjenlevenderettVedtak: VilkarsVedtak? = null
    var beregningsMetode: BeregningMetodeTypeCti? = null
    var tt_anv_kap_20: Int = 0
    var prorataBrok_kap_20: Brok? = null
    var tt_anv_kap_20AltKonv = 0
    var prorataBrok_kap_20AltKonv: Brok? = null

    constructor() : super()

    constructor(sb: SisteAldersberegning2011) : super(sb) {
        if (sb.regelverkType != null) {
            regelverkType = RegelverkTypeCti(sb.regelverkType)
        }
        if (sb.basispensjon != null) {
            basispensjon = Basispensjon(sb.basispensjon!!)
        }
        if (sb.restpensjon != null) {
            restpensjon = Basispensjon(sb.restpensjon!!)
        }
        if (sb.pensjonUnderUtbetaling != null) {
            pensjonUnderUtbetaling = PensjonUnderUtbetaling(sb.pensjonUnderUtbetaling!!)
        }
        if (sb.pensjonUnderUtbetaling2025AltKonv != null) {
            pensjonUnderUtbetaling2025AltKonv = PensjonUnderUtbetaling(sb.pensjonUnderUtbetaling2025AltKonv!!)
        }
        if (sb.beholdninger != null) {
            beholdninger = Beholdninger(sb.beholdninger!!)
        }
        if (sb.beholdningerAltKonv != null) {
            beholdningerAltKonv = Beholdninger(sb.beholdningerAltKonv!!)
        }
        if (sb.eps != null) {
            eps = Persongrunnlag(sb.eps!!)
        }
        epsMottarPensjon = sb.epsMottarPensjon
        if (sb.vilkarsvedtakEktefelletillegg != null) {
            vilkarsvedtakEktefelletillegg = VilkarsVedtak(sb.vilkarsvedtakEktefelletillegg!!)
        }
        if (sb.avdodesPersongrunnlag != null) {
            avdodesPersongrunnlag = Persongrunnlag(sb.avdodesPersongrunnlag!!)
        }
        gjenlevenderettAnvendt = sb.gjenlevenderettAnvendt
        anvendtGjenlevenderettVedtak = sb.anvendtGjenlevenderettVedtak
        if (sb.beregningsMetode != null) {
            beregningsMetode = BeregningMetodeTypeCti(sb.beregningsMetode)
        }
        tt_anv_kap_20 = sb.tt_anv_kap_20
        tt_anv_kap_20AltKonv = sb.tt_anv_kap_20AltKonv
        if (sb.prorataBrok_kap_20 != null) {
            prorataBrok_kap_20 = Brok(sb.prorataBrok_kap_20!!)
        }
        if (sb.prorataBrok_kap_20AltKonv != null) {
            prorataBrok_kap_20AltKonv = Brok(sb.prorataBrok_kap_20AltKonv!!)
        }
    }

    constructor(
        regelverkType: RegelverkTypeCti? = null,
        basispensjon: Basispensjon? = null,
        restpensjon: Basispensjon? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
        beholdninger: Beholdninger? = null,
        eps: Persongrunnlag? = null,
        epsMottarPensjon: Boolean = false,
        vilkarsvedtakEktefelletillegg: VilkarsVedtak? = null,
        avdodesPersongrunnlag: Persongrunnlag? = null,
        gjenlevenderettAnvendt: Boolean = false,
        anvendtGjenlevenderettVedtak: VilkarsVedtak? = null,
        tt_anv_kap_20: Int = 0,
        /** super SisteBeregning */
        virkDato: Date? = null,
        tt_anv: Int = 0,
        resultatType: ResultatTypeCti? = null,
        sivilstandType: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        pensjonUnderUtbetaling2025AltKonv: PensjonUnderUtbetaling? = null,
        beholdningerAltKonv: Beholdninger? = null,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        prorataBrok_kap_20: Brok? = null,
        tt_anv_kap_20AltKonv: Int = 0,
        prorataBrok_kap_20AltKonv: Brok? = null
    ) : super(
        virkDato = virkDato,
        tt_anv = tt_anv,
        resultatType = resultatType,
        sivilstandType = sivilstandType,
        benyttetSivilstand = benyttetSivilstand
    ) {
        this.regelverkType = regelverkType
        this.basispensjon = basispensjon
        this.restpensjon = restpensjon
        this.pensjonUnderUtbetaling = pensjonUnderUtbetaling
        this.beholdninger = beholdninger
        this.eps = eps
        this.epsMottarPensjon = epsMottarPensjon
        this.vilkarsvedtakEktefelletillegg = vilkarsvedtakEktefelletillegg
        this.avdodesPersongrunnlag = avdodesPersongrunnlag
        this.gjenlevenderettAnvendt = gjenlevenderettAnvendt
        this.anvendtGjenlevenderettVedtak = anvendtGjenlevenderettVedtak
        this.tt_anv_kap_20 = tt_anv_kap_20
        this.pensjonUnderUtbetaling2025AltKonv = pensjonUnderUtbetaling2025AltKonv
        this.beholdningerAltKonv = beholdningerAltKonv
        this.beregningsMetode = beregningsMetode
        this.prorataBrok_kap_20 = prorataBrok_kap_20
        this.tt_anv_kap_20AltKonv = tt_anv_kap_20AltKonv
        this.prorataBrok_kap_20AltKonv = prorataBrok_kap_20AltKonv
    }
}
