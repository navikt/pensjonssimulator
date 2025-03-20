package no.nav.pensjon.simulator.core.domain.reglerextend.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.Arbeidsavklaringspenger
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.ArbeidsavklaringspengerUT
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.BeregningYtelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.MotregningYtelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.SkattefriGrunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.SkattefriUforetrygdOrdiner
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.Sykepenger
import no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter.SykepengerUT
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copyYtelseskomponent

fun Arbeidsavklaringspenger.copy() =
    Arbeidsavklaringspenger().also {
        copyMotregningYtelseskomponent(source = this, target = it)
    }

fun ArbeidsavklaringspengerUT.copy() =
    ArbeidsavklaringspengerUT().also {
        copyMotregningYtelseskomponent(source = this, target = it)
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
