package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.core.domain.regler.enum.MinstepensjonstypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultatKildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultattypeEnum
import java.time.LocalDate

/**
 * Corresponds to no.nav.pensjon.pen.domain.api.beregning.Beregning in PEN and PSELV
 */
@JsonInclude(NON_NULL)
data class NavBeregningResultV2 (
    val merknadliste: List<MerknadV2> = emptyList(),
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkDatoFom: LocalDate? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkDatoTom: LocalDate? = null,
    val brutto: Int? = 0,
    val netto: Int? = 0,
    val g: Int? = 0,
    val resultatKilde: ResultatKildeEnum? = null,
    val resultatType: ResultattypeEnum? = null,
    val afpPensjonsgrad: Int? = 0,
    val ytelseskomponenter: List<NavYtelseKomponentV2> = emptyList(),
    val delberegningListe: List<BeregningRelasjonV2> = emptyList(),
    val minstepensjonType: MinstepensjonstypeEnum? = null,
    val ttAnv: Int? = 0,
    val yug: Int? = 0,
    val ufg: Int? = 0
    // Not used in PSELV:
    // beregningId
    // versjon
    // benyttetSivilstand
    // beregningsResultatId
    // brukersSivilstand
    // gjelderPerson
    // bruttoMnd
    // nettoMnd
    // harKontrollpunkter
    // beregningType
    // endringArsak
    // p67beregning
    // gradert
    // friinntekt
    // fribelop
    // totalVinner
    // belopOktFraForrigePeriode
    // belopRedusertFraForrigePeriode
    // hjemmelsEndringForrigePeriode
    // ektefelleMottarPensjon
    // inngaarIBeregningListe
    // uforeEkstra
    // trygdetid
    // sakId
    // kravId
    // vedtak
    // vilkarsprovResultat
    // ektefelleInntektOver2g
    // beregnetFremtidigInntekt
    // redusertPgaInstOpphold
    // instOppholdType
    // inntektBruktIAvkorting
    // brukOpptjeningFra65I66Aret
    // beregningsMetode
    // belopRedusert
    // belopOkt
    // hjemmelsendring
    // beregningArsak
    // datoForGyldigSats
    // gpKapittel3
    // gpInst
    // lonnsvekstInformasjon
    // pubReguleringFratrekk
    // ungUforGarantiFrafalt
    // uft
    // yst
    // ttBeregnetForGrunnlagsrolle
    // gpAfpPensjonsregulert
    // minstepensjonArsak
)
