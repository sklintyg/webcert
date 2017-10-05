/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.HashMap;
import java.util.Map;

import se.inera.intyg.infra.security.common.model.IntygUser;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser extends IntygUser {

    private static final long serialVersionUID = -2624303818412468774L;

    private Map<String, String> anvandarPreference;
    private IntegrationParameters parameters;

    public WebCertUser() {
        super("only-for-test-use");
    }

    /** The copy-constructor. */
    public WebCertUser(IntygUser intygUser) {
        super(intygUser.getHsaId());
        this.privatLakareAvtalGodkand = intygUser.isPrivatLakareAvtalGodkand();
        this.personId = intygUser.getPersonId();
        this.isSekretessMarkerad = intygUser.isSekretessMarkerad();
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
    }

    public Map<String, String> getAnvandarPreference() {
        if (anvandarPreference == null) {
            anvandarPreference = new HashMap<>();
        }
        return anvandarPreference;
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
}
