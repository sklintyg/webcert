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

package se.inera.intyg.webcert.web.service.underskrift.grp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpCancelResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpCollectResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpErrorResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpOrderRequest;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpOrderResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpSubjectIdentifier;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpRestClient {

    @Value("${cgi.grp.serviceId}")
    private String serviceId;
    @Value("${cgi.grp.displayName}")
    private String displayName;
    @Value("${cgi.grp.accessToken}")
    private String accessToken;
    @Value("${cgi.grp.url}")
    private String baseUrl;

    private final RestClient restClient;
    private final RedisTicketTracker redisTicketTracker;

    private static final String INIT_PATH = "/init";
    private static final String COLLECT_PATH = "/collect";
    private static final String CANCEL_PATH = "/cancel";
    private static final String SERVICE_ID = "serviceId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REF_ID = "refId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PROVIDER = "provider";
    private static final String PERSONID_TYPE = "TIN";
    private static final String REQUEST_TYPE = "requestType";
    private static final String END_USER_INFO = "endUserInfo";
    private static final String PROVIDER_BANKID = "bankid";
    private static final String REQUEST_TYPE_AUTH = "AUTH";
    private static final String LOCALHOST_IP = "127.0.0.1";

    public GrpOrderResponse init(String personId, SignaturBiljett ticket) {
        try {
            return restClient.post()
                .uri(UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path(INIT_PATH)
                    .queryParam(SERVICE_ID, serviceId)
                    .queryParam(DISPLAY_NAME, displayName)
                    .queryParam(PROVIDER, PROVIDER_BANKID)
                    .queryParam(REQUEST_TYPE, REQUEST_TYPE_AUTH)
                    .queryParam(TRANSACTION_ID, ticket.getTicketId())
                    .queryParam(END_USER_INFO, LOCALHOST_IP)
                    .build()
                    .toUri())
                .header(ACCESS_TOKEN, accessToken)
                .body(orderRequest(personId))
                .retrieve()
                .body(GrpOrderResponse.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            final var httpErrorResponse = handleError(e, ticket.getTicketId(), ticket.getIntygsId(), INIT_PATH.substring(1));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, httpErrorResponse, e);
        }
    }

    public GrpCollectResponse collect(String refId, String transactionId) {
        try {
            return grpGet(COLLECT_PATH, refId, transactionId).body(GrpCollectResponse.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleError(e, transactionId, certificateId(transactionId), COLLECT_PATH.substring(1));
            return null;
        }
    }

    public GrpCancelResponse cancel(String refId, String transactionId) {
        try {
            final var cancelResponse = grpGet(CANCEL_PATH, refId, transactionId).body(GrpCancelResponse.class);
            redisTicketTracker.updateStatus(transactionId, SignaturStatus.AVBRUTEN);
            return cancelResponse;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            final var httpErrorResponse = handleError(e, transactionId, certificateId(transactionId), CANCEL_PATH.substring(1));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, httpErrorResponse, e);
        }
    }

    private RestClient.ResponseSpec grpGet(String path, String refId, String transactionId) {
        return restClient.get()
            .uri(UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(path)
                .queryParam(SERVICE_ID, serviceId)
                .queryParam(REF_ID, refId)
                .queryParam(TRANSACTION_ID, transactionId)
                .build()
                .toUri())
            .header(ACCESS_TOKEN, accessToken)
            .retrieve();
    }

    private String handleError(HttpStatusCodeException e, String transactionId, String certificateId, String requestType) {
        redisTicketTracker.updateStatus(transactionId, SignaturStatus.OKAND);

        final var grpErrorResponse = parseErrorResponse(e);
        final var httpErrorResponse = String.join(" ", e.getStatusCode().toString(), e.getStatusText());
        if (grpErrorResponse != null) {
            log.error("Grp {} request failure for transactionId '{}' and certificateId '{}' with error code '{}' and message '{}'",
                requestType, transactionId, certificateId, grpErrorResponse.getErrorCode(), grpErrorResponse.getMessage(), e);
        } else {
            log.error("Grp {} request failure for transactionId '{}' and certificateId '{}' with http status '{}'",
                requestType, transactionId, certificateId, httpErrorResponse);
        }
        return httpErrorResponse;
    }

    private GrpErrorResponse parseErrorResponse(HttpStatusCodeException e) {
        try {
            final var objectMapper = new ObjectMapper();
            final var errorBody = e.getResponseBodyAsString();
            return !errorBody.isEmpty()
                ? objectMapper.readValue(errorBody, GrpErrorResponse.class)
                : null;
        } catch (Exception ex) {
            log.error("Failed to parse GRP error response", e);
            return null;
        }
    }

    private GrpOrderRequest orderRequest(String personId) {
        final var subjectIdentifier = GrpSubjectIdentifier.builder()
            .value(personId)
            .type(PERSONID_TYPE)
            .build();
        return GrpOrderRequest.builder()
            .subjectIdentifier(subjectIdentifier)
            .build();
    }

    private String certificateId(String transactionId) {
        return redisTicketTracker.findBiljett(transactionId).getIntygsId();
    }
}
