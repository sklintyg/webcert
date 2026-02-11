package se.inera.intyg.webcert.web.csintegration.integration.dto;

import lombok.Builder;

import java.util.List;


@Builder
public record GetUnansweredCommunicationInternalRequestDTO(List<String> patientIdList, int maxDays) {

}