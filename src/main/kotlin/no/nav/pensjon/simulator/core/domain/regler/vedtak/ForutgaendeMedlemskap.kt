package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Unntak
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class ForutgaendeMedlemskap : AbstraktVilkar {
    /**
     * Minst tre års forutgående medlemskap i Norge med uføretidspunkt FOM 01.01.1994
     */
    var minstTreArsFMNorge: Boolean = false

    /**
     * Avdøde har minst ett års forutgående medlemskap i Norge med dødsdato før 01.01.1994 og virkningsdato FOM 01.01.1990
     */
    var minstEttArFMNorge: Boolean = false

    /**
     * Minst fem års forutgående medlemskap i Norge med uføretidspunkt FOM 01.01.1994 (regelprøvd ved førsteKravFremsattDato fom 01.01.2021)
     */
    val minstFemArsFMNorge: Boolean? = null

    /**
     * Unntak fra forutgående medlemskap
     */
    var unntakFraForutgaendeMedlemskap: Unntak? = null

    /**
     * Unntak fra forutgående trygdetid
     */
    var unntakFraForutgaendeTT: Unntak? = null

    /**
     * Oppfylt etter gamle no.nav.preg.domain.regler.regler og virkningsdato før 01.01.1990
     */
    var oppfyltEtterGamleRegler: Boolean = false

    constructor()

    constructor(aForutgaendeMedlemskap: ForutgaendeMedlemskap?) : super(aForutgaendeMedlemskap!!) {
        minstTreArsFMNorge = aForutgaendeMedlemskap.minstTreArsFMNorge
        minstEttArFMNorge = aForutgaendeMedlemskap.minstEttArFMNorge
        oppfyltEtterGamleRegler = aForutgaendeMedlemskap.oppfyltEtterGamleRegler
        if (aForutgaendeMedlemskap.unntakFraForutgaendeMedlemskap != null) {
            unntakFraForutgaendeMedlemskap = Unntak(aForutgaendeMedlemskap.unntakFraForutgaendeMedlemskap!!)
        }
        if (aForutgaendeMedlemskap.unntakFraForutgaendeTT != null) {
            unntakFraForutgaendeMedlemskap = Unntak(aForutgaendeMedlemskap.unntakFraForutgaendeTT!!)
        }
    }

    constructor(
        resultat: VilkarOppfyltUTCti? = null,
        /** Interne felt */
        minstTreArsFMNorge: Boolean = false,
        minstEttArFMNorge: Boolean = false,
        unntakFraForutgaendeMedlemskap: Unntak? = null,
        unntakFraForutgaendeTT: Unntak? = null,
        oppfyltEtterGamleRegler: Boolean = false
    ) : super(resultat) {
        this.minstTreArsFMNorge = minstTreArsFMNorge
        this.minstEttArFMNorge = minstEttArFMNorge
        this.unntakFraForutgaendeMedlemskap = unntakFraForutgaendeMedlemskap
        this.unntakFraForutgaendeTT = unntakFraForutgaendeTT
        this.oppfyltEtterGamleRegler = oppfyltEtterGamleRegler
    }

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var fm: ForutgaendeMedlemskap? = null
        if (abstraktVilkar.javaClass == ForutgaendeMedlemskap::class.java) {
            fm = ForutgaendeMedlemskap(abstraktVilkar as ForutgaendeMedlemskap?)
        }
        return fm
    }
}
