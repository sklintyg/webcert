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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
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
@Profile("grp-rest-api")
public class GrpRestService {

    @Value("${cgi.grp.rest.url}")
    private String grpBaseUrl;
    @Value("${cgi.grp.rest.accessToken}")
    private String accessToken;
    @Value("${cgi.grp.rest.serviceId}")
    private String serviceId;
    @Value("${cgi.grp.rest.displayName}")
    private String displayName;

    private final RestClient grpRestClient;
    private final RedisTicketTracker redisTicketTracker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrpRestService(@Qualifier("grpRestClient") RestClient grpRestClient, RedisTicketTracker redisTicketTracker) {
        this.grpRestClient = grpRestClient;
        this.redisTicketTracker = redisTicketTracker;
    }

    private static final String INIT_PATH = "/init";
    private static final String COLLECT_PATH = "/collect";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String SERVICE_ID = "serviceId";
    private static final String REF_ID = "refId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PROVIDER = "provider";
    private static final String PERSONID_TYPE = "TIN";
    private static final String REQUEST_TYPE = "requestType";
    private static final String END_USER_INFO = "endUserInfo";
    private static final String PROVIDER_BANKID = "bankid";
    private static final String REQUEST_TYPE_AUTH = "AUTH";

    public GrpOrderResponse init(String personId, SignaturBiljett ticket) {
        try {
            return grpRestClient.post()
                .uri(UriComponentsBuilder.fromHttpUrl(grpBaseUrl)
                    .path(INIT_PATH)
                    .queryParam(SERVICE_ID, serviceId)
                    .queryParam(DISPLAY_NAME, displayName)
                    .queryParam(PROVIDER, PROVIDER_BANKID)
                    .queryParam(REQUEST_TYPE, REQUEST_TYPE_AUTH)
                    .queryParam(TRANSACTION_ID, ticket.getTicketId())
                    .queryParam(END_USER_INFO, ticket.getUserIpAddress())
                    .build().toUri())
                .header(ACCESS_TOKEN, accessToken)
                .body(orderRequest(personId))
                .retrieve()
                .body(GrpOrderResponse.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            final var errorMessage = handleError(e, ticket.getTicketId(), ticket.getIntygsId(), INIT_PATH.substring(1));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.GRP_PROBLEM, errorMessage, e);
        }
    }

    public GrpCollectResponse collect(String refId, String transactionId) {
        try {
            return grpRestClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl(grpBaseUrl)
                    .path(COLLECT_PATH)
                    .queryParam(SERVICE_ID, serviceId)
                    .queryParam(REF_ID, refId)
                    .queryParam(TRANSACTION_ID, transactionId)
                    .build()
                    .toUri())
                .header(ACCESS_TOKEN, accessToken)
                .retrieve()
                .body(GrpCollectResponse.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(handleError(e, transactionId, certificateId(transactionId), COLLECT_PATH.substring(1)), e);
            return null;
        }
    }

    private String handleError(HttpStatusCodeException e, String transactionId, String certificateId, String requestType) {
        redisTicketTracker.updateStatus(transactionId, SignaturStatus.OKAND);

        final var grpErrorResponse = parseErrorResponse(e);
        final var httpErrorResponse = String.join(" ", e.getStatusCode().toString(), e.getStatusText());
        return "Grp %s failure for transactionId '%s' and certificateId: '%s' with Grp error code '%s', Grp message '%s' and http status %s"
            .formatted(requestType, transactionId, certificateId, grpErrorResponse.getErrorCode(), grpErrorResponse.getMessage(),
                httpErrorResponse);
    }

    private GrpErrorResponse parseErrorResponse(HttpStatusCodeException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), GrpErrorResponse.class);

        } catch (Exception ex) {
            log.error("Failed to parse GRP error response", e);
            return GrpErrorResponse.builder().build();
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
