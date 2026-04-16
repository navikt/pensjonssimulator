package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.VilkaarsvedtakKravlinje
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelser
import no.nav.pensjon.simulator.ytelse.InformasjonOmAvdoed
import no.nav.pensjon.simulator.ytelse.LoependeYtelserResult
import no.nav.pensjon.simulator.ytelse.PrivatAfpYtelser

/**
 * Maps løpende ytelser from DTO (data transfer object) to pensjonssimulator domain.
 * The DTO is a hybrid of PEN and pensjon-regler properties.
 * This basically performs the inverse mapping of ReglerLoependeYtelserMapper in PEN.
 */
object PenLoependeYtelserResultMapper {

    fun fromDto(source: PenLoependeYtelserResult) =
        LoependeYtelserResult(
            alderspensjon = source.alderspensjon?.let(::alderspensjonYtelser),
            afpPrivat = source.afpPrivat?.let(::privatAfpYtelser)
        )

    private fun alderspensjonYtelser(source: PenAlderspensjonYtelser) =
        AlderspensjonYtelser(
            sokerVirkningFom = source.sokerVirkningFom,
            sisteBeregning = source.sisteBeregning,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat,
            forrigeVilkarsvedtakListe = source.forrigeVilkarsvedtakListe.orEmpty().map(::vilkaarsvedtak),
            avdoed = source.avdoed?.let(::avdoedYtelser)
        )

    private fun vilkaarsvedtak(source: PenVilkaarsvedtak) =
        VilkarsVedtak().apply {
            anbefaltResultatEnum = source.anbefaltResultatEnum
            vilkarsvedtakResultatEnum = source.vilkarsvedtakResultatEnum
            kravlinjeTypeEnum = source.kravlinjeTypeEnum
            anvendtVurderingEnum = source.anvendtVurderingEnum
            virkFom = source.virkFom
            virkTom = source.virkTom
            forsteVirk = source.forsteVirk
            kravlinjeForsteVirk = source.kravlinjeForsteVirk
            kravlinje = source.kravlinje?.let(::kravlinje)
            penPerson = source.penPerson
            vilkarsprovresultat = source.vilkarsprovresultat
            begrunnelseEnum = source.begrunnelseEnum
            avslattKapittel19 = source.avslattKapittel19
            avslattGarantipensjon = source.avslattGarantipensjon
            vurderSkattefritakET = source.vurderSkattefritakET
            unntakHalvMinstepensjon = source.unntakHalvMinstepensjon
            epsRettEgenPensjon = source.epsRettEgenPensjon
            beregningsvilkarPeriodeListe = source.beregningsvilkarPeriodeListe
            merknadListe = source.merknadListe
        }

    private fun kravlinje(source: Kravlinje) =
        VilkaarsvedtakKravlinje(
            type = source.kravlinjeTypeEnum!!,
            person = source.relatertPerson,
            status = source.kravlinjeStatus,
            land = source.land
        )

    private fun privatAfpYtelser(source: PenPrivatAfpYtelser) =
        PrivatAfpYtelser(
            virkningFom = source.virkningFom,
            forrigeBeregningsresultat = source.forrigeBeregningsresultat
        ).apply {
            this.forrigeBeregningsresultat?.let { it.virkFomLd = it.virkFom?.toNorwegianLocalDate() }
        }

    private fun avdoedYtelser(source: PenInformasjonOmAvdoed) =
        InformasjonOmAvdoed(
            pid = source.pid?.let(::Pid),
            doedsdato = source.doedsdato,
            foersteVirkningsdato = source.foersteVirkningsdato,
            aarligPensjonsgivendeInntektErMinst1G = source.aarligPensjonsgivendeInntektErMinst1G,
            harTilstrekkeligMedlemskapIFolketrygden = source.harTilstrekkeligMedlemskapIFolketrygden,
            antallAarUtenlands = source.antallAarUtenlands,
            erFlyktning = source.erFlyktning
        )
}
