/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.config;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.infra.security.filter.InternalApiFilter;
import se.inera.intyg.webcert.infra.security.filter.PrincipalUpdatedFilter;
import se.inera.intyg.webcert.infra.security.filter.RequestContextHolderUpdateFilter;
import se.inera.intyg.webcert.infra.security.filter.SecurityHeadersFilter;
import se.inera.intyg.webcert.infra.security.filter.SessionTimeoutFilter;
import se.inera.intyg.webcert.logging.MdcServletFilter;
import se.inera.intyg.webcert.web.logging.MdcUserServletFilter;
import se.inera.intyg.webcert.web.web.filter.AllowCorsFilter;
import se.inera.intyg.webcert.web.web.filter.DefaultCharacterEncodingFilter;
import se.inera.intyg.webcert.web.web.filter.LaunchIdValidationFilter;
import se.inera.intyg.webcert.web.web.filter.UnitSelectedAssuranceFilter;

/**
 * Registers all servlet filters with explicit ordering, preserving the order from the legacy
 * web.xml. Bean method names use the "Registration" suffix to avoid conflicts with the existing
 * {@code @Component} filter beans of the same base name.
 *
 * <p>springSecurityFilterChain (order 6) and springSessionRepositoryFilter (order 1) are
 * auto-managed by Spring Boot and Spring Session via application.properties:
 *
 * <ul>
 *   <li>spring.security.filter.order=6
 *   <li>spring.session.servlet.filter-order=1
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class WebFilterConfig {

  // @Component filters — inject the existing Spring-managed beans
  private final MdcServletFilter mdcServletFilter;
  private final MdcUserServletFilter mdcUserServletFilter;
  private final DefaultCharacterEncodingFilter defaultCharacterEncodingFilter;
  private final InternalApiFilter internalApiFilter;
  private final UnitSelectedAssuranceFilter unitSelectedAssuranceFilter;
  private final LaunchIdValidationFilter launchIdValidationFilter;
  private final AllowCorsFilter allowCorsFilter;

  // Order 2 — updates RequestContextHolder after spring-session sets up the session
  @Bean
  public FilterRegistrationBean<RequestContextHolderUpdateFilter>
      requestContextHolderUpdateFilterRegistration() {
    var registration = new FilterRegistrationBean<>(new RequestContextHolderUpdateFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(2);
    return registration;
  }

  // Order 3 — MDC correlation IDs for logging
  @Bean
  public FilterRegistrationBean<MdcServletFilter> mdcServletFilterRegistration() {
    var registration = new FilterRegistrationBean<>(mdcServletFilter);
    registration.addUrlPatterns("/*");
    registration.setOrder(3);
    return registration;
  }

  // Order 4 — character encoding fix for legacy PDF view URLs
  @Bean
  public FilterRegistrationBean<DefaultCharacterEncodingFilter>
      defaultCharacterEncodingFilterRegistration() {
    var registration = new FilterRegistrationBean<>(defaultCharacterEncodingFilter);
    registration.addUrlPatterns("/v2/visa/intyg/*");
    registration.setOrder(4);
    return registration;
  }

  // Order 5 — custom session timeout (runs before Spring Security so it can pre-invalidate)
  @Bean
  public FilterRegistrationBean<SessionTimeoutFilter> sessionTimeoutFilterRegistration() {
    var filter = new SessionTimeoutFilter();
    filter.setSkipRenewSessionUrls("/moduleapi/stat,/api/session-auth-check/ping");
    var registration = new FilterRegistrationBean<>(filter);
    registration.addUrlPatterns("/*");
    registration.setOrder(5);
    return registration;
  }

  // Order 7 — detects principal changes and touches the Redis session so changes are persisted
  @Bean
  public FilterRegistrationBean<PrincipalUpdatedFilter> principalUpdatedFilterRegistration() {
    var registration = new FilterRegistrationBean<>(new PrincipalUpdatedFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(7);
    return registration;
  }

  // Order 8 — verifies a unit is selected before allowing access to api/moduleapi
  @Bean
  public FilterRegistrationBean<UnitSelectedAssuranceFilter>
      unitSelectedAssuranceFilterRegistration() {
    var registration = new FilterRegistrationBean<>(unitSelectedAssuranceFilter);
    registration.addUrlPatterns("/api/*", "/moduleapi/*");
    registration.setInitParameters(
        Map.of(
            "ignoredUrls",
            "/api/config,/api/anvandare,/api/anvandare/andraenhet,/api/jslog,/moduleapi/stat,/api/user"));
    registration.setOrder(8);
    return registration;
  }

  // Order 9 — adds security-related HTTP response headers
  @Bean
  public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
    var registration = new FilterRegistrationBean<>(new SecurityHeadersFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(9);
    return registration;
  }

  // Order 10 — populates MDC with authenticated user details for logging
  @Bean
  public FilterRegistrationBean<MdcUserServletFilter> mdcUserServletFilterRegistration() {
    var registration = new FilterRegistrationBean<>(mdcUserServletFilter);
    registration.addUrlPatterns("/*");
    registration.setOrder(10);
    return registration;
  }

  // Order 11 — restricts access to /internalapi/* to requests from the internal port
  @Bean
  public FilterRegistrationBean<InternalApiFilter> internalApiFilterRegistration() {
    var registration = new FilterRegistrationBean<>(internalApiFilter);
    registration.addUrlPatterns("/internalapi/*");
    registration.setOrder(11);
    return registration;
  }

  // Order 12 — validates launchId header on api/moduleapi requests
  @Bean
  public FilterRegistrationBean<LaunchIdValidationFilter> launchIdValidationFilterRegistration() {
    var registration = new FilterRegistrationBean<>(launchIdValidationFilter);
    registration.addUrlPatterns("/api/*", "/moduleapi/*");
    registration.setOrder(12);
    return registration;
  }

  // Order 13 — allows CORS for the session invalidation endpoint
  @Bean
  public FilterRegistrationBean<AllowCorsFilter> allowCorsFilterRegistration() {
    var registration = new FilterRegistrationBean<>(allowCorsFilter);
    registration.addUrlPatterns("/api/v1/session/invalidate");
    registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
    registration.setOrder(13);
    return registration;
  }
}