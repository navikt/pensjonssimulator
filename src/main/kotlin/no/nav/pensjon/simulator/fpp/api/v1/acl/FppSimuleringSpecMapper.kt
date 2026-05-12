package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.fpp.*
import no.nav.pensjon.simulator.person.Pid

object FppSimuleringSpecMapper {

    fun fromDto(source: FppSimuleringSpecDto) =
        FppSimuleringSpec(
            simuleringstype = source.simuleringstype.internalValue,
            uttaksdato = source.uttaksdato,
            personopplysninger = personopplysninger(source.personopplysninger),
            barneopplysninger = source.barneopplysninger?.let(::barneopplysninger),
            opptjeningFolketrygden = source.opptjeningFolketrygden?.let(::opptjeningFolketrygden),
        )

    private fun personopplysninger(source: PersonopplysningerDto) =
        Personopplysninger().apply {
            ident = source.ident
            fodselsdato = source.fodselsdato
            valgtAfpOrdning = source.valgtAfpOrdning?.internalValue
            flyktning = source.flyktning
            antAarIUtlandet = source.antAarIUtlandet
            utenlandsopphold = source.utenlandsopphold
            forventetArbeidsinntekt = source.forventetArbeidsinntekt
            forventetArbeidsinntektGjenlevende = source.forventetArbeidsinntektGjenlevende
            inntektMndForAfp = source.inntektMndForAfp
            erUnderUtdanning = source.erUnderUtdanning
            epsData = source.epsData?.let(::epsData)
            avdodList = source.avdodList.map(::avdoedData)
        }

    private fun avdoedData(source: AvdoedDataDto) =
        AvdoedData().apply {
            datoForDodsfall = source.datoForDodsfall
            avdodAntAarIUtlandet = source.avdodAntAarIUtlandet
            inntektPaaDodstidspunktHvisYrkesskade = source.inntektPaaDodstidspunktHvisYrkesskade
            avdodInntektMinst1G = source.avdodInntektMinst1G
            avdodMedlemFolketrygden = source.avdodMedlemFolketrygden
            avdodFlyktning = source.avdodFlyktning
            dodAvYrkesskade = source.dodAvYrkesskade
            relasjon = source.relasjon?.let(::relasjon)
        }

    private fun epsData(source: EpsDataDto) =
        EpsData().apply {
            eps = source.eps?.let(::relasjon)
            valgtSivilstatus = source.valgtSivilstatus?.internalValue
            registrertSivilstatus = source.registrertSivilstatus?.internalValue
            epsMottarPensjon = source.epsMottarPensjon
            epsInntektOver2G = source.epsInntektOver2G
            tidligereGiftEllerBarnMedSamboer = source.tidligereGiftEllerBarnMedSamboer
            erEpsInntektOver1G = source.erEpsInntektOver1G
        }

    private fun barneopplysninger(source: BarneopplysningerDto) =
        Barneopplysninger().apply {
            barn = source.barn.map(::barneopplysningerData)
            sosken = source.sosken.map(::barneopplysningerSoeskenData)
        }

    private fun barneopplysningerData(source: BarneopplysningerDataDto) =
        BarneopplysningerData().apply {
            fnr = source.fnr
            borMedBeggeForeldre = source.borMedBeggeForeldre
            erInntektOver1G = source.erInntektOver1G
        }

    private fun barneopplysningerSoeskenData(source: BarneopplysningerSoeskenDataDto) =
        BarneopplysningerSoeskenData().apply {
            fnr = source.fnr
            underUtdanning = source.underUtdanning
            oppdrattSammen = source.oppdrattSammen
            helSosken = source.helSosken
        }

    private fun opptjeningFolketrygden(source: OpptjeningFolketrygdenDto) =
        OpptjeningFolketrygden().apply {
            egenOpptjeningFolketrygden = source.egenOpptjeningFolketrygden.map(::opptjeningFolketrygdenData)
            avdodesOpptjeningFolketrygden = source.avdodesOpptjeningFolketrygden.map(::opptjeningFolketrygdenData)
            morsOpptjeningFolketrygden = source.morsOpptjeningFolketrygden.map(::opptjeningFolketrygdenData)
            farsOpptjeningFolketrygden = source.farsOpptjeningFolketrygden.map(::opptjeningFolketrygdenData)
        }

    private fun opptjeningFolketrygdenData(source: OpptjeningFolketrygdenDataDto) =
        OpptjeningFolketrygdenData().apply {
            ar = source.ar
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt
            omsorgspoeng = source.omsorgspoeng
            maksUforegrad = source.maksUforegrad
            registrertePensjonspoeng = source.registrertePensjonspoeng
        }

    private fun person(source: PersonDto) =
        FppPerson().apply {
            pid = source.pid?.let(::Pid)
            personUtland = source.personUtland?.let(::personUtland)
        }

    private fun personUtland(source: PersonUtlandDto) =
        PersonUtland().apply {
            statsborgerskap = LandkodeEnum.entries.firstOrNull { it.name == source.statsborgerskap }
        }

    private fun relasjon(source: RelasjonDto) =
        Relasjon().apply {
            relasjonsType = source.relasjonsType?.internalValue
            fom = source.fom
            person = source.person?.let(::person)
        }
}