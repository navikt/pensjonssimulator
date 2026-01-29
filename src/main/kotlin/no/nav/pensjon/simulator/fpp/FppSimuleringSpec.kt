package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.fpp.api.acl.v1.RelasjonTypeCodeV1
import no.nav.pensjon.simulator.fpp.api.acl.v1.SimuleringTypeV1
import java.time.LocalDate

// PEN: no.nav.pensjon.pen.domain.api.kalkulator.PensjonskalkulatorInput
data class FppSimuleringSpec(
    val simuleringstype: SimuleringTypeV1,
    val uttaksdato: LocalDate,
    val personopplysninger: Personopplysninger,
    val barneopplysninger: Barneopplysninger?,
    val opptjeningFolketrygden: OpptjeningFolketrygden?
)

class Personopplysninger {
    var ident: String? = null
    var fodselsdato: LocalDate? = null
    var valgtAfpOrdning: AFPtypeEnum? = null // PEN: String
    var flyktning: Boolean? = null
    var antAarIUtlandet: Int? = null
    var forventetArbeidsinntekt: Int? = null
    var forventetArbeidsinntektGjenlevende: Long? = null
    var inntektMndForAfp: Int? = null
    var erUnderUtdanning: Boolean? = null // for barnepensjon (PEN: isUnderUtdanning)
    var epsData: EpsData? = null
    var avdodList: List<AvdoedData> = emptyList()
}

class AvdoedData {
    var datoForDodsfall: LocalDate? = null // PEN: Date
    var avdodAntAarIUtlandet: Int? = null
    var inntektPaaDodstidspunktHvisYrkesskade: Int? = null
    var avdodInntektMinst1G: Boolean? = null
    var avdodMedlemFolketrygden: Boolean? = null
    var avdodFlyktning: Boolean? = null
    var dodAvYrkesskade: Boolean? = null
    var relasjon: Relasjon? = null
}

class EpsData {
    var eps: Relasjon? = null
    var valgtSivilstatus: SivilstatusType? = null // PEN: String
    var registrertSivilstatus: SivilstandEnum? = null // PEN: String
    var epsMottarPensjon: Boolean? = null
    var epsInntektOver2G: Boolean? = null
    var tidligereGiftEllerBarnMedSamboer: Boolean? = null
    var erEpsInntektOver1G: Boolean? = null
}

class Barneopplysninger {
    var barn: List<BarneopplysningerData> = emptyList()
    var sosken: List<BarneopplysningerSoeskenData> = emptyList()
}

class BarneopplysningerData {
    var fnr: String? = null
    var borMedBeggeForeldre = false
    var erInntektOver1G = false
}

class BarneopplysningerSoeskenData {
    var fnr: String? = null
    var underUtdanning: Boolean? = null
    var oppdrattSammen: Boolean? = null
    var helSosken: Boolean? = null
}

class OpptjeningFolketrygden {
    var egenOpptjeningFolketrygden: List<OpptjeningFolketrygdenData> = emptyList()
    var avdodesOpptjeningFolketrygden: List<OpptjeningFolketrygdenData> = emptyList()
    var morsOpptjeningFolketrygden: List<OpptjeningFolketrygdenData> = emptyList()
    var farsOpptjeningFolketrygden: List<OpptjeningFolketrygdenData> = emptyList()
}

class OpptjeningFolketrygdenData {
    var ar: Int? = null
    var pensjonsgivendeInntekt: Int? = null
    var omsorgspoeng: Double? = null
    var maksUforegrad: Int? = null // PEN: Int 0
    var registrertePensjonspoeng: Double? = null
}

// PEN: no.nav.domain.pensjon.common.person.Person
class PersonV1 {
    var pid: String? = null // PEN: Pid
    var personUtland: PersonUtland? = null
}

// PEN: no.nav.domain.pensjon.common.person.Relasjon
class Relasjon {
    var relasjonsType: RelasjonTypeCodeV1? = null // PEN: String
    var fom: LocalDate? = null // PEN: Calendar
    var person: PersonV1? = null
}
