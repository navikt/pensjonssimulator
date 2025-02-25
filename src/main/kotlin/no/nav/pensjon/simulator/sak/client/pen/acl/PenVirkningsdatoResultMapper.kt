package no.nav.pensjon.simulator.sak.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.Fravik_19_3_Enum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtilleggEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.EosEkstra
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantiTrygdetid
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Ventetilleggsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FppGarantiKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ProRataBeregningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.UforeTypeCti
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

object PenVirkningsdatoResultMapper {

    fun fromDto(source: PenVirkningsdatoResult) =
        FoersteVirkningDatoCombo(
            foersteVirkningDatoGrunnlagListe = source.forsteVirkningsdatoGrunnlagListe.map(::virkningsdatoGrunnlag)
        )

    private fun afpHistorikk(source: PenAfpHistorikk) =
        AfpHistorikk(
            afpFpp = source.afpFpp,
            virkFom = source.virkFom?.toNorwegianDateAtNoon(),
            virkTom = source.virkTom?.toNorwegianDateAtNoon(),
            afpPensjonsgrad = source.afpPensjonsgrad,
            afpOrdning = source.afpOrdning?.let(::AfpOrdningTypeCti)
        )

    private fun eoesEkstra(source: PenEosEkstra) =
        EosEkstra(
            proRataBeregningType = source.proRataBeregningType?.let(::ProRataBeregningTypeCti),
            redusertAntFppAr = source.redusertAntFppAr,
            spt_eos = source.spt_eos,
            spt_pa_f92_eos = source.spt_pa_f92_eos,
            spt_pa_e91_eos = source.spt_pa_e91_eos,
            vilkar3_17Aok = source.vilkar3_17Aok,
        )

    private fun virkningsdatoGrunnlag(source: PenVirkningsdatoGrunnlag) =
        ForsteVirkningsdatoGrunnlag().apply {
            virkningsdato = source.virkningsdato?.toNorwegianDateAtNoon()
            kravFremsattDato = source.kravFremsattDato?.toNorwegianDateAtNoon()
            bruker = source.bruker?.let(::penPerson)
            annenPerson = source.annenPerson?.let(::penPerson)
            kravlinjeTypeEnum = source.kravlinjeType?.let(KravlinjeTypeEnum::valueOf)
        }

    private fun garantiTrygdetid(source: PenGarantiTrygdetid) =
        GarantiTrygdetid(
            trygdetid_garanti = source.trygdetid_garanti,
            fomDato = source.fomDato?.toNorwegianDateAtNoon(),
            tomDato = source.tomDato?.toNorwegianDateAtNoon()
        )

    private fun generellHistorikk(source: PenGenerellHistorikk) =
        GenerellHistorikk().apply {
            generellHistorikkId = source.generellHistorikkId
            fravik_19_3Enum = source.fravik_19_3?.let(Fravik_19_3_Enum::valueOf)
            fpp_eos = source.fpp_eos
            ventetilleggsgrunnlag = source.ventetilleggsgrunnlag?.let(::ventetilleggGrunnlag)
            poengtilleggEnum = source.poengtillegg?.let(PoengtilleggEnum::valueOf)
            eosEkstra = source.eosEkstra?.let(::eoesEkstra)
            garantiTrygdetid = source.garantiTrygdetid?.let(::garantiTrygdetid)
            sertillegg1943kull = source.sertillegg1943kull?.let(::saertillegg)
            giftFor2011 = source.giftFor2011
        }

    private fun merknad(source: PenMerknad) =
        Merknad(
            kode = source.kode,
            argumentListe = source.argumentListe.toMutableList()
        )

    private fun penPerson(source: PenPenPerson) =
        PenPerson(penPersonId = source.penPersonId).apply {
            pid = source.pid
            foedselsdato = source.fodselsdato
            afpHistorikkListe = source.afpHistorikkListe?.map(::afpHistorikk).orEmpty().toMutableList()
            uforehistorikk = source.uforehistorikk?.let(::ufoerehistorikk)
            generellHistorikk = source.generellHistorikk?.let(::generellHistorikk)
        }

    private fun reguleringInformasjon(source: PenReguleringsInformasjon) =
        ReguleringsInformasjon(
            lonnsvekst = source.lonnsvekst,
            fratrekksfaktor = source.fratrekksfaktor,
            gammelG = source.gammelG,
            nyG = source.nyG,
            reguleringsfaktor = source.reguleringsfaktor,
            gjennomsnittligUttaksgradSisteAr = source.gjennomsnittligUttaksgradSisteAr,
            reguleringsbelop = source.reguleringsbelop,
            prisOgLonnsvekst = source.prisOgLonnsvekst
        )

    private fun saertillegg(source: PenSaertillegg) =
        Sertillegg().apply {
            pSats_st = source.pSats_st
            brutto = source.brutto
            netto = source.netto
            fradrag = source.fradrag
            bruttoPerAr = source.bruttoPerAr
            nettoPerAr = source.nettoPerAr
            fradragPerAr = source.fradragPerAr
            ytelsekomponentTypeEnum = YtelseskomponentTypeEnum.valueOf(source.ytelsekomponentType)
            merknadListe = source.merknadListe.map(::merknad).toMutableList()
            fradragsTransaksjon = source.fradragsTransaksjon
            opphort = source.opphort
            sakTypeEnum = source.sakType?.let(SakTypeEnum::valueOf)
            formelKodeEnum = source.formelKode?.let(FormelKodeEnum::valueOf)
            reguleringsInformasjon = source.reguleringsInformasjon?.let(::reguleringInformasjon)
        }

    private fun ufoerehistorikk(source: PenUfoerehistorikk) =
        Uforehistorikk(
            uforeperiodeListe = source.uforeperiodeListe.map(::ufoereperiode).toMutableList(),
            garantigrad = source.garantigrad,
            garantigradYrke = source.garantigradYrke,
            sistMedlITrygden = source.sistMedlITrygden?.toNorwegianDateAtNoon(),
        )

    private fun ufoereperiode(source: PenUfoereperiode) =
        Uforeperiode(
            ufg = source.ufg,
            uft = source.uft?.toNorwegianDateAtNoon(),
            redusertAntFppAr = source.redusertAntFppAr,
            redusertAntFppAr_proRata = source.redusertAntFppAr_proRata,
            virk = source.virk?.toNorwegianDateAtNoon(),
            uftTom = source.uftTom?.toNorwegianDateAtNoon(),
            ufgFom = source.ufgFom?.toNorwegianDateAtNoon(),
            ufgTom = source.ufgTom?.toNorwegianDateAtNoon(),
            fodselsArYngsteBarn = source.fodselsArYngsteBarn,
            spt = source.spt,
            spt_proRata = source.spt_proRata,
            opt = source.opt,
            ypt = source.ypt,
            spt_pa_f92 = source.spt_pa_f92,
            spt_pa_e91 = source.spt_pa_e91,
            proRata_teller = source.proRata_teller,
            proRata_nevner = source.proRata_nevner,
            opt_pa_f92 = source.opt_pa_f92,
            opt_pa_e91 = source.opt_pa_e91,
            ypt_pa_f92 = source.ypt_pa_f92,
            ypt_pa_e91 = source.ypt_pa_e91,
            paa = source.paa,
            fpp = source.fpp,
            fpp_omregnet = source.fpp_omregnet,
            spt_eos = source.spt_eos,
            spt_pa_e91_eos = source.spt_pa_e91_eos,
            spt_pa_f92_eos = source.spt_pa_f92_eos,
            beregningsgrunnlag = source.beregningsgrunnlag,
            angittUforetidspunkt = source.angittUforetidspunkt?.toNorwegianDateAtNoon(),
            antattInntektFaktorKap19 = source.antattInntektFaktorKap19,
            antattInntektFaktorKap20 = source.antattInntektFaktorKap20,
            fppGaranti = source.fppGaranti,
            uforeType = source.uforeType?.let(::UforeTypeCti),
            fppGarantiKode = source.fppGarantiKode?.let(::FppGarantiKodeCti),
            proRataBeregningType = source.proRataBeregningType?.let(::ProRataBeregningTypeCti)
        )

    private fun ventetilleggGrunnlag(source: PenVentetilleggsgrunnlag) =
        Ventetilleggsgrunnlag(
            ventetilleggprosent = source.ventetilleggprosent,
            vt_spt = source.vt_spt,
            vt_opt = source.vt_opt,
            vt_pa = source.vt_pa,
            tt_vent = source.tt_vent
        )
}
