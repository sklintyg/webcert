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
package se.inera.intyg.webcert.web.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@Service
public class ITIntegrationServiceImpl implements ITIntegrationService {

    @Bean("itRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    @Qualifier("itRestTemplate")
    private RestTemplate restTemplate;

    @Value("${intygstjanst.base.url}")
    private String intygstjanstenUrl;

    @Override
    public List<MessageFromIT> findMessagesByCertificateId(String certificateId) {
        final var url = intygstjanstenUrl + "/internalapi/message/" + certificateId;
        try {
            return Arrays.asList(restTemplate.getForObject(url, MessageFromIT[].class));
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw ex;
            }
            return Collections.emptyList();
        }
    }

    @Override
    public ItIntygInfo getCertificateInfo(String certificateId) {
        final var url = intygstjanstenUrl + "/internalapi/intygInfo/" + certificateId;
        try {
            return restTemplate.getForObject(url, ItIntygInfo.class);
        } catch (HttpClientErrorException ex) {
            return new ItIntygInfo();
        }
    }

    @Override
    public CertificateListResponse getCertificatesForDoctor(QueryIntygParameter queryParam, Set<String> types) {
        final String url = intygstjanstenUrl + "/internalapi/certificatelist/certificates/doctor";
        CertificateListRequest requestObject = getCertificateListRequest(queryParam, types);
        return restTemplate.postForObject(url, requestObject, CertificateListResponse.class);
    }

    private CertificateListRequest getCertificateListRequest(QueryIntygParameter queryParam, Set<String> types) {
        CertificateListRequest requestObject = new CertificateListRequest();
        requestObject.setHsaId(queryParam.getHsaId());
        requestObject.setCivicRegistrationNumber(queryParam.getPatientId());
        requestObject.setUnitIds(queryParam.getUnitIds());
        requestObject.setToDate(queryParam.getSignedTo());
        requestObject.setFromDate(queryParam.getSignedFrom());
        requestObject.setOrderBy(queryParam.getOrderBy());
        requestObject.setOrderAscending(queryParam.getOrderAscending());
        requestObject.setStartFrom(queryParam.getStartFrom());
        requestObject.setPageSize(queryParam.getPageSize());
        requestObject.setTypes(types);
        return requestObject;
    }
}
