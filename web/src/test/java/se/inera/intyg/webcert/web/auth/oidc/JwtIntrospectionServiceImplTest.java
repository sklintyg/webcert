package se.inera.intyg.webcert.web.auth.oidc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.web.service.jwt.JwtIntrospectionServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtIntrospectionServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JwtIntrospectionServiceImpl testee;

    private String responseJson = "{\n" +
            "\"scope\": \"\",\n" +
            "\"active\": true,\n" +
            "\"exp\": 1545051289,\n" +
            "\"token_type\": \"Bearer\",\n" +
            "\"client_id\": \"lupander\"\n" +
            "}";

    @Before
    public void init() {
        ReflectionTestUtils.setField(testee, "tokenIntrospectionEndpointUrl", "http://some.url");
        ReflectionTestUtils.setField(testee, "clientId", "CLIENT_ID");
        ReflectionTestUtils.setField(testee, "clientSecret", "CLIENT_SECRET");
    }

    @Test
    public void testActiveToken() {

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok(responseJson));
        testee.validateToken("header.claims.signature");
    }
}
