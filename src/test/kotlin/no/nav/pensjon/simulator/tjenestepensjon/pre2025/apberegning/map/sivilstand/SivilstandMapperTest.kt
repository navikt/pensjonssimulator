package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.sivilstand

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SivilstandKode

class SivilstandMapperTest : StringSpec({

    ("Alle sivilstatuser blir mappet til sivilstand"){
        SivilstandMapper.map(SivilstatusType.ENKE) shouldBe SivilstandKode.ENKE
        SivilstandMapper.map(SivilstatusType.GJPA) shouldBe SivilstandKode.ENKE
        SivilstandMapper.map(SivilstatusType.GIFT) shouldBe SivilstandKode.GIFT
        SivilstandMapper.map(SivilstatusType.SEPR) shouldBe SivilstandKode.GIFT
        SivilstandMapper.map(SivilstatusType.REPA) shouldBe SivilstandKode.REGISTRERT_PARTNER
        SivilstandMapper.map(SivilstatusType.SEPA) shouldBe SivilstandKode.REGISTRERT_PARTNER
        SivilstandMapper.map(SivilstatusType.SKIL) shouldBe SivilstandKode.SKILT
        SivilstandMapper.map(SivilstatusType.SKPA) shouldBe SivilstandKode.SKILT
        SivilstandMapper.map(SivilstatusType.UGIF) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.NULL) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.GLAD) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.SAMB) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.PLAD) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.GJSA) shouldBe SivilstandKode.UGIFT
        SivilstandMapper.map(SivilstatusType.GJES) shouldBe SivilstandKode.UGIFT
    }

})
