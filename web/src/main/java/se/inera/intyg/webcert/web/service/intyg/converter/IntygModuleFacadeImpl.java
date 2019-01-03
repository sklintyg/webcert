/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class IntygModuleFacadeImpl implements IntygModuleFacade {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleFacadeImpl.class);
    private static final String HSVARD_RECIPIENT_ID = "HSVARD";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Override
    public IntygPdf convertFromInternalToPdfDocument(String intygType, String internalIntygJsonModel, List<Status> statuses,
                                                     UtkastStatus utkastStatus, boolean isEmployer)
            throws IntygModuleFacadeException {

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType,
                    moduleRegistry.resolveVersionFromUtlatandeJson(internalIntygJsonModel));
            PdfResponse pdfResponse;
            if (!isEmployer) {
                pdfResponse = moduleApi.pdf(internalIntygJsonModel, statuses, ApplicationOrigin.WEBCERT, utkastStatus);
            } else {
                pdfResponse = moduleApi.pdfEmployer(internalIntygJsonModel, statuses, ApplicationOrigin.WEBCERT, Collections.emptyList(),
                        utkastStatus);
            }
            return new IntygPdf(pdfResponse.getPdfData(), pdfResponse.getFilename());
        } catch (ModuleException me) {
            LOG.error("ModuleException occured when when generating PDF document from internal");
            throw new IntygModuleFacadeException("ModuleException occured when generating PDF document from internal", me);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when generating PDF document from internal", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when registering certificate", e);
        }
    }

    // If there either are no statuses, or if there is no RECEIVED status, this intyg has not yet been signed.
    private boolean isUtkast(List<Status> statuses) {
        return statuses == null || statuses.stream().noneMatch(s -> s.getType() == CertificateState.RECEIVED);
    }

    @Override
    public CertificateResponse getCertificate(String certificateId, String intygType, String intygTypeVersion)
            throws IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType, intygTypeVersion);
            return moduleApi.getCertificate(certificateId, logicalAddress, HSVARD_RECIPIENT_ID);
        } catch (ModuleException me) {
            throw new IntygModuleFacadeException(me.getMessage(), me);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when registering certificate", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when registering certificate", e);
        }
    }

    @Override
    public void registerCertificate(String intygType, String internalIntygJsonModel) throws ModuleException, IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType,
                    moduleRegistry.resolveVersionFromUtlatandeJson(internalIntygJsonModel));
            moduleApi.registerCertificate(internalIntygJsonModel, logicalAddress);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when registering certificate", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when registering certificate", e);
        }
    }

    @Override
    public String getRevokeCertificateRequest(String intygType, Utlatande utlatande, HoSPersonal skapatAv, String message)
            throws ModuleException, IntygModuleFacadeException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType, utlatande.getTextVersion());
            return moduleApi.createRevokeRequest(utlatande, skapatAv, message);
        } catch (ModuleNotFoundException e) {
            LOG.error("ModuleNotFoundException occured for intygstyp '{}' when revoking certificate", intygType);
            throw new IntygModuleFacadeException("ModuleNotFoundException occured when revoking certificate", e);
        }
    }

    @Override
    public Utlatande getUtlatandeFromInternalModel(String intygType, String internalModel) {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType, moduleRegistry.resolveVersionFromUtlatandeJson(internalModel));
            return moduleApi.getUtlatandeFromJson(internalModel);
        } catch (IOException | ModuleNotFoundException | ModuleException e) {
            LOG.error("Module problems occured when trying to unmarshall Utlatande.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }
}
