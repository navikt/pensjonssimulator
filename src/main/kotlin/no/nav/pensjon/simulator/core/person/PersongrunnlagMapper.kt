package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.core.beholdning.BeholdningUtil.SISTE_GYLDIGE_OPPTJENING_AAR
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.PersongrunnlagMapper
@Component
class PersongrunnlagMapper(
    private val generelleDataHolder: GenerelleDataHolder,
    private val time: Time
) {
    fun mapToPersongrunnlag(person: PenPerson, spec: SimuleringSpec) =
        createPersongrunnlag(
            person = person,
            personDetalj = createPersonDetalj(spec),
            utlandAntallAar = spec.utlandAntallAar,
            erFolketrygdMedlem = true,
            erFlyktning = spec.flyktning
        ).also {
            it.over60ArKanIkkeForsorgesSelv = false
            it.sisteGyldigeOpptjeningsAr = SISTE_GYLDIGE_OPPTJENING_AAR
            it.inngangOgEksportGrunnlag = InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
        }

    fun mapToEpsPersongrunnlag(sivilstatus: SivilstatusType, foedselsdato: LocalDate) =
        Persongrunnlag().apply {
            gjelderOmsorg = false
            gjelderUforetrygd = false
            penPerson = PenPerson(EPS_PERSON_ID)
            fodselsdato = foedselsdato.toNorwegianDateAtNoon()
            antallArUtland = 0
            dodsdato = null
            statsborgerskapEnum = norge
            flyktning = false
            bosattLandEnum = norge
            afpHistorikkListe = mutableListOf()
            uforeHistorikk = null
            generellHistorikk = null
            personDetaljListe.add(mapToEpsPersonDetalj(sivilstatus, foedselsdato))
        }.also { it.finishInit() }

    // PersongrunnlagMapper.mapToPersongrunnlagAvdod
    fun avdoedPersongrunnlag(avdoed: Avdoed, avdoedPerson: PenPerson, soekerPid: Pid?): Persongrunnlag =
        createPersongrunnlag(
            person = avdoedPerson,
            personDetalj = avdoedPersonDetalj(soekerPid),
            utlandAntallAar = avdoed.antallAarUtenlands,
            erFolketrygdMedlem = avdoed.erMedlemAvFolketrygden,
            erFlyktning = false
        ).apply {
            dodsdato = avdoed.doedDato.toNorwegianDateAtNoon()
            dodAvYrkesskade = false
            arligPGIMinst1G = avdoed.harInntektOver1G
        }

    // PersongrunnlagMapper.createPersonDetaljAvdod
    private fun avdoedPersonDetalj(soekerPid: Pid?) =
        PersonDetalj().apply {
            grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
            grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            penRolleFom = soekerPid?.let {
                generelleDataHolder.getPerson(it).foedselDato.toNorwegianDateAtNoon()
            }
            borMedEnum = null
            bruk = true
        }.also {
            it.finishInit()
        }

    // PersongrunnlagMapper.createPersonDetalj
    private fun createPersonDetalj(spec: SimuleringSpec) =
        PersonDetalj().apply {
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            penRolleFom = rolleFom(spec)?.toNorwegianDateAtNoon()
            sivilstandTypeEnum = mapToSivilstand(spec)
            bruk = true
            grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
        }.also {
            it.finishInit()
        }

    private fun createPersongrunnlag(
        person: PenPerson,
        personDetalj: PersonDetalj,
        utlandAntallAar: Int,
        erFolketrygdMedlem: Boolean?,
        erFlyktning: Boolean?
    ) =
        Persongrunnlag().apply {
            gjelderOmsorg = false
            gjelderUforetrygd = false
            penPerson = person
            fodselsdato = person.foedselsdato?.toNorwegianDateAtNoon()
            statsborgerskapEnum = person.pid?.let { generelleDataHolder.getPerson(it).statsborgerskap }
            bosattLandEnum = norge
            afpHistorikkListe = person.afpHistorikkListe ?: mutableListOf()
            uforeHistorikk = person.uforehistorikk
            generellHistorikk = person.generellHistorikk
            personDetaljListe.add(personDetalj)
            medlemIFolketrygdenSiste3Ar = erFolketrygdMedlem
            antallArUtland = utlandAntallAar
            flyktning = erFlyktning
        }.also {
            it.finishInit()
        }

    private fun rolleFom(spec: SimuleringSpec): LocalDate? =
        when (spec.type) {
            SimuleringType.AFP_FPP -> spec.foersteUttakDato?.let(::sisteDagForrigeMaaned)
            else -> time.today()
        }

    private companion object {
        private const val EPS_PERSON_ID = -2L
        private val norge = LandkodeEnum.NOR

        private val simuleringTyperForGjenlevende =
            EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN)

        private fun mapToSivilstand(spec: SimuleringSpec): SivilstandEnum {
            if (simuleringTyperForGjenlevende.contains(spec.type)) {
                return SivilstandEnum.ENKE
            }

            return when (spec.sivilstatus) {
                SivilstatusType.GIFT -> SivilstandEnum.GIFT
                SivilstatusType.REPA -> SivilstandEnum.REPA
                else -> SivilstandEnum.UGIF
            }
        }

        // PersongrunnlagMapper.mapToPersonDetaljEPS
        private fun mapToEpsPersonDetalj(sivilstatus: SivilstatusType, foedselsdato: LocalDate?) =
            PersonDetalj().apply {
                grunnlagsrolleEnum = mapToEpsGrunnlagRolle(sivilstatus)
                penRolleFom = foedselsdato?.toNorwegianDateAtNoon()
                borMedEnum = mapToEpsBorMedType(sivilstatus)
                bruk = true
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            }.also {
                it.finishInit()
            }

        private fun mapToEpsGrunnlagRolle(sivilstatus: SivilstatusType): GrunnlagsrolleEnum? =
            when (sivilstatus) {
                SivilstatusType.SAMB -> GrunnlagsrolleEnum.SAMBO
                SivilstatusType.GIFT -> GrunnlagsrolleEnum.EKTEF
                SivilstatusType.REPA -> GrunnlagsrolleEnum.PARTNER
                else -> null
            }

        // PersongrunnlagMapper.mapToBorMedTypeEPS
        private fun mapToEpsBorMedType(sivilstatus: SivilstatusType): BorMedTypeEnum? =
            when (sivilstatus) {
                SivilstatusType.SAMB -> BorMedTypeEnum.SAMBOER1_5
                SivilstatusType.GIFT -> BorMedTypeEnum.J_EKTEF
                SivilstatusType.REPA -> BorMedTypeEnum.J_PARTNER
                else -> null
            }

        private fun sisteDagForrigeMaaned(dato: LocalDate): LocalDate =
            dato.withDayOfMonth(1).minusDays(1)
    }
}
