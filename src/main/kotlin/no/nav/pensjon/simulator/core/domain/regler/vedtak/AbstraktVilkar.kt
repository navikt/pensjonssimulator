package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.VilkarOppfyltUTEnum
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
    var resultatEnum: VilkarOppfyltUTEnum? = null

    constructor()

    protected constructor(source: AbstraktVilkar) {
        resultat = source.resultat?.let(::VilkarOppfyltUTCti)
    }

    protected constructor(resultat: VilkarOppfyltUTCti?) {
        this.resultat = resultat
    }

    abstract fun dypKopi(source: AbstraktVilkar): AbstraktVilkar?
}

