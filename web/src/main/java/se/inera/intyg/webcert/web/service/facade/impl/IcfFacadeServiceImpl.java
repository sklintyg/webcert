/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOS_ICD_10_ID;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.icf.AvailableIcfCodes;
import se.inera.intyg.common.support.facade.model.icf.Icd10Code;
import se.inera.intyg.common.support.facade.model.icf.IcfCode;
import se.inera.intyg.common.support.facade.model.icf.IcfCodeCollection;
import se.inera.intyg.common.support.modules.service.WebcertModuleService;
import se.inera.intyg.webcert.web.service.facade.IcfFacadeService;
import se.inera.intyg.webcert.web.service.fmb.icf.IcfService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKoder;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfResponse;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.IcfResponseDTO;

@Service
public class IcfFacadeServiceImpl implements IcfFacadeService {

    private final IcfService icfService;

    private final WebcertModuleService moduleService;

    @Autowired
    public IcfFacadeServiceImpl(IcfService icfService, WebcertModuleService moduleService) {
        this.icfService = icfService;
        this.moduleService = moduleService;
    }

    @Override
    public IcfResponseDTO getIcfInformation(IcfRequestDTO request) {
        if (isIcdCodesEmpty(request)) {
            return new IcfResponseDTO();
        }

        final var icfInternalData = getIcfData(request);

        if (isIcfDataEmpty(icfInternalData)) {
            return new IcfResponseDTO();
        }

        return convert(icfInternalData);
    }

    private IcfResponse getIcfData(IcfRequestDTO request) {
        return icfService.findIcfInformationByIcd10Koder(
            Icd10KoderRequest.of(request.getIcd10Code(0), request.getIcd10Code(1), request.getIcd10Code(2)));
    }

    private boolean isIcfDataEmpty(IcfResponse icfInternalData) {
        return icfInternalData.getGemensamma() == null && icfInternalData.getUnika() == null;
    }

    private boolean isIcdCodesEmpty(IcfRequestDTO request) {
        return request.getIcdCodes() == null || request.getIcdCodes().length == 0;
    }

    private IcfResponseDTO convert(IcfResponse response) {
        final var result = new IcfResponseDTO();

        result.setActivityLimitation(
            AvailableIcfCodes.builder()
                .commonCodes(
                    getActivityLimitationCommonCodes(response)
                )
                .uniqueCodes(
                    getActivityLimitationUniqueCodes(response)
                )
                .build()
        );

        result.setDisability(
            AvailableIcfCodes.builder()
                .commonCodes(
                    getDisabilityCommonCodes(response)
                )
                .uniqueCodes(
                    getDisabilityUniqueCodes(response)
                )
                .build()
        );

        return result;
    }

    private IcfCodeCollection getDisabilityCommonCodes(IcfResponse response) {
        if (response.getGemensamma() == null) {
            return IcfCodeCollection.builder().build();
        }

        return getCommonCodes(response.getGemensamma().getFunktionsNedsattningsKoder());
    }

    private IcfCodeCollection getActivityLimitationCommonCodes(IcfResponse response) {
        if (response.getGemensamma() == null) {
            return IcfCodeCollection.builder().build();
        }

        return getCommonCodes(response.getGemensamma().getAktivitetsBegransningsKoder());
    }

    private IcfCodeCollection getCommonCodes(IcfKoder icfKoder) {
        if (icfKoder == null) {
            return IcfCodeCollection.builder().build();
        }

        return IcfCodeCollection.builder()
            .icd10Codes(
                icfKoder.getIcd10Koder().stream().sorted(Collections.reverseOrder()).map(this::getIcdCode).collect(Collectors.toList())
            )
            .icfCodes(
                getIcfCodeList(icfKoder.getIcfKoder())
            )
            .build();
    }

    private List<IcfCodeCollection> getDisabilityUniqueCodes(IcfResponse response) {
        if (response.getUnika() == null) {
            return Collections.emptyList();
        }

        return response.getUnika().stream()
            .map(icfDiagnoskodResponse ->
                IcfCodeCollection.builder()
                    .icd10Codes(
                        List.of(
                            getIcdCode(icfDiagnoskodResponse.getIcd10Kod())
                        )
                    )
                    .icfCodes(icfDiagnoskodResponse.getFunktionsNedsattningsKoder() != null ? getIcfCodeList(
                        icfDiagnoskodResponse.getFunktionsNedsattningsKoder().getIcfKoder()) : Collections.emptyList()
                    )
                    .build())
            .collect(Collectors.toList());
    }

    private List<IcfCodeCollection> getActivityLimitationUniqueCodes(IcfResponse response) {
        if (response.getUnika() == null) {
            return Collections.emptyList();
        }

        return response.getUnika().stream()
            .map(icfDiagnoskodResponse ->
                IcfCodeCollection.builder()
                    .icd10Codes(
                        List.of(
                            getIcdCode(icfDiagnoskodResponse.getIcd10Kod())
                        )
                    )
                    .icfCodes(
                        icfDiagnoskodResponse.getAktivitetsBegransningsKoder() != null ? getIcfCodeList(
                            icfDiagnoskodResponse.getAktivitetsBegransningsKoder().getIcfKoder()) : Collections.emptyList()
                    )
                    .build())
            .collect(Collectors.toList());
    }

    private List<IcfCode> getIcfCodeList(List<IcfKod> icfCodeList) {
        return icfCodeList.stream()
            .map(icfKod ->
                IcfCode.builder()
                    .code(icfKod.getKod())
                    .description(icfKod.getBeskrivning())
                    .includes(icfKod.getInnefattar())
                    .title(icfKod.getBenamning())
                    .build()
            ).collect(Collectors.toList());
    }

    private Icd10Code getIcdCode(String icd10Code) {
        return Icd10Code.builder()
            .title(getDiagnosisTitle(icd10Code))
            .code(icd10Code)
            .build();
    }

    private String getDiagnosisTitle(String icdCode) {
        return moduleService.getDescriptionFromDiagnosKod(icdCode, DIAGNOS_ICD_10_ID);
    }
}
