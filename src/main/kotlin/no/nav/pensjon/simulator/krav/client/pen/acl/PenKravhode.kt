package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravVelgtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.KravVelgTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.person.Pid
import java.util.*

/**
 * Kravhode DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.krav.Kravhode in PEN.
 */
class PenKravhode {
    var kravId: Long? = null
    var kravFremsattDato: Date? = null
    var onsketVirkningsdato: Date? = null
    var gjelder: KravGjelder? = null // PEN: KravGjelderCode
    var sakId: Long? = null
    var sakType: SakType? = null // PEN: SakTypeCode
    var sakPenPersonFnr: Pid? = null
    var sakForsteVirkningsdatoListe: List<PenFoersteVirkningDato> = emptyList()
    var persongrunnlagListe: MutableList<PenPersongrunnlag> = mutableListOf()
    var kravlinjeListe: MutableList<PenKravlinje> = mutableListOf()
    var afpOrdning: AfpOrdningTypeCti? = null
    var afpOrdningEnum: AFPtypeEnum? = null
    var afptillegg = false
    var brukOpptjeningFra65I66Aret = false
    var kravVelgType: KravVelgTypeCti? = null
    var kravVelgTypeEnum: KravVelgtypeEnum? = null
    var boddEllerArbeidetIUtlandet = false
    var boddArbeidUtlandFar = false
    var boddArbeidUtlandMor = false
    var boddArbeidUtlandAvdod = false
    var uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf()
    var regelverkTypeCti: RegelverkTypeCti? = null
    var regelverkTypeEnum: RegelverkTypeEnum? = null
    var sisteSakstypeForAP: SakTypeCti? = null
    var sisteSakstypeForAPEnum: SakTypeEnum? = null
    var epsMottarPensjon = false
    var btVurderingsperiodeBenyttet = false
    // overstyrendeP_satsGP ikke i PEN
}
