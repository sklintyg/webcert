package se.inera.intyg.webcert.web.service.jwt;

public interface JwtIntrospectionService {
    void validateToken(String token);
}
