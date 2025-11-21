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
package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;

public interface UtkastService {

    Utkast createNewDraft(CreateNewDraftRequest request);

    String getCertificateType(String certificateId);

    /**
     * Retrieves an utkast and always create a PDL log event.
     *
     * @param intygId utkast id
     * @return Utkast
     */
    Utkast getDraft(String intygId, boolean pdlLog);

    /**
     * Retrieves an utkast and always create a PDL log event.
     *
     * @param intygId utkast id
     * @param intygType utkast type
     * @return Utkast
     */
    Utkast getDraft(String intygId, String intygType);

    /**
     * Retrieves an utkast and will only create a PDL log event if createPdlLogEvent is set as true.
     *
     * Can be used when the utkast need to be retrieved for another service but won't be returned to the user.
     *
     * @param intygId utkast id
     * @param intygType utkast type
     * @param createPdlLogEvent true if a PDL log event should be logged.
     * @return Utkast
     */
    Utkast getDraft(String intygId, String intygType, boolean createPdlLogEvent);

    /**
     * Updates a draft (i.e Utkast) with data from an existing signed certificate.
     *
     * @param fromIntygId the identifier of the certificate we want to copy data from
     * @param fromIntygType the type of the certificate we want to copy data from
     * @param toUtkastId the identifier of the draft that we are copying data to
     * @param toUtkastType the type of the draft that we are copying data to
     * @return a response with the updated draft's status and version
     */
    SaveDraftResponse updateDraftFromCandidate(String fromIntygId, String fromIntygType, String toUtkastId, String toUtkastType);

    /**
     * Updates a draft (i.e Utkast) with data from an existing signed certificate.
     *
     * @param fromIntygId the identifier of the certificate we want to copy data from
     * @param fromIntygType the type of the certificate we want to copy data from
     * @param toUtkast the draft that we are copying data to
     * @return a response with the updated draft's status and version
     */
    SaveDraftResponse updateDraftFromCandidate(String fromIntygId, String fromIntygType, Utkast toUtkast);

    Utkast setNotifiedOnDraft(String intygsId, long version, Boolean notified);

    SaveDraftResponse saveDraft(String intygId, long version, String draftAsJson, boolean createPdlLogEvent);

    void updatePatientOnDraft(UpdatePatientOnDraftRequest request);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    List<Utkast> filterIntyg(UtkastFilter filter);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId, long version);

    int countFilterIntyg(UtkastFilter filter);

    String getQuestions(String intygsTyp, String version);

    void setKlarForSigneraAndSendStatusMessage(String intygsId, String intygsTyp);

    /**
     * Fairly specialized method to check if a person has existing Intyg of the same type.
     * Returns a Map of either "Utkast" or "Intyg" to Map of String, Boolean, where String is the intyg type and the
     * Boolean indicates that a previous Intyg or Utkast of the same type exists within the same caregiver.
     *
     * @param personnummer the personnummer of the patient to check for existing intyg
     * @param user the intended creator of the certificate or the logged in user
     * @param currentDraftId id of current draft if one exists
     */
    Map<String, Map<String, PreviousIntyg>> checkIfPersonHasExistingIntyg(Personnummer personnummer,
        IntygUser user,
        String currentDraftId);

    int lockOldDrafts(int lockedAfterDay, LocalDate today);

    void revokeLockedDraft(String intygId, String intygTyp, String revokeMessage, String reason);

    /**
     * Check if draft has been created through the replacement function.
     *
     * @return true or false value if draft has been created through a replacement of another certificate.
     */
    boolean isDraftCreatedFromReplacement(String certificateId);

    List<Utkast> findUtkastByPatientAndUnits(Personnummer patientId, List<String> unitIds);

    int dispose(LocalDateTime disposeObsoleteDraftsDate, Integer pageSize);
}