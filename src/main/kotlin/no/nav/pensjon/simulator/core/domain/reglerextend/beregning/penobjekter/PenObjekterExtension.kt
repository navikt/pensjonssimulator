package no.nav.pensjon.simulator.core.domain.reglerextend.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.*
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copyYtelseskomponent

fun Arbeidsavklaringspenger.copy() =
    Arbeidsavklaringspenger().also {
        copyMotregningYtelseskomponent(source = this, target = it)
    }

fun ArbeidsavklaringspengerUT.copy() =
    ArbeidsavklaringspengerUT().also {
        copyMotregningYtelseskomponent(source = this, target = it)
    }

fun FasteUtgifterTilleggUT.copy() =
    FasteUtgifterTilleggUT().also {
        it.nettoAkk = this.nettoAkk
        it.nettoRestAr = this.nettoRestAr
        it.avkortningsbelopPerAr = this.avkortningsbelopPerAr
        copyYtelseskomponent(source = this, target = it)
    }

fun Garantitillegg_Art_27.copy() =
    Garantitillegg_Art_27().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Garantitillegg_Art_27_UT.copy() =
    Garantitillegg_Art_27_UT().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Garantitillegg_Art_50.copy() =
    Garantitillegg_Art_50().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Hjelpeloshetsbidrag.copy() =
    Hjelpeloshetsbidrag().also {
        it.grunnlagForUtbetaling = this.grunnlagForUtbetaling
        copyYtelseskomponent(source = this, target = it)
    }

fun KrigOgGammelYrkesskade.copy() =
    KrigOgGammelYrkesskade().also {
        it.pensjonsgrad = this.pensjonsgrad
        it.grunnlagForUtbetaling = this.grunnlagForUtbetaling
        it.kapitalutlosning = this.kapitalutlosning
        it.ps = this.ps
        it.yg = this.yg
        it.mendel = this.mendel
        copyYtelseskomponent(source = this, target = it)
    }

fun Mendel.copy() =
    Mendel().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun SkattefriGrunnpensjon.copy() =
    SkattefriGrunnpensjon().also {
        it.pensjonsgrad = this.pensjonsgrad
        copyBeregningYtelseskomponent(source = this, target = it)
    }

fun SkattefriUforetrygdOrdiner.copy() =
    SkattefriUforetrygdOrdiner().also {
        it.pensjonsgrad = this.pensjonsgrad
        copyBeregningYtelseskomponent(source = this, target = it)
    }

fun Sykepenger.copy() =
    Sykepenger().also {
        copyMotregningYtelseskomponent(source = this, target = it)
    }

fun SykepengerUT.copy() =
    SykepengerUT().also {
        copyMotregningYtelseskomponent(source = this, target = it)
    }

fun copyBeregningYtelseskomponent(
    source: BeregningYtelseskomponent,
    target: BeregningYtelseskomponent
) {
    //TODO target.beregning = source.beregning?.let(::Beregning)
    copyYtelseskomponent(source, target)
}

fun copyMotregningYtelseskomponent(
    source: MotregningYtelseskomponent,
    target: MotregningYtelseskomponent
) {
    target.dagsats = source.dagsats
    target.antallDager = source.antallDager
    copyBeregningYtelseskomponent(source, target)
}
