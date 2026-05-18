package no.nav.pensjon.simulator.fpp.api.v1.acl

import java.time.LocalDate

// PEN: no.nav.pensjon.pen.domain.api.kalkulator.PensjonskalkulatorInput
data class FppSimuleringSpecDto(
    val simuleringstype: SimuleringstypeDto,
    val uttaksdato: LocalDate,
    val personopplysninger: PersonopplysningerDto,
    val barneopplysninger: BarneopplysningerDto?,
    val opptjeningFolketrygden: OpptjeningFolketrygdenDto?
)

class PersonopplysningerDto {
    var ident: String? = null
    var fodselsdato: LocalDate? = null
    var valgtAfpOrdning: AfpTypeDto? = null
    var flyktning: Boolean? = null
    var antAarIUtlandet: Int? = null
    var utenlandsopphold: List<UtlandSpecDto>? = null
    var forventetArbeidsinntekt: Int? = null
    var forventetArbeidsinntektGjenlevende: Long? = null
    var inntektMndForAfp: Int? = null
    var erUnderUtdanning: Boolean? = null // for barnepensjon
    var epsData: EpsDataDto? = null
    var avdodList: List<AvdoedDataDto> = emptyList()
}

class AvdoedDataDto {
    var datoForDodsfall: LocalDate? = null // PEN: Date
    var avdodAntAarIUtlandet: Int? = null
    var inntektPaaDodstidspunktHvisYrkesskade: Int? = null
    var avdodInntektMinst1G: Boolean? = null
    var avdodMedlemFolketrygden: Boolean? = null
    var avdodFlyktning: Boolean? = null
    var dodAvYrkesskade: Boolean? = null
    var relasjon: RelasjonDto? = null
}

class EpsDataDto {
    var eps: RelasjonDto? = null
    var valgtSivilstatus: SivilstatusDto? = null
    var registrertSivilstatus: SivilstandDto? = null
    var epsMottarPensjon: Boolean? = null
    var epsInntektOver2G: Boolean? = null
    var tidligereGiftEllerBarnMedSamboer: Boolean? = null
    var erEpsInntektOver1G: Boolean? = null
}

class BarneopplysningerDto {
    var barn: List<BarneopplysningerDataDto> = emptyList()
    var sosken: List<BarneopplysningerSoeskenDataDto> = emptyList()
}

class BarneopplysningerDataDto {
    var fnr: String? = null
    var borMedBeggeForeldre = false
    var erInntektOver1G = false
}

class BarneopplysningerSoeskenDataDto {
    var fnr: String? = null
    var underUtdanning: Boolean? = null
    var oppdrattSammen: Boolean? = null
    var helSosken: Boolean? = null
}

class OpptjeningFolketrygdenDto {
    var egenOpptjeningFolketrygden: List<OpptjeningFolketrygdenDataDto> = emptyList()
    var avdodesOpptjeningFolketrygden: List<OpptjeningFolketrygdenDataDto> = emptyList()
    var morsOpptjeningFolketrygden: List<OpptjeningFolketrygdenDataDto> = emptyList()
    var farsOpptjeningFolketrygden: List<OpptjeningFolketrygdenDataDto> = emptyList()
}

class OpptjeningFolketrygdenDataDto {
    var ar: Int? = null
    var pensjonsgivendeInntekt: Int? = null
    var omsorgspoeng: Double? = null
    var maksUforegrad: Int? = null // PEN: Int 0
    var registrertePensjonspoeng: Double? = null
}

// PEN: no.nav.domain.pensjon.common.person.Person
class PersonDto {
    var pid: String? = null
    var personUtland: PersonUtlandDto? = null
}

// PEN: no.nav.domain.pensjon.common.person.Relasjon
class RelasjonDto {
    var relasjonsType: RelasjonstypeDto? = null
    var fom: LocalDate? = null // PEN: Calendar
    var person: PersonDto? = null
}

// PEN: no.nav.domain.pensjon.common.person.PersonUtland
class PersonUtlandDto {
    var statsborgerskap: String? = null // LandkodeEnum
}

data class UtlandSpecDto(
    val fom: LocalDate,
    val tom: LocalDate?,
    val land: String,
    val arbeidetUtenlands: Boolean
)