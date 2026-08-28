package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

data class TidsbegrensetAfpSpec(
    val uttakFom: LocalDate,
    val personopplysninger: PersonSpec,
    val opptjeningListe: List<FolketrygdOpptjeningSpec>
)

data class PersonSpec(
    val pid: Pid,
    val foedselsdato: LocalDate? = null,
    val angittAfpOrdning: AFPtypeEnum? = null,
    val flyktning: Boolean? = null,
    val antallAarUtenlands: Int? = null,
    val utenlandsoppholdListe: List<UtlandPeriode>,
    val forventetArbeidsinntekt: Int? = null,
    val inntektMaanedenFoerAfp: Int? = null,
    val eps: EpsSpec? = null
)

data class EpsSpec(
    val relasjon: RelasjonSpec? = null,
    val angittSivilstatus: SivilstatusType? = null,
    val registrertSivilstand: SivilstandEnum? = null,
    val mottarPensjon: Boolean? = null,
    val harInntektOver1G: Boolean? = null,
    val harInntektOver2G: Boolean? = null,
    val tidligereGiftEllerBarnMedSamboer: Boolean? = null
)

data class FolketrygdOpptjeningSpec(
    val aar: Int? = null,
    val pensjonsgivendeInntekt: Int? = null,
    val omsorgspoeng: Double? = null,
    val registrertePensjonspoeng: Double? = null,
    val maxUfoeregrad: Int? = null
)

data class StatsborgerSpec(
    val pid: Pid,
    val statsborgerskap: LandkodeEnum? = null
)

data class RelasjonSpec(
    val fom: LocalDate? = null,
    val person: StatsborgerSpec? = null
)