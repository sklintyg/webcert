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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.facade.CertificateTextVersionFacadeService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
public class CertificateTextVersionFacadeServiceImpl implements CertificateTextVersionFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateTextVersionFacadeServiceImpl.class);

    private final IntygTextsService intygTextsService;
    private final UtkastRepository utkastRepository;
    private final ObjectMapper objectMapper;
    private final IntygModuleRegistry moduleRegistry;
    private final MonitoringLogService monitoringLogService;

    public CertificateTextVersionFacadeServiceImpl(IntygTextsService intygTextsService, UtkastRepository utkastRepository,
        ObjectMapper objectMapper, IntygModuleRegistry moduleRegistry, MonitoringLogService monitoringLogService) {
        this.intygTextsService = intygTextsService;
        this.utkastRepository = utkastRepository;
        this.objectMapper = objectMapper;
        this.moduleRegistry = moduleRegistry;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public Utkast upgradeToLatestMinorTextVersion(Utkast utkast) {

        if (utkast == null || isLockedOrSigned(utkast)) {
            return utkast;
        }

        final var certificateId = utkast.getIntygsId();
        final var certificateType = utkast.getIntygsTyp();
        final var objectTextVersion = utkast.getIntygTypeVersion();
        final var latestTextVersion = intygTextsService.getLatestVersionForSameMajorVersion(certificateType, objectTextVersion);

        try {
            final var jsonModelTextVersion = getModelTextVersion(utkast);

            if (isLatestTextVersion(objectTextVersion, jsonModelTextVersion, latestTextVersion)) {
                return utkast;
            }

            if (!jsonModelTextVersion.equals(latestTextVersion)) {
                updateJsonModelTextVersion(utkast, latestTextVersion);
            }

            utkast.setIntygTypeVersion(latestTextVersion);
            logUpdatedTextVersion(certificateId, certificateType, objectTextVersion, latestTextVersion, jsonModelTextVersion);
            return utkastRepository.save(utkast);

        } catch (OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(certificateId, certificateType);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        } catch (JsonProcessingException e) {
            LOG.error("Failure updating json model to latest minor text version for certificate {} of type {} with text version {}.",
                certificateId, certificateType, objectTextVersion, e);
            return utkast;
        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            LOG.error("Could not confirm latest minor text version for certificate {} of type {} with text version {}.",
                certificateId, certificateType, objectTextVersion, e);
            return utkast;
        }
    }

    private boolean isLockedOrSigned(Utkast utkast) {
        final var status = utkast.getStatus();
        return status == UtkastStatus.DRAFT_LOCKED || status == UtkastStatus.SIGNED;
    }

    private String getModelTextVersion(Utkast utkast) throws ModuleNotFoundException, IOException, ModuleException {
        final var moduleAPi = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
        final var utlatande = moduleAPi.getUtlatandeFromJson(utkast.getModel(), utkast.getSkapad());
        return utlatande.getTextVersion();
    }

    private boolean isLatestTextVersion(String objectTextVersion, String modelTextVersion, String latestTextVersion) {
        return objectTextVersion.equals(latestTextVersion) && modelTextVersion.equals(latestTextVersion);
    }

    private void updateJsonModelTextVersion(Utkast utkast, String latestTextVersion) throws JsonProcessingException {
        final var jsonModel = utkast.getModel();
        final var jsonNode = objectMapper.readTree(jsonModel);
        final var textVersionNode = objectMapper.readTree("\"" + latestTextVersion + "\"");
        final var updatedModel = ((ObjectNode) jsonNode).set("textVersion", textVersionNode).toString();
        utkast.setModel(updatedModel);
    }

    private void logUpdatedTextVersion(String certificateId, String certificateType, String objectTextVersion, String latestTextVersion,
        String jsonModelTextVersion) {
        final var previousVersion = !objectTextVersion.equals(latestTextVersion) ? objectTextVersion : jsonModelTextVersion;
        LOG.info("Updating text version for certificate {} of type {} from version {} to version {}.", certificateId, certificateType,
            previousVersion, latestTextVersion);
    }
}