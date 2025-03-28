package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

@JsonSubTypes(
    JsonSubTypes.Type(value = Alderspensjon2011VedDod::class),
    JsonSubTypes.Type(value = TrygdetidBeregningsvilkar::class),
    JsonSubTypes.Type(value = TidligereGjenlevendePensjon::class),
    JsonSubTypes.Type(value = Yrkesskadegrad::class),
    JsonSubTypes.Type(value = InntektVedSkadetidspunktet::class),
    JsonSubTypes.Type(value = InntektEtterUforhet::class),
    JsonSubTypes.Type(value = Skadetidspunkt::class),
    JsonSubTypes.Type(value = Uforetidspunkt::class),
    JsonSubTypes.Type(value = InntektForUforhet::class),
    JsonSubTypes.Type(value = Uforegrad::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBeregningsvilkar {
    var merknadListe: MutableList<Merknad> = mutableListOf()

    constructor()

    protected constructor(abstraktBeregningsvilkar: AbstraktBeregningsvilkar) {
        merknadListe = abstraktBeregningsvilkar.merknadListe.map { it.copy() }.toMutableList()
    }

    constructor(merknadListe: MutableList<Merknad> = mutableListOf()) {
        for (merknad in merknadListe) {
            this.merknadListe.add(merknad)
        }
    }

    /**
     * Metoden, når implementert i en klasse som implementerer abstraktberegningsvilkar skal returnere en kopi av input hvis den er av den implementerende klassen.
     * Dette gjør kopiering av lister av abstrakte beregningsvilkår lettere, og gjør at koden ikke kompilerer hvis implementasjon mangler.
     * Tidligere ble lister av abstrakte beregningsvilkår kopiert i BeregningsvilkarPeriode ved å sjekke om et gitt vilkår var av en gitt implementerende klasse,
     * for deretter å bruke denne klassens kopikonstruktør for å lage en kopi. Dette var imidlertid vanskelig å vedlikeholde, da man må oppdatere listekopieringen
     * i beregningsvilkarperiode for nye AbstraktBeregningsvilkar.
     *
     * @param abstraktBeregningsvilkar det abstrakte vilkaret som skal kopieres.
     * @return En dyp kopi av dette objektet hvis det er av samme klasse, ellers null.
     */
    abstract fun dypKopi(abstraktBeregningsvilkar: AbstraktBeregningsvilkar): AbstraktBeregningsvilkar?

}
