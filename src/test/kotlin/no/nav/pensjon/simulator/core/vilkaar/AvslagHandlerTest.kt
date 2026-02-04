package no.nav.pensjon.simulator.core.vilkaar

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException

class AvslagHandlerTest : FunSpec({

    context("handleAvslag") {

        test("kaster ikke exception for tom vedtak liste") {
            shouldNotThrow<Exception> {
                AvslagHandler.handleAvslag(emptyList())
            }
        }

        test("kaster ikke exception når anbefaltResultat ikke er AVSL") {
            val vedtakListe = listOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.INNV
                    begrunnelseEnum = null
                }
            )

            shouldNotThrow<Exception> {
                AvslagHandler.handleAvslag(vedtakListe)
            }
        }

        test("kaster ikke exception når anbefaltResultat er VELG") {
            val vedtakListe = listOf(
                VilkarsVedtak().apply {
                    anbefaltResultatEnum = VedtakResultatEnum.VELG
                    begrunnelseEnum = BegrunnelseTypeEnum.UNDER_3_AR_TT
                }
            )

            shouldNotThrow<Exception> {
                AvslagHandler.handleAvslag(vedtakListe)
            }
        }

        test("kaster UtilstrekkeligTrygdetidException for AVSL uten begrunnelse") {
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

        test("kaster UtilstrekkeligTrygdetidException for UNDER_1_AR_TT begrunnelse") {
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_1_AR_TT
                        }
                    )
                )
            }
        }

        test("kaster UtilstrekkeligTrygdetidException for UNDER_3_AR_TT begrunnelse") {
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_3_AR_TT
                        }
                    )
                )
            }
        }

        test("kaster UtilstrekkeligTrygdetidException for UNDER_5_AR_TT begrunnelse") {
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_5_AR_TT
                        }
                    )
                )
            }
        }

        test("kaster UtilstrekkeligTrygdetidException for UNDER_20_AR_TT_2025 begrunnelse") {
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_20_AR_TT_2025
                        }
                    )
                )
            }
        }

        test("kaster UtilstrekkeligOpptjeningException for LAVT_TIDLIG_UTTAK begrunnelse") {
            shouldThrow<UtilstrekkeligOpptjeningException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.LAVT_TIDLIG_UTTAK
                        }
                    )
                )
            }
        }

        test("kaster InvalidArgumentException for UTG_MINDRE_ETT_AR begrunnelse") {
            val exception = shouldThrow<InvalidArgumentException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UTG_MINDRE_ETT_AR
                        }
                    )
                )
            }

            exception.message shouldContain "ett år fra gradsendring"
        }

        test("kaster ikke exception for andre begrunnelser (logger kun warning)") {
            // Disse begrunnelsene skal bare logge warning, ikke kaste exception
            val andreBegrunnelser = listOf(
                BegrunnelseTypeEnum.UNDER_62,
                BegrunnelseTypeEnum.TOT_UTGRD_OVER_100,
                BegrunnelseTypeEnum.FAMPL_OG_AP,
                BegrunnelseTypeEnum.ANNET
            )

            andreBegrunnelser.forEach { begrunnelse ->
                shouldNotThrow<Exception> {
                    AvslagHandler.handleAvslag(
                        listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = begrunnelse
                            }
                        )
                    )
                }
            }
        }

        test("håndterer flere vedtak og kaster exception ved første avslag") {
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.INNV
                        },
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_3_AR_TT
                        }
                    )
                )
            }
        }

        test("håndterer flere vedtak uten avslag uten å kaste exception") {
            shouldNotThrow<Exception> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.INNV
                        },
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.VELG
                        }
                    )
                )
            }
        }

        test("filtrerer kun vedtak med AVSL resultat") {
            // Vedtak med INNV + begrunnelse som ville kastet exception, skal ikke kaste
            shouldNotThrow<Exception> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.INNV
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_3_AR_TT
                        }
                    )
                )
            }
        }

        test("kaster exception for første avslag ved flere AVSL vedtak") {
            // Første avslag kaster exception, så vi aldri behandler den andre
            shouldThrow<UtilstrekkeligTrygdetidException> {
                AvslagHandler.handleAvslag(
                    listOf(
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.UNDER_3_AR_TT
                        },
                        VilkarsVedtak().apply {
                            anbefaltResultatEnum = VedtakResultatEnum.AVSL
                            begrunnelseEnum = BegrunnelseTypeEnum.LAVT_TIDLIG_UTTAK
                        }
                    )
                )
            }
        }
    }
})
