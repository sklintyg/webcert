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

import static se.inera.intyg.common.fkparent.model.converter.RespConstants.DIAGNOS_ICD_10_ID;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.icf.IcdCode;
import se.inera.intyg.common.support.facade.model.icf.Icf;
import se.inera.intyg.common.support.facade.model.icf.IcfCode;
import se.inera.intyg.common.support.facade.model.icf.IcfIcd;
import se.inera.intyg.common.support.modules.service.WebcertModuleService;
import se.inera.intyg.webcert.web.service.facade.IcfFacadeService;
import se.inera.intyg.webcert.web.service.fmb.icf.IcfService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Icd10KoderRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;
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
        if (request.getIcdCodes().length == 0) {
            return new IcfResponseDTO();
        }

        final var icfInternalData = icfService.findIcfInformationByIcd10Koder(
            Icd10KoderRequest.of(request.getIcd10Code(0), request.getIcd10Code(1), request.getIcd10Code(2)));

        if (icfInternalData.getGemensamma() == null && icfInternalData.getUnika() == null) {
            return new IcfResponseDTO();
        }

        return (convert(icfInternalData));
    }


    private IcfResponseDTO convert(IcfResponse response) {
        final var result = new IcfResponseDTO();

        List<IcdCode> activityLimitationCommonIcdCodes = null;
        List<IcfCode> activityLimitationCommonIcfCodes = null;
        List<IcfIcd> activityLimitationUniqueCodes = null;

        List<IcdCode> disabilityCommonIcdCodes = null;
        List<IcfCode> disabilityCommonIcfCodes = null;
        List<IcfIcd> disabilityUniqueCodes = null;

        if (response.getGemensamma() != null) {
            activityLimitationCommonIcdCodes = getIcdCodeList(response.getGemensamma()
                .getAktivitetsBegransningsKoder()
                .getIcd10Koder());

            activityLimitationCommonIcfCodes = getIcfCodeList(response.getGemensamma()
                .getAktivitetsBegransningsKoder()
                .getIcfKoder());

            disabilityCommonIcdCodes = getIcdCodeList(response.getGemensamma()
                .getFunktionsNedsattningsKoder()
                .getIcd10Koder());

            disabilityCommonIcfCodes = getIcfCodeList(response.getGemensamma()
                .getFunktionsNedsattningsKoder()
                .getIcfKoder());
        }

        if (response.getUnika() != null) {
            activityLimitationUniqueCodes = response.getUnika().stream().map(
                    icfDiagnoskodResponse -> IcfIcd.builder()
                        .icdCodes(List.of(IcdCode.builder().code(icfDiagnoskodResponse.getIcd10Kod())
                            .title(getDiagnosisTitle(icfDiagnoskodResponse.getIcd10Kod())).build()))
                        .icfCodes(getIcfCodeList(icfDiagnoskodResponse.getAktivitetsBegransningsKoder().getIcfKoder())).build())
                .collect(Collectors.toList());

            disabilityUniqueCodes = response.getUnika().stream().map(
                    icfDiagnoskodResponse -> IcfIcd.builder()
                        .icdCodes(List.of(IcdCode.builder()
                            .code(icfDiagnoskodResponse.getIcd10Kod())
                            .title(getDiagnosisTitle(icfDiagnoskodResponse.getIcd10Kod())).build()))
                        .icfCodes(getIcfCodeList(icfDiagnoskodResponse.getFunktionsNedsattningsKoder().getIcfKoder())).build())
                .collect(Collectors.toList());
        }

        result.setActivityLimitation(
            Icf.builder()
                .commonCodes(
                    IcfIcd.builder()
                        .icdCodes(activityLimitationCommonIcdCodes)
                        .icfCodes(activityLimitationCommonIcfCodes)
                        .build()
                )
                .uniqueCodes(activityLimitationUniqueCodes)
                .build()
        );

        result.setDisability(
            Icf.builder()
                .commonCodes(
                    IcfIcd.builder()
                        .icdCodes(disabilityCommonIcdCodes)
                        .icfCodes(disabilityCommonIcfCodes).build()
                )
                .uniqueCodes(disabilityUniqueCodes)
                .build()
        );

        return result;
    }

    private List<IcfCode> getIcfCodeList(List<IcfKod> icfCodeList) {
        return icfCodeList.stream()
            .map(icfKod ->
                IcfCode.builder()
                    .code(icfKod.getKod())
                    .description(icfKod.getBeskrivning())
                    .includes(icfKod.getInnefattar())
                    .title(icfKod.getBenamning()).build()
            ).collect(Collectors.toList());
    }

    private List<IcdCode> getIcdCodeList(List<String> icdCodeList) {
        return icdCodeList.stream()
            .map(icd10Code -> IcdCode.builder().title(getDiagnosisTitle(icd10Code)).code(icd10Code).build()
            ).collect(Collectors.toList());
    }

    private String getDiagnosisTitle(String icdCode) {
        return moduleService.getDescriptionFromDiagnosKod(icdCode, DIAGNOS_ICD_10_ID);
    }
}
