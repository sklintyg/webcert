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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.facade.model.Patient;

public class PatientResponseDTO {

    private Patient patient;
    private PatientResponseStatusDTO status;

    public static PatientResponseDTO create(Patient patient) {
        final var responseDTO = new PatientResponseDTO();
        if (patient == null) {
            responseDTO.status = PatientResponseStatusDTO.NOT_FOUND;
        } else {
            responseDTO.patient = patient;
            responseDTO.status = PatientResponseStatusDTO.FOUND;
        }
        return responseDTO;
    }

    public static PatientResponseDTO createErrorResponse() {
        final var responseDTO = new PatientResponseDTO();
        responseDTO.status = PatientResponseStatusDTO.ERROR;
        return responseDTO;
    }

    public static PatientResponseDTO createInvalidPatientIdResponse() {
        final var responseDTO = new PatientResponseDTO();
        responseDTO.status = PatientResponseStatusDTO.INVALID_PATIENT_ID;
        return responseDTO;
    }

    public static PatientResponseDTO createNoNameResponse() {
        final var responseDTO = new PatientResponseDTO();
        responseDTO.status = PatientResponseStatusDTO.NO_NAME;
        return responseDTO;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setStatus(PatientResponseStatusDTO status) {
        this.status = status;
    }

    public PatientResponseStatusDTO getStatus() {
        return status;
    }
}
