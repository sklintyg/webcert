/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser extends IntygUser {

    private static final long serialVersionUID = -2624303818412468774L;

    private Map<String, String> anvandarPreference = new HashMap<>();
    private IntegrationParameters parameters;
    private boolean useSigningService = false;
    private SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
    private String identityProviderForSign;
    private String launchFromOrigin;

    public WebCertUser() {
        super("only-for-test-use");
    }

    /**
     * The copy-constructor.
     */
    public WebCertUser(IntygUser intygUser) {
        super(intygUser.getHsaId());
        this.userTermsApprovedOrSubscriptionInUse = intygUser.isUserTermsApprovedOrSubscriptionInUse();
        this.personId = intygUser.getPersonId();
        this.isSekretessMarkerad = intygUser.isSekretessMarkerad();
        this.fornamn = intygUser.getFornamn();
        this.efternamn = intygUser.getEfternamn();
        this.namn = intygUser.getNamn();
        this.titel = intygUser.getTitel();
        this.forskrivarkod = intygUser.getForskrivarkod();
        this.authenticationScheme = intygUser.getAuthenticationScheme();
        this.vardgivare = intygUser.getVardgivare();
        this.befattningar = intygUser.getBefattningar();
        this.specialiseringar = intygUser.getSpecialiseringar();
        this.legitimeradeYrkesgrupper = intygUser.getLegitimeradeYrkesgrupper();

        this.valdVardenhet = intygUser.getValdVardenhet();
        this.valdVardgivare = intygUser.getValdVardgivare();
        this.miuNamnPerEnhetsId = intygUser.getMiuNamnPerEnhetsId();
        this.authenticationMethod = intygUser.getAuthenticationMethod();
        this.features = intygUser.getFeatures();
        this.roles = intygUser.getRoles();
        this.authorities = intygUser.getAuthorities();
        this.origin = intygUser.getOrigin();
        this.roleTypeName = intygUser.getRoleTypeName();
    }

    public Map<String, String> getAnvandarPreference() {
        return this.anvandarPreference;
    }

    @Override
    public boolean equals(final Object o) {
        if (super.equals(o)) {
            WebCertUser that = (WebCertUser) o;
            return Objects.equals(this.anvandarPreference, that.anvandarPreference)
                && Objects.equals(this.parameters, that.parameters)
                && Objects.equals(this.subscriptionInfo, that.subscriptionInfo);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(this.anvandarPreference, this.parameters, this.subscriptionInfo);
    }

    public void setAnvandarPreference(Map<String, String> anvandarMetadata) {
        this.anvandarPreference = anvandarMetadata;
    }

    public IntegrationParameters getParameters() {
        return parameters;
    }

    public void setParameters(IntegrationParameters parameters) {
        this.parameters = parameters;
    }

    public boolean isUseSigningService() {
        return useSigningService;
    }

    public void setUseSigningService(boolean useSigningService) {
        this.useSigningService = useSigningService;
    }

    public SubscriptionInfo getSubscriptionInfo() {
        return subscriptionInfo;
    }

    public void setSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
        this.subscriptionInfo = subscriptionInfo;
    }

    public String getIdentityProviderForSign() {
        return identityProviderForSign;
    }

    public void setIdentityProviderForSign(String identityProviderForSign) {
        this.identityProviderForSign = identityProviderForSign;
    }

    @JsonIgnore
    public boolean isValdVardenhetMottagning() {
        if (valdVardenhet == null) {
            return false;
        }

        for (Vardgivare vg : vardgivare) {
            for (Vardenhet ve : vg.getVardenheter()) {
                if (ve.getId().equals(valdVardenhet.getId())) {
                    return false;
                }
                for (Mottagning m : ve.getMottagningar()) {
                    if (m.getId().equals(valdVardenhet.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getLaunchFromOrigin() {
        return launchFromOrigin;
    }

    public void setLaunchFromOrigin(String launchFromOrigin) {
        this.launchFromOrigin = launchFromOrigin;
    }
}
