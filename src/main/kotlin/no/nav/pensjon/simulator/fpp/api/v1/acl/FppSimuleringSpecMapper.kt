package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.*
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import no.nav.pensjon.simulator.validity.BadSpecException

object FppSimuleringSpecMapper {

    fun fromDto(source: FppSimuleringSpecDto) =
        TidsbegrensetAfpSpec(
            uttakFom = source.uttaksdato,
            personopplysninger = personopplysninger(source.personopplysninger),
            opptjeningListe = source.opptjeningFolketrygden?.egenOpptjeningFolketrygden.orEmpty()
                .map(::opptjeningFolketrygdenData)
        )

    private fun personopplysninger(source: PersonopplysningerDto) =
        PersonSpec(
            pid = source.ident?.let(::Pid) ?: throw BadSpecException("udefinert person-ident i personopplysninger"),
            foedselsdato = source.fodselsdato,
            angittAfpOrdning = source.valgtAfpOrdning?.internalValue,
            flyktning = source.flyktning,
            antallAarUtenlands = source.antAarIUtlandet,
            utenlandsoppholdListe = source.utenlandsopphold.orEmpty().map(::utlandPeriode),
            forventetArbeidsinntekt = source.forventetArbeidsinntekt,
            inntektMaanedenFoerAfp = source.inntektMndForAfp,
            eps = source.epsData?.let(::epsData),
        )

    private fun epsData(source: EpsDataDto) =
        EpsSpec(
            relasjon = source.eps?.let(::relasjon),
            angittSivilstatus = source.valgtSivilstatus?.internalValue,
            registrertSivilstand = source.registrertSivilstatus?.internalValue,
            mottarPensjon = source.epsMottarPensjon,
            harInntektOver1G = source.erEpsInntektOver1G,
            harInntektOver2G = source.epsInntektOver2G,
            tidligereGiftEllerBarnMedSamboer = source.tidligereGiftEllerBarnMedSamboer
        )

    private fun opptjeningFolketrygdenData(source: OpptjeningFolketrygdenDataDto) =
        FolketrygdOpptjeningSpec(
            aar = source.ar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt,
            omsorgspoeng = source.omsorgspoeng,
            registrertePensjonspoeng = source.registrertePensjonspoeng,
            maxUfoeregrad = source.maksUforegrad
        )

    private fun person(source: PersonDto) =
        StatsborgerSpec(
            pid = source.pid?.let(::Pid) ?: throw BadSpecException("udefinert person-ident for statsborger"),
            statsborgerskap = LandkodeEnum.entries.firstOrNull { it.name == source.personUtland?.statsborgerskap }
        )

    private fun relasjon(source: RelasjonDto) =
        RelasjonSpec(
            fom = source.fom,
            person = source.person?.let(::person)
        )

    private fun utlandPeriode(source: UtlandSpecDto) =
        UtlandPeriode(
            fom = source.fom,
            tom = source.tom,
            land = LandkodeEnum.valueOf(source.land),
            arbeidet = source.arbeidetUtenlands
        )
}