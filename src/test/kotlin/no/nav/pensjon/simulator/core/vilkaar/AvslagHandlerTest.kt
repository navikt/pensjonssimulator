package no.nav.pensjon.simulator.core.vilkaar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException

class AvslagHandlerTest : FunSpec({

    test("handleAvslag should throw 'utilstrekkelig trygdetid' exception for 'avslag uten begrunnelse'") {
        shouldThrow<UtilstrekkeligTrygdetidException> {
            AvslagHandler.handleAvslag(
                listOf(
                    VilkarsVedtak().apply {
                        anbefaltResultatEnum = VedtakResultatEnum.AVSL
                        begrunnelseEnum = null
                    }
                )
            )
        }
    }
})
