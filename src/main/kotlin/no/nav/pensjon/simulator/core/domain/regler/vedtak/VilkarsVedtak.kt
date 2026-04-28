package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VilkarVurderingEnum
import no.nav.pensjon.simulator.vedtak.VilkaarsvedtakKravlinje
import java.time.LocalDate

// 2026-04-23
class VilkarsVedtak {
    /**
     * Kode som angir hvilket resultat pensjon-regler setter på vilkårsvedtaket. Se
     * K_VILKAR_RESUL_T for gyldige typer. Kun satt hvis pensjon-regler utfører
     * vilkårsprøving.
     */
    var anbefaltResultatEnum: VedtakResultatEnum? = null

    /**
     * Kode som angir det resultatet til vilkårsvedtaket. Settes av konsumenten
     * av tjenesten, eventuelt basert på anbefaltResultat. Se K_VILKAR_RESUL_T
     * for gyldige typer.
     */
    var vilkarsvedtakResultatEnum: VedtakResultatEnum? = null

    /**
     * Kode som angir hvilken type kravlinje vilkårsvedtaket relaterer seg til.
     * K_KRAVLINJE_T for gyldige typer.
     */
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    /**
     * Beskriver hvilken vurdering saksbehandler har lagt til grunn for
     * resultatvurderingen. Se K_VILKAR_VURD_T.
     */
    var anvendtVurderingEnum: VilkarVurderingEnum? = null

    /**
     * Dato vilkårsvedtaket har virkning fra.
     */
    var virkFomLd: LocalDate? = null

    /**
     * Dato vilkårsvedtaket har virkning til. Denne skal ikke være satt, og det
     * betyr at vedtaket har uendelig gyldighet. Skulle den likevel være satt
     * vil pensjon-regler sjekke at virken på ytelsen det beregnes for er nnnnenfor
     * virkFom-virkTom. Er den utenfor blir vedtaket behandlet som ikke gyldig.
     */
    var virkTomLd: LocalDate? = null

    /**
     * Dato Første innvilgede vilkårsvedtak personen har fått fra trygden.
     * Eks: har personen tidligere hatt UP og før nå AP vil det være datoen for Første UP-vedtaket.
     */
    var forsteVirkLd: LocalDate? = null

    /**
     * Dato dette vilkårsvedtakets kravlinje fårst ble innvilget.
     * Eks: personen fikk innvilget gjenlevenderett fom dette virkningstidspunkt.
     */
    var kravlinjeForsteVirkLd: LocalDate? = null

    /**
     * Kravlinje som er vilkårsprøvd.
     */
    @JsonIgnore
    var kravlinje: VilkaarsvedtakKravlinje? = null

    /**
     * Id for personen
     */
    var penPerson: PenPerson? = null

    var vilkarsprovresultat: AbstraktVilkarsprovResultat? = null
    var begrunnelseEnum: BegrunnelseTypeEnum? = null
    var avslattKapittel19 = false
    var avslattGarantipensjon = false
    var vurderSkattefritakET = false

    var unntakHalvMinstepensjon = false
    var epsRettEgenPensjon = false

    /**
     * List av Beregningsvilkarperioder
     */
    var beregningsvilkarPeriodeListe: List<BeregningsvilkarPeriode> = mutableListOf()

    /**
     * Liste av merknader - forklaringer,unntak og avvisningsgrunner fra
     * regelmotoren.
     */
    var merknadListe: List<Merknad> = mutableListOf()

    //--- Extra:
    @JsonIgnore
    var gjelderPerson: PenPerson? = null
    // end extra
}
