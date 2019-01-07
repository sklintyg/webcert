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
package se.inera.intyg.webcert.web.service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.web.service.jwt.dto.IntrospectionResponse;

/**
 * This class is responsible for calling the OIDC introspection endpoint to validate an OAuth token.
 */
@Service
public class JwtIntrospectionServiceImpl implements JwtIntrospectionService {

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String TOKEN = "token";

    @Value("${idp.oidc.introspection.endpoint.url}")
    private String tokenIntrospectionEndpointUrl;

    @Value("${idp.oidc.client.id}")
    private String clientId;

    @Value("${idp.oidc.client.secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void validateToken(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID, clientId);
        map.add(CLIENT_SECRET, clientSecret);
        map.add(TOKEN, token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenIntrospectionEndpointUrl, request, String.class);
            IntrospectionResponse introspectionResponse = objectMapper.readValue(response.getBody(), IntrospectionResponse.class);
            if (!introspectionResponse.isActive()) {
                throw new AuthenticationServiceException("OIDC introspection fails isActive check, has your OAuth token expired?");
            }
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

}
