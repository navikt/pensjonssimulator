package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.IBeregning
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningGjelderTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningMetodeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatTypeCti
import no.nav.pensjon.simulator.core.domain.regler.util.FindBeregningVisitor
import java.io.Serializable
import java.util.*

@JsonSubTypes(
    JsonSubTypes.Type(value = Uforetrygdberegning::class),
    JsonSubTypes.Type(value = AfpPrivatBeregning::class),
    JsonSubTypes.Type(value = AldersberegningKapittel20::class),
    JsonSubTypes.Type(value = AldersberegningKapittel19::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class Beregning2011 : IBeregning, Serializable {
    var gjelderPerson: PenPerson? = null
    var grunnbelop: Int = 0
    var tt_anv: Int = 0
    var resultatType: ResultatTypeCti? = null
    var beregningsMetode: BeregningMetodeTypeCti? = null
    var beregningType: BeregningTypeCti? = null
    override val delberegningsListe: MutableList<BeregningRelasjon>
        get() = delberegning2011Liste
    var delberegning2011Liste: MutableList<BeregningRelasjon> = mutableListOf()

    var merknadListe: MutableList<Merknad> = mutableListOf()

    /**
     * Feltet støtter navngivning av beregningen i beregningstreet.
     * Ifbm. Gjenlevendetillegg settes koden avhengig av hvilke grunnlagsroller som beregningen angår.
     */
    var beregningGjelderType: BeregningGjelderTypeCti? = null

    @JsonIgnore
    override var beregningsnavn: String = resultatType?.kode ?: "Ukjentnavn"

    /**
     * Referanse tilbake til beregningsrelasjon dersom denne beregning inngår i en beregningsrelasjon.
     */
    @JsonIgnore
    var beregningsrelasjon: BeregningRelasjon? = null

    /**
     * Hvis beregningsrelasjon ikke inneholder beregning 1967 blir, delberegning1967 satt til null.
     * Delberegning1967 finnes fra før, blir derfor overskrevet/fjernet
     */
    var delberegning1967: BeregningRelasjon?
        get() = finnDelberegning1967()
        set(delberegning1967) {
            if (erBeregning1967(delberegning1967)) {
                val eksisterende = finnDelberegning1967()
                if (eksisterende != null) {
                    delberegningsListe.remove(eksisterende)
                }
                if (delberegning1967 != null) {
                    delberegningsListe.add(delberegning1967)
                    delberegning1967.parentBeregning2011 = this
                }
            } else {
                throw RuntimeException("delberegning1967 kan bare være av typen Beregning")
            }
        }

    constructor()

    @JvmOverloads
    constructor(b: Beregning2011, kopierDelberegning2011Liste: Boolean = true) {
        this.grunnbelop = b.grunnbelop
        if (b.gjelderPerson != null) {
            gjelderPerson = PenPerson(b.gjelderPerson!!.penPersonId)
        }
        tt_anv = b.tt_anv
        if (b.resultatType != null) {
            resultatType = ResultatTypeCti(b.resultatType)
        }
        if (b.beregningsMetode != null) {
            beregningsMetode = BeregningMetodeTypeCti(b.beregningsMetode)
        }
        if (b.beregningType != null) {
            beregningType = BeregningTypeCti(b.beregningType)
        }
        if (kopierDelberegning2011Liste) {
            for (r in b.delberegningsListe) {
                delberegningsListe.add(BeregningRelasjon(r))
            }
        }
        beregningsnavn = b.beregningsnavn
        for (m in b.merknadListe) {
            merknadListe.add(Merknad(m))
        }
        if (b.beregningGjelderType != null) {
            this.beregningGjelderType = BeregningGjelderTypeCti(b.beregningGjelderType)
        }
    }

    constructor(
        gjelderPerson: PenPerson? = null,
        grunnbelop: Int = 0,
        tt_anv: Int = 0,
        resultatType: ResultatTypeCti? = null,
        beregningsMetode: BeregningMetodeTypeCti? = null,
        beregningType: BeregningTypeCti? = null,
        delberegning2011Liste: MutableList<BeregningRelasjon> = mutableListOf(),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        beregningGjelderType: BeregningGjelderTypeCti? = null,
        beregningsnavn: String = "Ukjentnavn",
        beregningsrelasjon: BeregningRelasjon? = null,
        delberegning1967: BeregningRelasjon?
    ) {
        this.gjelderPerson = gjelderPerson
        this.grunnbelop = grunnbelop
        this.tt_anv = tt_anv
        this.resultatType = resultatType
        this.beregningsMetode = beregningsMetode
        this.beregningType = beregningType
        this.delberegning2011Liste = delberegning2011Liste
        this.merknadListe = merknadListe
        this.beregningGjelderType = beregningGjelderType
        this.beregningsnavn = beregningsnavn
        this.beregningsrelasjon = beregningsrelasjon
        this.delberegning1967 = delberegning1967
    }

    fun accept(findBeregningVisitor: FindBeregningVisitor): Optional<IBeregning> {
        findBeregningVisitor.visit(this)
        return findBeregningVisitor.result
    }

    fun addBeregning2011Relasjon(berRel: BeregningRelasjon?) {
        if (berRel != null) {
            delberegningsListe.add(berRel)
        }
    }

    fun addMerknad(m: Merknad?) {
        if (m != null) {
            merknadListe.add(m)
        }
    }

    /**
     * Legger til beregningRelasjon til lista. BeregningsId blir satt på den respektive beregningen.
     *
     * @param beregningRelasjon
     * @param beregningId
     */
    fun addBeregningRelasjon(beregningRelasjon: BeregningRelasjon?, beregningId: String?) {
        if (beregningId != null && beregningRelasjon != null) {
            val eksisterende = getBeregningRelasjon(beregningId)
            if (eksisterende != null) {
                // Fjern eksisterende
                delberegningsListe.remove(eksisterende)
            }
            if (beregningRelasjon.beregning != null) {
                beregningRelasjon.beregning!!.beregningsnavn = beregningId
            }
            if (beregningRelasjon.beregning2011 != null) {
                beregningRelasjon.beregning2011!!.beregningsnavn = beregningId
            }
            beregningRelasjon.parentBeregning2011 = this
            delberegningsListe.add(beregningRelasjon)
        }
    }

    fun addBeregning(beregning2011: Beregning2011, beregningId: String) {
        beregning2011.beregningsnavn = beregningId
        val br = BeregningRelasjon()
        br.beregning2011 = beregning2011
        addBeregningRelasjon(br, beregningId)
    }

    /**
     * Hent BeregningsRelasjon som tilhører en bestemt beregning
     *
     * @param beregningId
     */
    fun getBeregningRelasjon(beregningId: String): BeregningRelasjon? {
        for (br in delberegningsListe) {
            if (br.beregning != null && beregningId == br.beregning!!.beregningsnavn) {
                return br
            }
            if (br.beregning2011 != null && beregningId == br.beregning2011!!.beregningsnavn) {
                return br
            }
        }
        return null
    }

    /**
     * Sjekker om beregningRelasjon er gammel beregning (1967)
     * Hvis BeregningRelasjon eller Beregning er null returneres true.
     *
     * @param toCheck
     * @return beregning1967
     */
    private fun erBeregning1967(toCheck: BeregningRelasjon?): Boolean {
        return if (toCheck != null) {
            toCheck.beregning2011 == null
        } else true
    }

    private fun finnDelberegning1967(): BeregningRelasjon? {
        for (br in delberegningsListe) {
            if (br.beregning != null) {
                return br
            }
        }
        return null
    }
}
