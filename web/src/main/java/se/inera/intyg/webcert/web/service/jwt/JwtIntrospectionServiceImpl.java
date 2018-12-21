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

@Service
public class JwtIntrospectionServiceImpl implements JwtIntrospectionService {

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
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("token", token);

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
