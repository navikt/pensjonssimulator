package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.YtelseTypeCti
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.io.Serializable

class InfoPavirkendeYtelse(

        /**
         * Liste av alle vilkårsvedtak for hovedytelse som EPS har løpende.
         */
        var vilkarsvedtakEPSListe: MutableList<VilkarsVedtak> = mutableListOf(),
        /**
         * EPS uforegrad dersom EPS har uførepensjon.
         */
        var uforegradEPS: Int = 0,

        /**
         * Hvis vilkarsvedtakEPSListen er tom og det finnes en tjenestepensjon for ektefellen som
         * ikke blir beregnet av PESYS skal denne fylles ut.
         */
        var tjenestepensjonsordningEps: YtelseTypeCti? = null

) : Serializable {

    constructor(vilkarsvedtakEPSListe: MutableList<VilkarsVedtak>, uforegradEPS: Int) : this() {
        this.vilkarsvedtakEPSListe = vilkarsvedtakEPSListe
        this.uforegradEPS = uforegradEPS
    }

    constructor(obj: InfoPavirkendeYtelse) : this() {
        uforegradEPS = obj.uforegradEPS
        if (obj.tjenestepensjonsordningEps != null) {
            tjenestepensjonsordningEps = YtelseTypeCti(obj.tjenestepensjonsordningEps)
        }
        for (v in obj.vilkarsvedtakEPSListe) {
            vilkarsvedtakEPSListe.add(VilkarsVedtak(v))
        }
    }

}
