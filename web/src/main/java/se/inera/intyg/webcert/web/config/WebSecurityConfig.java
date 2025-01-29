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

package se.inera.intyg.webcert.web.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import static se.inera.intyg.webcert.web.auth.CustomAuthenticationEntrypoint.SITHS_REQUEST_MATCHER;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ATTRIBUTE_EMPLOYEE_HSA_ID;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ATTRIBUTE_IDENTITY_PROVIDER_FOR_SIGN;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ATTRIBUTE_LOGIN_METHOD;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ATTRIBUTE_SECURITY_LEVEL_DESCRIPTION;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ATTRIBUTE_SUBJECT_SERIAL_NUMBER;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.AUTHN_CONTEXT_CLASS_REF_PATTERN;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEMENT_LOCAL_NAME_SESSION_INDEX;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.METADATA_LOCATION_STRING_TEMPLATE;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.NAMESPACE_PREFIX_SAML2P;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_ELEG;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_SITHS;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.REGISTRATION_ID_SITHS_NORMAL;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SAML_2_0_NAMEID_FORMAT_TRANSIENT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SAML_2_0_PROTOCOL;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import se.inera.intyg.infra.security.common.cookie.IneraCookieSerializer;
import se.inera.intyg.webcert.web.auth.CsrfCookieFilter;
import se.inera.intyg.webcert.web.auth.CustomAccessDeniedHandler;
import se.inera.intyg.webcert.web.auth.CustomAuthenticationEntrypoint;
import se.inera.intyg.webcert.web.auth.CustomAuthenticationFailureHandler;
import se.inera.intyg.webcert.web.auth.CustomAuthenticationSuccessHandler;
import se.inera.intyg.webcert.web.auth.CustomXFrameOptionsHeaderWriter;
import se.inera.intyg.webcert.web.auth.Saml2AuthenticationToken;
import se.inera.intyg.webcert.web.auth.SpaCsrfTokenRequestHandler;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.auth.eleg.ElegAuthenticationMethodResolver;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;

@Configuration(proxyBeanMethods = false)
@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@EnableRedisIndexedHttpSession
public class WebSecurityConfig {

    private final ElegWebCertUserDetailsService elegWebCertUserDetailsService;
    private final ElegAuthenticationMethodResolver elegAuthMethodResolver;
    private final WebcertUserDetailsService webcertUserDetailsService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final Environment environment;

    @Value("${saml.sp.entity.id.eleg}")
    private String samlEntityIdEleg;
    @Value("${saml.sp.entity.id.siths}")
    private String samlEntityIdSiths;
    @Value("${saml.sp.entity.id.siths.normal}")
    private String samlEntityIdSithsNormal;
    @Value("${saml.sp.assertion.consumer.service.location.siths}")
    private String assertionConsumerServiceLocationSiths;
    @Value("${saml.sp.assertion.consumer.service.location.eleg}")
    private String assertionConsumerServiceLocationEleg;
    @Value("${saml.sp.assertion.consumer.service.location.siths.normal}")
    private String assertionConsumerServiceLocationSithsNormal;
    @Value("${saml.idp.metadata.location.siths}")
    private String samlIdpMetadataLocationSiths;
    @Value("${saml.idp.metadata.location.eleg}")
    private String samlIdpMetadataLocationEleg;
    @Value("${saml.sp.single.logout.service.location}")
    private String singleLogoutServiceLocation;
    @Value("${saml.sp.single.logout.service.response.location}")
    private String singleLogoutServiceResponseLocation;
    @Value("${saml.logout.success.url}")
    private String samlLogoutSuccessUrl;
    @Value("${saml.keystore.type:PKCS12}")
    private String keyStoreTypeSiths;
    @Value("${sakerhetstjanst.saml.keystore.file}")
    private String keyStorePath;
    @Value("${sakerhetstjanst.saml.keystore.alias}")
    private String keyAlias;
    @Value("${sakerhetstjanst.saml.keystore.password}")
    private String keyStorePassword;
    @Value("${webcert.domain.name}")
    private String webcertDomainName;

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository()
        throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {

        final var keyStoreSiths = KeyStore.getInstance(keyStoreTypeSiths);
        keyStoreSiths.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePassword.toCharArray());
        final var appPrivateKey = (PrivateKey) keyStoreSiths.getKey(keyAlias, keyStorePassword.toCharArray());
        final var appCertificate = (X509Certificate) keyStoreSiths.getCertificate(keyAlias);

        final var registrationEleg = buildRegistration(samlIdpMetadataLocationEleg, REGISTRATION_ID_ELEG,
            samlEntityIdEleg, assertionConsumerServiceLocationEleg, appPrivateKey, appCertificate);

        final var registrationSiths = buildRegistration(samlIdpMetadataLocationSiths, REGISTRATION_ID_SITHS,
            samlEntityIdSiths, assertionConsumerServiceLocationSiths, appPrivateKey, appCertificate);

        final var registrationSithsNormal = buildRegistration(samlIdpMetadataLocationSiths, REGISTRATION_ID_SITHS_NORMAL,
            samlEntityIdSithsNormal, assertionConsumerServiceLocationSithsNormal, appPrivateKey, appCertificate);

        return new InMemoryRelyingPartyRegistrationRepository(List.of(registrationSiths, registrationEleg, registrationSithsNormal));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RelyingPartyRegistrationRepository relyingPartyRegistrationRepository,
        Saml2LogoutRequestResolver logoutRequestResolver, CustomAuthenticationEntrypoint customAuthenticationEntrypoint,
        CustomAccessDeniedHandler customAccessDeniedHandler, CustomXFrameOptionsHeaderWriter customXFrameOptionsHeaderWriter,
        RequestCache requestCache) throws Exception {

        if (environment.matchesProfiles("dev", "testability-api")) {
            configureTestability(http);
        }

        http
            .authorizeHttpRequests(request -> request
                .requestMatchers(antMatcher("/metrics")).permitAll()
                .requestMatchers(antMatcher("/services/**")).permitAll()
                .requestMatchers(antMatcher("/api/config/**")).permitAll()
                .requestMatchers(antMatcher("/api/configuration/**")).permitAll()
                .requestMatchers(antMatcher("/api/log/**")).permitAll()
                .requestMatchers(antMatcher("/api/session-auth-check/**")).permitAll()
                .requestMatchers(antMatcher("/internalapi/**")).permitAll()
                .requestMatchers(antMatcher("/favicon.ico")).permitAll()
                .requestMatchers(antMatcher("/api/signature/signservice/v1/metadata")).permitAll()
                .requestMatchers(antMatcher("/api/v1/session/invalidate")).permitAll()
                .anyRequest().fullyAuthenticated())
            .saml2Metadata(
                withDefaults()
            )
            .saml2Login(saml2 -> saml2
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                .authenticationManager(new ProviderManager(getOpenSaml4AuthenticationProvider()))
                .failureHandler(customAuthenticationFailureHandler)
                .successHandler(customAuthenticationSuccessHandler)
            )
            .saml2Logout(saml2 -> saml2
                .logoutRequest(logout -> logout
                    .logoutRequestResolver(logoutRequestResolver))
            )
            .logout(logout -> logout
                .logoutSuccessUrl(samlLogoutSuccessUrl)
            )
            .requestCache(cacheConfigurer -> cacheConfigurer
                .requestCache(requestCache)
            )
            .exceptionHandling(exceptionConfigurer -> exceptionConfigurer
                .authenticationEntryPoint(customAuthenticationEntrypoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .headers(headersConfigurer -> headersConfigurer
                .frameOptions(FrameOptionsConfig::disable)
                .addHeaderWriter(customXFrameOptionsHeaderWriter)
            )
            .csrf(csrfConfigurer -> csrfConfigurer
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers(
                    SITHS_REQUEST_MATCHER,
                    antMatcher("/internalapi/**"),
                    antMatcher("/api/signature/**"),
                    antMatcher("/testability/**"),
                    antMatcher("/authtestability/**"),
                    antMatcher("/services/**"),
                    antMatcher("/api/v1/session/invalidate")
                )
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean(name = "mvcHandlerMappingIntrospector")
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public DefaultCookieSerializer cookieSerializer() {
        return new IneraCookieSerializer(true);
    }

    @Bean
    Saml2AuthenticationRequestResolver authenticationRequestResolver(RelyingPartyRegistrationRepository registrations) {
        final var registrationResolver = new DefaultRelyingPartyRegistrationResolver(registrations);
        final var authenticationRequestResolver = new OpenSaml4AuthenticationRequestResolver(registrationResolver);
        authenticationRequestResolver.setAuthnRequestCustomizer(context -> {
                context.getAuthnRequest().setAttributeConsumingServiceIndex(1);
                context.getAuthnRequest().setRequestedAuthnContext(buildRequestedAuthnContext(context.getRelyingPartyRegistration()));
            }
        );
        return authenticationRequestResolver;
    }

    @Bean
    Saml2LogoutRequestResolver logoutRequestResolver(RelyingPartyRegistrationRepository registrations) {
        final var logoutRequestResolver = new OpenSaml4LogoutRequestResolver(registrations);
        logoutRequestResolver.setParametersConsumer(parameters -> {
            final var token = (Saml2AuthenticationToken) parameters.getAuthentication();
            final var principal = (DefaultSaml2AuthenticatedPrincipal) token.getSaml2Authentication().getPrincipal();
            final var registrationId = principal.getRelyingPartyRegistrationId();
            final var name = principal.getName();
            final var logoutRequest = parameters.getLogoutRequest();
            final var nameId = logoutRequest.getNameID();
            final var sessionIndex = new MySessionIndex(SAML_2_0_PROTOCOL, ELEMENT_LOCAL_NAME_SESSION_INDEX, NAMESPACE_PREFIX_SAML2P);
            final var issuer = logoutRequest.getIssuer();

            nameId.setValue(name);
            nameId.setFormat(SAML_2_0_NAMEID_FORMAT_TRANSIENT);
            sessionIndex.setValue(principal.getSessionIndexes().getFirst());
            logoutRequest.getSessionIndexes().add(sessionIndex);

            if (registrationId.equals(REGISTRATION_ID_SITHS) || registrationId.equals(REGISTRATION_ID_SITHS_NORMAL)) {
                final var issuerValue = METADATA_LOCATION_STRING_TEMPLATE.formatted(webcertDomainName, registrationId);
                issuer.setValue(issuerValue);
            }
        });
        return logoutRequestResolver;
    }

    private RelyingPartyRegistration buildRegistration(String metadataLocation, String registrationId, String entityId,
        String assertionConsumerLocation, PrivateKey key, X509Certificate certificate) {
        return RelyingPartyRegistrations.fromMetadataLocation(metadataLocation)
            .registrationId(registrationId)
            .entityId(entityId)
            .assertionConsumerServiceLocation(assertionConsumerLocation)
            .singleLogoutServiceLocation(singleLogoutServiceLocation)
            .singleLogoutServiceResponseLocation(singleLogoutServiceResponseLocation)
            .signingX509Credentials(signing -> signing.add(Saml2X509Credential.signing(key, certificate)))
            .build();
    }

    private void configureTestability(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
            .requestMatchers(antMatcher("/testability/**")).permitAll()
            .requestMatchers(antMatcher("/api/hsa-api/**")).permitAll()
        );
    }

    private OpenSaml4AuthenticationProvider getOpenSaml4AuthenticationProvider() {
        final var authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(responseToken -> {

            final var authentication = OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter()
                .convert(responseToken);

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            final var isElegLogin = getRegistrationId(authentication).equals(REGISTRATION_ID_ELEG);
            return isElegLogin ? elegSaml2AuthenticationToken(authentication) : sithsSaml2AuthenticationToken(authentication);
        });

        return authenticationProvider;
    }

    private Saml2AuthenticationToken sithsSaml2AuthenticationToken(Saml2Authentication authentication) {
        final var authMethod = getSithsAuthMethod(authentication);
        final var personId = getAttribute(authentication, ATTRIBUTE_EMPLOYEE_HSA_ID);
        final var identityProviderForSign = getAttribute(authentication, ATTRIBUTE_IDENTITY_PROVIDER_FOR_SIGN);
        final var principal = webcertUserDetailsService.buildUserPrincipal(personId, authMethod, identityProviderForSign);
        final var saml2AuthenticationToken = new Saml2AuthenticationToken(principal, authentication);
        saml2AuthenticationToken.setAuthenticated(true);
        return saml2AuthenticationToken;
    }

    private Saml2AuthenticationToken elegSaml2AuthenticationToken(Saml2Authentication authentication) {
        final var personId = getAttribute(authentication, ATTRIBUTE_SUBJECT_SERIAL_NUMBER);
        final var authScheme = getElegAuthScheme(authentication);
        final var loginMethod = getAttribute(authentication, ATTRIBUTE_LOGIN_METHOD);
        final var authMethod = elegAuthMethodResolver.resolveAuthenticationMethod(loginMethod);
        final var principal = elegWebCertUserDetailsService.buildUserPrincipal(personId, authScheme, authMethod);
        final var saml2AuthenticationToken = new Saml2AuthenticationToken(principal, authentication);
        saml2AuthenticationToken.setAuthenticated(true);
        return saml2AuthenticationToken;
    }

    private String getSithsAuthMethod(Saml2Authentication authentication) {
        final var saml2Response = authentication.getSaml2Response();
        final var matcher = AUTHN_CONTEXT_CLASS_REF_PATTERN.matcher(saml2Response);
        return matcher.find() ? matcher.group() : null;
    }

    private String getRegistrationId(Saml2Authentication saml2Authentication) {
        return ((DefaultSaml2AuthenticatedPrincipal) saml2Authentication.getPrincipal()).getRelyingPartyRegistrationId();
    }

    private String getElegAuthScheme(Saml2Authentication saml2Authentication) {
        final var securityLevelDescription = getAttribute(saml2Authentication, ATTRIBUTE_SECURITY_LEVEL_DESCRIPTION);
        return AuthConstants.ELEG_AUTHN_CLASSES.stream()
            .filter(authClass -> authClass.endsWith(securityLevelDescription))
            .findFirst()
            .orElse(securityLevelDescription);
    }

    private String getAttribute(Saml2Authentication samlCredential, String attributeId) {
        final var principal = (DefaultSaml2AuthenticatedPrincipal) samlCredential.getPrincipal();
        final var attributes = principal.getAttributes();
        if (attributes.containsKey(attributeId)) {
            return (String) attributes.get(attributeId).getFirst();
        }
        throw new IllegalArgumentException("Could not extract attribute '%s' from Saml2Authentication.".formatted(attributeId));
    }

    public static class MySessionIndex extends XSStringImpl implements SessionIndex {
        public MySessionIndex(String namespaceURI, String elementLocalName, String namespacePrefix) {
            super(namespaceURI, elementLocalName, namespacePrefix);
        }
    }

    private RequestedAuthnContext buildRequestedAuthnContext(RelyingPartyRegistration registration) {
        if (registration.getRegistrationId().equals(REGISTRATION_ID_ELEG)) {
            return new RequestedAuthnContextBuilder().buildObject();
        }

        final var authnContextClassRefLoa2 = getAuthnContextClassRef();
        final var authnContextClassRefLoa3 = getAuthnContextClassRef();
        authnContextClassRefLoa2.setURI("http://id.sambi.se/loa/loa2");
        authnContextClassRefLoa3.setURI("http://id.sambi.se/loa/loa3");

        final var requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
        final var requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRefLoa2);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRefLoa3);
        return requestedAuthnContext;
    }

    private AuthnContextClassRef getAuthnContextClassRef() {
        final var authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        return authnContextClassRefBuilder.buildObject(SAMLConstants.SAML20_NS, AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);
    }

}
