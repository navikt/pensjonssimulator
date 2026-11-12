package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.spec.TpoFolketrygdberegnetAfpSimuleringTypeSpecV0
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec.FolketrygdberegnetAfpSimuleringTypeSpecV1
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec.NavSimuleringTypeSpecV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec.AnonymSimuleringTypeSpecV1
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringTypeSpecV2
import no.nav.pensjon.simulator.api.nav.v2.acl.spec.SimuleringstypeSpecDto
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.OftpSimuleringTypeSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.SimuleringTypeSpecV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.SimuleringTypeSpecV3
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class LegacySimuleringTypeAliasTest : ShouldSpec({
    val jsonMapper = JsonMapper.builder().findAndAddModules().build()
    val legacyAndCanonicalValues = listOf("ENDR_AP_M_AFP_PRIVAT", "ENDR_ALDER")

    should("accept legacy and canonical Nav V3 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", NavSimuleringTypeSpecV3::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            NavSimuleringTypeSpecV3.fromExternalValue(value).internalValue shouldBe SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical Nav V2 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", NavSimuleringTypeSpecV2::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            NavSimuleringTypeSpecV2.fromExternalValue(value).internalValue shouldBe SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical anonymous V1 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", AnonymSimuleringTypeSpecV1::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            AnonymSimuleringTypeSpecV1.fromExternalValue(value).internalValue shouldBe SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept the original Nav DTO public spelling and canonical change type as ENDR_ALDER") {
        listOf("ENDRING_ALDERSPENSJON_MED_PRIVAT_AFP", "ENDRING_ALDERSPENSJON").forEach { value ->
            jsonMapper.readValue("\"$value\"", SimuleringstypeSpecDto::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical OFTP change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", OftpSimuleringTypeSpec::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            OftpSimuleringTypeSpec.fromExternalValue(value).internalValue shouldBe SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical tjenestepensjon V2 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", SimuleringTypeSpecV2::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical tjenestepensjon V3 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", SimuleringTypeSpecV3::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical TPO folketrygdberegnet AFP V0 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", TpoFolketrygdberegnetAfpSimuleringTypeSpecV0::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            TpoFolketrygdberegnetAfpSimuleringTypeSpecV0.fromExternalValue(value).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("accept legacy and canonical folketrygdberegnet AFP V1 change types as ENDR_ALDER") {
        legacyAndCanonicalValues.forEach { value ->
            jsonMapper.readValue("\"$value\"", FolketrygdberegnetAfpSimuleringTypeSpecV1::class.java).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
            FolketrygdberegnetAfpSimuleringTypeSpecV1.fromExternalValue(value).internalValue shouldBe
                    SimuleringTypeEnum.ENDR_ALDER
        }
    }

    should("deserialize and map a Nav V2 request with the legacy alias to ENDR_ALDER without falling back to ALDER") {
        val uttakDato = LocalDate.of(2029, 1, 1)
        val source = jsonMapper.readValue(
            """
            {
                "simuleringId": 123,
                "simuleringType": "ENDR_AP_M_AFP_PRIVAT",
                "simuleringNavn": "Legacy alias regression",
                "lagringstidspunkt": "2026-11-11",
                "fnr": "${pid.value}",
                "fodselsar": 1963,
                "forventetInntekt": 250000,
                "forsteUttakDato": ${uttakDato.toNorwegianDateAtNoon().time},
                "utg": "P_100",
                "inntektEtterHeltUttak": 67500,
                "antallArInntektEtterHeltUttak": 5,
                "utenlandsopphold": 0,
                "flyktning": false,
                "sivilstatus": "UGIF",
                "epsPensjon": false,
                "eps2G": true,
                "simulerForTp": false,
                "tpOrigSimulering": false,
                "utenlandsperiodeForSimuleringList": []
            }
            """.trimIndent(),
            NavSimuleringSpecV2::class.java
        )
        source.simuleringType shouldBe NavSimuleringTypeSpecV2.ENDR_AP_M_AFP_PRIVAT

        val mapper = NavSimuleringSpecMapperV2(Arrange.foedselsdato(1963, 4, 5))
        val result = mapper.fromSimuleringSpecV2(
            source,
            brukBeregnetBeholdningVedPensjonsberegning = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false
        )

        result.type shouldBe SimuleringTypeEnum.ENDR_ALDER
        result.pid shouldBe pid
        result.foedselDato shouldBe LocalDate.of(1963, 4, 5)
        result.foersteUttakDato shouldBe uttakDato
        result.uttakGrad shouldBe UttakGradKode.P_100
        result.forventetInntektBeloep shouldBe 250000
        result shouldBe mapper.fromSimuleringSpecV2(
            source.copy(simuleringType = NavSimuleringTypeSpecV2.ENDR_ALDER),
            brukBeregnetBeholdningVedPensjonsberegning = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false
        )
    }
})
