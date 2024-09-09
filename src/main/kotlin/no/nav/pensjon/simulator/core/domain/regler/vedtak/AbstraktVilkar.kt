package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsvilkarPeriode::class),
    JsonSubTypes.Type(value = MedlemskapForUTEtterTrygdeavtaler::class),
    JsonSubTypes.Type(value = RettTilEksportEtterTrygdeavtaler::class),
    JsonSubTypes.Type(value = RettTilGjenlevendetillegg::class),
    JsonSubTypes.Type(value = ForutgaendeMedlemskap::class),
    JsonSubTypes.Type(value = Yrkesskade::class),
    JsonSubTypes.Type(value = FortsattMedlemskap::class),
    JsonSubTypes.Type(value = UngUfor::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktVilkar {
    var resultat: VilkarOppfyltUTCti? = null

    constructor()

    protected constructor(abstraktVilkar: AbstraktVilkar) {
        if (abstraktVilkar.resultat != null) {
            resultat = VilkarOppfyltUTCti(abstraktVilkar.resultat)
        }
    }

    protected constructor(resultat: VilkarOppfyltUTCti?) {
        this.resultat = resultat
    }

    /**
     * Metoden, når implementert i en klasse som implementerer abstraktvilkar skal returnere en kopi av input hvis den er av den implementerende klassen.
     * Dette gjør kopiering av lister av abstrakte vilkår lettere, og gjør at koden ikke kompilerer hvis implementasjon mangler.
     * Tidligere ble lister av abstrakte vilkår kopiert i BeregningsvilkarPeriode ved å sjekke om et gitt vilkår var av en gitt implementerende klasse,
     * for deretter å bruke denne klassens kopikonstruktør for å lage en kopi. Dette var imidlertid vanskelig å vedlikeholde, da man må oppdatere listekopieringen
     * i beregningsvilkarperiode for nye AbstrakteVilkar.
     *
     * @param abstraktVilkar det abstrakte vilkaret som skal kopieres.
     * @return En dyp kopi av dette objektet hvis det er av samme klasse, ellers null.
     */
    abstract fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar?
}
