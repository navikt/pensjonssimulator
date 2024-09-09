package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleDatoCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleKritCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import java.io.Serializable
import java.util.*

class Trygdeavtale(
    /**
     * Hvilket land personen bor i. Se /2/, arkfane K_AVTALELAND_T
     */
    var bostedsland: LandCti? = null,
    /**
     * Hva slags type avtale som er inngått. Se /2/, arkfane K_AVTALE_T.
     */
    var avtaleType: AvtaleTypeCti? = null,
    /**
     * Felt for å registrere når avtalen tro i kraft, hvis avtaletypen tilsier at
     * landet har flere mulige avtaler med Norge. Se /2/, arkfane K_AVTALE_DATO.
     */
    var avtaledato: AvtaleDatoCti? = null,
    /**
     * Felt for å registrere kriterier som er oppfylt for å omfattes av trygdeavtalen.
     * Se /2/, arkfane K_AVTALE_KRIT_T.
     */
    var avtaleKriterie: AvtaleKritCti? = null,
    /**
     * Angir om personen omfattes av avtalens personkrets
     */
    var omfattesavAvtalensPersonkrets: Boolean = false,
    /**
     * Dato for kravdato i avtale
     */
    var kravDatoIAvtaleland: Date? = null
) : Serializable {
    constructor(trygdeavtale: Trygdeavtale) : this() {
        if (trygdeavtale.bostedsland != null) {
            this.bostedsland = LandCti(trygdeavtale.bostedsland)
        }
        if (trygdeavtale.avtaleType != null) {
            this.avtaleType = AvtaleTypeCti(trygdeavtale.avtaleType)
        }
        if (trygdeavtale.avtaledato != null) {
            this.avtaledato = AvtaleDatoCti(trygdeavtale.avtaledato)
        }
        if (trygdeavtale.avtaleKriterie != null) {
            this.avtaleKriterie = AvtaleKritCti(trygdeavtale.avtaleKriterie)
        }
        omfattesavAvtalensPersonkrets = trygdeavtale.omfattesavAvtalensPersonkrets
        if (trygdeavtale.kravDatoIAvtaleland != null) {
            this.kravDatoIAvtaleland = trygdeavtale.kravDatoIAvtaleland!!.clone() as Date
        }
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("Trygdeavtale ( ").append(super.toString()).append(TAB).append("bostedsland = ")
            .append(bostedsland).append(TAB).append("avtaleType = ").append(avtaleType)
            .append(TAB).append("avtaledato = ").append(avtaledato).append(TAB).append("avtaleKriterie = ")
            .append(avtaleKriterie).append(TAB)
            .append("omfattesavAvtalensPersonkrets = ").append(omfattesavAvtalensPersonkrets).append(TAB)
            .append("kravDatoIAvtaleland = ").append(kravDatoIAvtaleland)
            .append(TAB).append(" )")

        return retValue.toString()
    }
}
