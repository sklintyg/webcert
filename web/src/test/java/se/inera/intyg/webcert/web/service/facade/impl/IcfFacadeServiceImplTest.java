/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.service.WebcertModuleService;
import se.inera.intyg.webcert.web.service.fmb.icf.IcfService;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.AktivitetsBegransningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.FunktionsNedsattningsKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfDiagnoskodResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfRequestDTO;

@ExtendWith(MockitoExtension.class)
class IcfFacadeServiceImplTest {

    @Mock
    private IcfService icfService;

    @Mock
    private WebcertModuleService moduleService;

    @InjectMocks
    private IcfFacadeServiceImpl icfFacadeService;

    @Nested
    class CommonIcfCodes {

        private final String icfCode = "icf code";
        private final String icfTitle = "Icf title";
        private final String icfDescription = "Icf description";
        private final String icfIncludes = "Icf includes";
        private final String icdCode = "icd code";
        private final String icdTitle = "icd title";

        private IcfResponse icfResponse;
        private IcfDiagnoskodResponse commonDiagnoskodResponse;
        private IcfKod icfCodeObj;
        private List<IcfKod> icfCodes;
        private List<String> icdCodes;
        private FunktionsNedsattningsKoder disabilityCodes;
        private AktivitetsBegransningsKoder activityLimitationCodes;

        @BeforeEach
        void setUp() {
            icfResponse = new IcfResponse();
            commonDiagnoskodResponse = new IcfDiagnoskodResponse();
            icfCodeObj = new IcfKod(icfCode, icfTitle, icfDescription, icfIncludes);
            icfCodes = List.of(icfCodeObj);
            icdCodes = List.of(icdCode);
            disabilityCodes = FunktionsNedsattningsKoder.of(icdCodes, icfCodes);
            commonDiagnoskodResponse.setFunktionsNedsattningsKoder(disabilityCodes);
            activityLimitationCodes = AktivitetsBegransningsKoder.of(icdCodes, icfCodes);
            commonDiagnoskodResponse.setAktivitetsBegransningsKoder(activityLimitationCodes);
            icfResponse.setGemensamma(commonDiagnoskodResponse);

            doReturn(icfResponse)
                .when(icfService)
                .findIcfInformationByIcd10Koder(any());
        }

        @Test
        void shallReturnEmptyWhenNoCodesFound() {
            icfResponse.setGemensamma(null);
            icfResponse.setUnika(null);
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertNull(actual.getDisability());
            assertNull(actual.getActivityLimitation());
        }

        @Test
        void shallReturnResponseWithCommonActivityLimitationIcfCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());
            assertEquals(icfResponse.getGemensamma().getAktivitetsBegransningsKoder().getIcfKoder().size(),
                actual.getDisability().getCommonCodes().getIcfCodes().size());
        }


        @Test
        void shallReturnResponseWithCommonActivityLimitationIcdCodes() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());

            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getGemensamma().getAktivitetsBegransningsKoder().getIcd10Koder().size(),
                actual.getDisability().getCommonCodes().getIcdCodes().size());
        }

        @Test
        void shallReturnResponseWithCommonDisabilityIcfCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getGemensamma().getFunktionsNedsattningsKoder().getIcfKoder().size(),
                actual.getDisability().getCommonCodes().getIcfCodes().size());
        }

        @Test
        void shallReturnResponseWithCommonDisabilityIcdCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getGemensamma().getFunktionsNedsattningsKoder().getIcd10Koder().size(),
                actual.getDisability().getCommonCodes().getIcdCodes().size());

        }

        @Test
        void shallReturnResponseWithIcdTitle() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icdTitle, actual.getDisability().getCommonCodes().getIcdCodes().get(0).getTitle());
        }

        @Test
        void shallReturnResponseWithIcdCode() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icdCode, actual.getDisability().getCommonCodes().getIcdCodes().get(0).getCode());
        }

        @Test
        void shallReturnResponseWithIcfCode() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getKod(), actual.getActivityLimitation().getCommonCodes().getIcfCodes().get(0).getCode());
        }

        @Test
        void shallReturnResponseWithIcfTitle() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getBenamning(), actual.getActivityLimitation().getCommonCodes().getIcfCodes().get(0).getTitle());
        }

        @Test
        void shallReturnResponseWithIcfDescription() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getBeskrivning(),
                actual.getActivityLimitation().getCommonCodes().getIcfCodes().get(0).getDescription());
        }

        @Test
        void shallReturnResponseWithIcfIncludes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getInnefattar(), actual.getActivityLimitation().getCommonCodes().getIcfCodes().get(0).getIncludes());
        }

        IcfRequestDTO createRequestDTO() {
            final var request = new IcfRequestDTO();
            request.setIcdCodes(new String[]{"1"});

            return request;
        }
    }

    @Nested
    class UniqueIcfCodes {

        private final String icfCode = "icf code";
        private final String icfTitle = "Icf title";
        private final String icfDescription = "Icf description";
        private final String icfIncludes = "Icf includes";
        private final String icdCode = "icd code";
        private final String icdTitle = "icd title";

        private IcfResponse icfResponse;
        private IcfDiagnoskodResponse uniqueDiagnoskodResponse;
        private List<IcfDiagnoskodResponse> uniqueDiagnoskodRespones;
        private IcfKod icfCodeObj;
        private List<IcfKod> icfCodes;
        private List<String> icdCodes;
        private FunktionsNedsattningsKoder disabilityCodes;
        private AktivitetsBegransningsKoder activityLimitationCodes;

        @BeforeEach
        void setUp() {
            icfResponse = new IcfResponse();
            uniqueDiagnoskodResponse = new IcfDiagnoskodResponse();
            uniqueDiagnoskodResponse.setIcd10Kod(icdCode);
            icfCodeObj = new IcfKod(icfCode, icfTitle, icfDescription, icfIncludes);
            icfCodes = List.of(icfCodeObj);
            icdCodes = List.of(icdCode);
            disabilityCodes = FunktionsNedsattningsKoder.of(icdCodes, icfCodes);
            uniqueDiagnoskodResponse.setFunktionsNedsattningsKoder(disabilityCodes);
            activityLimitationCodes = AktivitetsBegransningsKoder.of(icdCodes, icfCodes);
            uniqueDiagnoskodResponse.setAktivitetsBegransningsKoder(activityLimitationCodes);
            uniqueDiagnoskodRespones = List.of(uniqueDiagnoskodResponse);
            icfResponse.setUnika(uniqueDiagnoskodRespones);

            doReturn(icfResponse)
                .when(icfService)
                .findIcfInformationByIcd10Koder(any());
        }


        @Test
        void shallReturnEmptyWhenNoCodesFound() {
            icfResponse.setGemensamma(null);
            icfResponse.setUnika(null);
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertNull(actual.getDisability());
            assertNull(actual.getActivityLimitation());
        }

        @Test
        void shallReturnResponseWithUniqueActivityLimitationIcfCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());
            assertEquals(icfResponse.getUnika().get(0).getAktivitetsBegransningsKoder().getIcfKoder().size(),
                actual.getDisability().getUniqueCodes().get(0).getIcfCodes().size());
        }


        @Test
        void shallReturnResponseWithUniqueActivityLimitationIcdCodes() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());

            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getUnika().get(0).getAktivitetsBegransningsKoder().getIcd10Koder().size(),
                actual.getDisability().getUniqueCodes().get(0).getIcdCodes().size());
        }

        @Test
        void shallReturnResponseWithUniqueDisabilityIcfCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getUnika().get(0).getFunktionsNedsattningsKoder().getIcfKoder().size(),
                actual.getDisability().getUniqueCodes().get(0).getIcfCodes().size());
        }

        @Test
        void shallReturnResponseWithUniqueDisabilityIcdCodes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfResponse.getUnika().get(0).getFunktionsNedsattningsKoder().getIcd10Koder().size(),
                actual.getDisability().getUniqueCodes().get(0).getIcdCodes().size());

        }

        @Test
        void shallReturnResponseWithIcdTitle() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icdTitle, actual.getDisability().getUniqueCodes().get(0).getIcdCodes().get(0).getTitle());
        }

        @Test
        void shallReturnResponseWithIcdCode() {
            doReturn(icdTitle)
                .when(moduleService)
                .getDescriptionFromDiagnosKod(any(), any());
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icdCode, actual.getDisability().getUniqueCodes().get(0).getIcdCodes().get(0).getCode());
        }

        @Test
        void shallReturnResponseWithIcfCode() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getKod(), actual.getActivityLimitation().getUniqueCodes().get(0).getIcfCodes().get(0).getCode());
        }

        @Test
        void shallReturnResponseWithIcfTitle() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getBenamning(), actual.getActivityLimitation().getUniqueCodes().get(0).getIcfCodes().get(0).getTitle());
        }

        @Test
        void shallReturnResponseWithIcfDescription() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getBeskrivning(),
                actual.getActivityLimitation().getUniqueCodes().get(0).getIcfCodes().get(0).getDescription());
        }

        @Test
        void shallReturnResponseWithIcfIncludes() {
            final var actual = icfFacadeService.getIcfInformation(createRequestDTO());

            assertEquals(icfCodeObj.getInnefattar(),
                actual.getActivityLimitation().getUniqueCodes().get(0).getIcfCodes().get(0).getIncludes());
        }

        IcfRequestDTO createRequestDTO() {
            final var request = new IcfRequestDTO();
            request.setIcdCodes(new String[]{"1"});

            return request;
        }
    }
}
