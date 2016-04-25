/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.mocks;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.ConverterException;
import se.inera.intyg.common.support.modules.registry.*;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.*;
import se.inera.intyg.common.support.modules.support.api.dto.*;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;


public class MockIntygModuleRegistry implements IntygModuleRegistry {
    @Override
    public ModuleApi getModuleApi(String id) throws ModuleNotFoundException {
        return new ModuleApi() {
            @Override
            public void setModuleContainer(ModuleContainerApi moduleContainer) {
            }

            @Override
            public ModuleContainerApi getModuleContainer() {
                return null;
            }

            @Override
            public ValidateDraftResponse validateDraft(InternalModelHolder internalModel) throws ModuleException {
                return null;
            }

            @Override
            public PdfResponse pdf(InternalModelHolder internalModel, List<Status> statuses, ApplicationOrigin applicationOrigin) throws ModuleException {
                return null;
            }

            @Override
            public PdfResponse pdfEmployer(InternalModelHolder internalModel, List<Status> statuses, ApplicationOrigin applicationOrigin) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse createNewInternal(CreateNewDraftHolder draftCertificateHolder) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse createNewInternalFromTemplate(CreateDraftCopyHolder draftCopyHolder, InternalModelHolder template) throws ModuleException {
                return null;
            }

            @Override
            public void registerCertificate(InternalModelHolder internalModel, String logicalAddress) throws ModuleException {
            }

            @Override
            public void sendCertificateToRecipient(InternalModelHolder internalModel, String logicalAddress, String recipientId) throws ModuleException {
            }

            @Override
            public CertificateResponse getCertificate(String certificateId, String logicalAddress) throws ModuleException {
                return null;
            }

            @Override
            public boolean isModelChanged(String persistedState, String currentState) throws ModuleException {
                return false;
            }

            @Override
            public InternalModelResponse updateBeforeSave(InternalModelHolder internalModel, HoSPersonal hosPerson) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse updateBeforeSigning(InternalModelHolder internalModel, HoSPersonal hosPerson, LocalDateTime signingDate) throws ModuleException {
                return null;
            }

            @Override
            public Object createNotification(NotificationMessage notificationMessage) throws ModuleException {
                CertificateStatusUpdateForCareType certificateStatusUpdateForCareType = new CertificateStatusUpdateForCareType();
                UtlatandeType utlatande = new UtlatandeType();
                UtlatandeId utlatandeId = new UtlatandeId();
                utlatandeId.setExtension(notificationMessage.getIntygsId());
                utlatande.setUtlatandeId(utlatandeId);
                certificateStatusUpdateForCareType.setUtlatande(utlatande);
                return certificateStatusUpdateForCareType;
            }

            @Override
            public String marshall(String jsonString) {
                return null;
            }

            @Override
            public Utlatande getUtlatandeFromJson(String utlatandeJson) {
                return null;
            }

            @Override
            public se.inera.intyg.common.support.model.common.internal.Utlatande getUtlatandeFromIntyg(Intyg intyg) throws ConverterException {
                throw new UnsupportedOperationException("Module Fk7263 does not support getUtlatandeFromIntyg.");
            }

            @Override
            public String transformToStatisticsService(String inputXml) throws ModuleException {
                return inputXml;
            }

            @Override
            public ValidateXmlResponse validateXml(String inputXml) throws ModuleException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, List<String>> getModuleSpecificArendeParameters(Utlatande utlatande) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String decorateUtlatande(String utlatandeJson) throws ModuleException {
                return utlatandeJson;
            }

            @Override
            public InternalModelResponse createRenewalFromTemplate(CreateDraftCopyHolder draftCopyHolder, InternalModelHolder internalModelHolder)
                    throws ModuleException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Intyg getIntygFromCertificateHolder(CertificateHolder certificateHolder) throws ModuleException {
                return null;
            }

            @Override
            public String getAdditionalInfo(Intyg intyg) throws ModuleException {
                return null;
            }

        };
    }

    @Override
    public ModuleEntryPoint getModuleEntryPoint(String id) throws ModuleNotFoundException {
        return null;
    }

    @Override
    public IntygModule getIntygModule(String id) throws ModuleNotFoundException {
        return null;
    }

    @Override
    public List<IntygModule> listAllModules() {
        return null;
    }

    @Override
    public List<ModuleEntryPoint> getModuleEntryPoints() {
        return null;
    }

    @Override
    public boolean moduleExists(String moduleId) {
        return false;
    }
}
