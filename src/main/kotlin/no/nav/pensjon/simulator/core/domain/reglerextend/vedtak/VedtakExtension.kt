package no.nav.pensjon.simulator.core.domain.reglerextend.vedtak

import no.nav.pensjon.simulator.core.domain.regler.vedtak.ForutgaendeMedlemskap
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy

fun ForutgaendeMedlemskap.copy() =
    ForutgaendeMedlemskap().also {
        it.minstTreArsFMNorge = this.minstTreArsFMNorge
        it.minstFemArsFMNorge = this.minstFemArsFMNorge
        it.minstEttArFMNorge = this.minstEttArFMNorge
        it.unntakFraForutgaendeMedlemskap = this.unntakFraForutgaendeMedlemskap?.copy()
        it.unntakFraForutgaendeTT = this.unntakFraForutgaendeTT?.copy()
        it.oppfyltEtterGamleRegler = this.oppfyltEtterGamleRegler
        it.unntakHalvminsteytelseFolketrygd = this.unntakHalvminsteytelseFolketrygd
        it.unntakHalvminsteytelseUtland = this.unntakHalvminsteytelseUtland
        // Super:
        it.resultatEnum = this.resultatEnum
    }
