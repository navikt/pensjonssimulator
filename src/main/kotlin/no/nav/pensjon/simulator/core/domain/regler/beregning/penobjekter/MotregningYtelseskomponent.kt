package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = ArbeidsavklaringspengerUT::class),
    JsonSubTypes.Type(value = Arbeidsavklaringspenger::class),
    JsonSubTypes.Type(value = Sykepenger::class),
    JsonSubTypes.Type(value = SykepengerUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class MotregningYtelseskomponent : BeregningYtelseskomponent {
    var dagsats: Int = 0
    var antallDager: Int = 0

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti()
    )

    constructor(motregningYtelseskomponent: MotregningYtelseskomponent) : super(motregningYtelseskomponent) {
        dagsats = motregningYtelseskomponent.dagsats
        antallDager = motregningYtelseskomponent.antallDager
    }

    constructor(
            dagsats: Int = 0,
            antallDager: Int = 0,
            /** super BeregningYtelseskomponent */
            ytelseKomponentTypeName: String? = null,
            beregning: Beregning? = null,
            /** super Ytelseskomponent */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti(),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = null,
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super BeregningYtelseskomponent */
            ytelseKomponentTypeName = ytelseKomponentTypeName,
            beregning = beregning,
            /** super Ytelseskomponent */
            brutto = brutto,
            netto = netto,
            fradrag = fradrag,
            bruttoPerAr = bruttoPerAr,
            nettoPerAr = nettoPerAr,
            fradragPerAr = fradragPerAr,
            ytelsekomponentType = ytelsekomponentType,
            merknadListe = merknadListe,
            fradragsTransaksjon = fradragsTransaksjon,
            opphort = opphort,
            sakType = sakType,
            formelKode = formelKode,
            reguleringsInformasjon = reguleringsInformasjon
    ) {
        this.dagsats = dagsats
        this.antallDager = antallDager
    }
}
