package no.nav.pensjon.simulator.core.vilkaar

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException

class AvslagHandlerTest : ShouldSpec({

    context("funksjonen 'handleAvslag'") {
        context("tom vedtakliste") {
            should("ikke kaste exception") {
                shouldNotThrow<Exception> {
                    AvslagHandler.handleAvslag(vedtakListe = emptyList())
                }
            }
        }

        context("anbefalt resultat er ikke 'avslag'") {
            should("ikke kaste exception") {
                shouldNotThrow<Exception> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.INNV
                                begrunnelseEnum = null
                            }
                        )
                    )
                }
            }
        }

        context("avslag uten begrunnelse") {
            should("kaste UtilstrekkeligTrygdetidException med begrunnelse") {
                shouldThrow<UtilstrekkeligTrygdetidException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = null
                            }
                        )
                    )
                }.message shouldBe "Utilstrekkelig trygdetid"
            }
        }

        context("under 1 år botid") {
            should("kaste UtilstrekkeligTrygdetidException med begrunnelse") {
                shouldThrow<UtilstrekkeligTrygdetidException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = BegrunnelseTypeEnum.UNDER_1_AR_TT
                            }
                        )
                    )
                }.message shouldBe "Vilkår for forutgående medlemskap ikke oppfylt (under 1 år botid)"
            }
        }

        context("under 5 år botid") {
            should("kaste UtilstrekkeligTrygdetidException med begrunnelse") {
                shouldThrow<UtilstrekkeligTrygdetidException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = BegrunnelseTypeEnum.UNDER_5_AR_TT
                            }
                        )
                    )
                }.message shouldBe "Vilkår for forutgående medlemskap ikke oppfylt (under 5 år botid)"
            }
        }

        context("under 20 år botid") {
            should("kaste UtilstrekkeligTrygdetidException med begrunnelse") {
                shouldThrow<UtilstrekkeligTrygdetidException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = BegrunnelseTypeEnum.UNDER_20_AR_TT_2025
                            }
                        )
                    )
                }.message shouldBe "Vilkår for forutgående medlemskap ikke oppfylt (under 20 år botid)"
            }
        }

        context("utilstrekkelig opptjening") {
            should("kaste UtilstrekkeligOpptjeningException med begrunnelse") {
                shouldThrow<UtilstrekkeligOpptjeningException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = BegrunnelseTypeEnum.LAVT_TIDLIG_UTTAK
                            }
                        )
                    )
                }.message shouldBe "Utilstrekkelig opptjening"
            }
        }

        context("mindre enn ett år fra gradsendring") {
            should("kaste InvalidArgumentException med begrunnelse") {
                shouldThrow<InvalidArgumentException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                begrunnelseEnum = BegrunnelseTypeEnum.UTG_MINDRE_ETT_AR
                            }
                        )
                    )
                }.message shouldBe "Mindre enn ett år fra gradsendring"
            }
        }

        context("andre begrunnelser") {
            should("ikke kaste exception") {
                val andreBegrunnelser = listOf(
                    BegrunnelseTypeEnum.UNDER_62,
                    BegrunnelseTypeEnum.TOT_UTGRD_OVER_100,
                    BegrunnelseTypeEnum.FAMPL_OG_AP,
                    BegrunnelseTypeEnum.ANNET
                )

                andreBegrunnelser.forEach { begrunnelse ->
                    shouldNotThrow<Exception> {
                        AvslagHandler.handleAvslag(
                            vedtakListe = listOf(
                                VilkarsVedtak().apply {
                                    anbefaltResultatEnum = VedtakResultatEnum.AVSL
                                    begrunnelseEnum = begrunnelse
                                }
                            )
                        )
                    }
                }
            }
        }

        context("flere vedtak hvorav noen med avslag") {
            should("kaste exception for første avslag") {
                shouldThrow<UtilstrekkeligTrygdetidException> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
                            VilkarsVedtak().apply {
                                anbefaltResultatEnum = VedtakResultatEnum.INNV
                            },
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
                }.message shouldBe "Vilkår for forutgående medlemskap ikke oppfylt (under 3 år botid)"
            }
        }

        context("flere vedtak uten avslag") {
            should("ikke kaste exception") {
                shouldNotThrow<Exception> {
                    AvslagHandler.handleAvslag(
                        vedtakListe = listOf(
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
        }
    }
})