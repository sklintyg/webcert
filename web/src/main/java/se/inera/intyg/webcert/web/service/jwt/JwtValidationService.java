package se.inera.intyg.webcert.web.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtValidationService {
    Jws<Claims> validateJwsToken(String token);
}
