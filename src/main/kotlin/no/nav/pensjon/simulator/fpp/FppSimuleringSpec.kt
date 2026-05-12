package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.api.nav.v2.acl.spec.UtlandSpecDto
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class FppSimuleringSpec(
    val simuleringstype: SimuleringTypeEnum,
    val uttaksdato: LocalDate,
    val personopplysninger: Personopplysninger,
    val barneopplysninger: Barneopplysninger?,
    val opptjeningFolketrygden: OpptjeningFolketrygden?
)

class Personopplysninger {
    var ident: String? = null
    var fodselsdato: LocalDate? = null
    var valgtAfpOrdning: AFPtypeEnum? = null
    var flyktning: Boolean? = null
    var antAarIUtlandet: Int? = null
    var utenlandsopphold: List<UtlandSpecDto>? = null
    var forventetArbeidsinntekt: Int? = null
    var forventetArbeidsinntektGjenlevende: Long? = null
    var inntektMndForAfp: Int? = null
    var erUnderUtdanning: Boolean? = null // for barnepensjon (PEN: isUnderUtdanning)
    var epsData: EpsData? = null
    var avdodList: List<AvdoedData> = emptyList()
}

class AvdoedData {
    var datoForDodsfall: LocalDate? = null
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
    var valgtSivilstatus: SivilstatusType? = null
    var registrertSivilstatus: SivilstandEnum? = null
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
    var maksUforegrad: Int? = null
    var registrertePensjonspoeng: Double? = null
}

class FppPerson {
    var pid: Pid? = null
    var personUtland: PersonUtland? = null
}

class Relasjon {
    var relasjonsType: RelasjonTypeCode? = null
    var fom: LocalDate? = null
    var person: FppPerson? = null
}
