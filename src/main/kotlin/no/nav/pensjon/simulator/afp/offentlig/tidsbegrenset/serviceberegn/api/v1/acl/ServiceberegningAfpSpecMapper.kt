package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.*
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import no.nav.pensjon.simulator.validity.BadSpecException

object ServiceberegningAfpSpecMapper {

    fun fromDto(source: ServiceberegningAfpSpecDto) =
        TidsbegrensetAfpSpec(
            uttakFom = source.uttakFom,
            personopplysninger = personopplysninger(source.personopplysninger),
            opptjeningListe = source.opptjeningListe.map(::opptjening),
        )

    private fun personopplysninger(source: PersonopplysningerDto) =
        PersonSpec(
            pid = source.pid?.let(::Pid) ?: throw BadSpecException("udefinert person-ident i personopplysninger"),
            foedselsdato = source.foedselsdato,
            angittAfpOrdning = source.angittAfpOrdning?.internalValue,
            flyktning = source.flyktning,
            antallAarUtenlands = source.antallAarUtenlands,
            utenlandsoppholdListe = source.utenlandsoppholdListe.orEmpty().map(::utenlandsopphold),
            forventetArbeidsinntekt = source.forventetArbeidsinntekt,
            inntektMaanedenFoerAfp = source.inntektMaanedenFoerAfp,
            eps = source.eps?.let(::eps)
        )

    private fun eps(source: EpsDataDto) =
        EpsSpec(
            relasjon = relasjon(source.relasjon),
            angittSivilstatus = source.angittSivilstatus?.internalValue,
            registrertSivilstand = source.registrertSivilstand?.internalValue,
            mottarPensjon = source.mottarPensjon,
            harInntektOver1G = source.harInntektOver1G,
            harInntektOver2G = source.harInntektOver2G,
            tidligereGiftEllerBarnMedSamboer = source.tidligereGiftEllerBarnMedSamboer
        )

    private fun opptjening(source: OpptjeningFolketrygdenDataDto) =
        FolketrygdOpptjeningSpec(
            aar = source.aar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt,
            omsorgspoeng = source.omsorgspoeng,
            registrertePensjonspoeng = source.registrertePensjonspoeng,
            maxUfoeregrad = source.maxUfoeregrad
        )

    private fun person(source: StatsborgerDto) =
        StatsborgerSpec(
            pid = source.pid?.let(::Pid) ?: throw BadSpecException("udefinert person-ident for statsborger"),
            statsborgerskap = LandkodeEnum.entries.firstOrNull { it.name == source.statsborgerskap?.name }
        )

    private fun relasjon(source: RelasjonDto) =
        RelasjonSpec(
            fom = source.fom,
            person = source.person?.let(::person)
        )

    private fun utenlandsopphold(source: UtlandSpecDto) =
        UtlandPeriode(
            fom = source.fom,
            tom = source.tom,
            land = LandkodeEnum.entries.firstOrNull { it.name == source.land.name }
                ?: throw BadSpecException("udefinert landkode for utenlandsopphold"),
            arbeidet = source.arbeidetUtenlands
        )
}