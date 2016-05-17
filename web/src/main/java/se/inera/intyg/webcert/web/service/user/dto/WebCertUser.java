/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_PRIVATLAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_TANDLAKARE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.util.Assert;
import se.inera.intyg.common.integration.hsa.model.AuthenticationMethod;
import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.auth.authorities.Privilege;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.model.UserDetails;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser implements UserDetails {

    private static final long serialVersionUID = -2624303818412468774L;

    private boolean privatLakareAvtalGodkand;

    private String personId;
    private String hsaId;
    private String namn;
    private String titel;
    private String forskrivarkod;
    private String authenticationScheme;

    private List<Vardgivare> vardgivare;
    private List<String> befattningar;
    private List<String> specialiseringar;
    private List<String> legitimeradeYrkesgrupper;

    private SelectableVardenhet valdVardenhet;
    private SelectableVardenhet valdVardgivare;

    private AuthenticationMethod authenticationMethod;

    // Fields related to the authority context
    private Set<String> features;
    private Map<String, Role> roles;
    private Map<String, Privilege> authorities;
    private String origin;


    /** The sole constructor. */
    public WebCertUser() {
    }


    // ~ Public scope
    // ======================================================================================================

    public boolean changeValdVardenhet(String vardenhetId) {
        if (vardenhetId == null) {
            return false;
        }

        for (Vardgivare vg : getVardgivare()) {
            SelectableVardenhet ve = vg.findVardenhet(vardenhetId);
            if (ve != null) {
                setValdVardenhet(ve);
                setValdVardgivare(vg);
                return true;
            }
        }

        return false;
    }

    public Set<String> getFeatures() {
        if (features == null) {
            features = new HashSet<>();
        }
        return features;
    }

    public void setFeatures(Set<String> features) {
        this.features = features;
    }

    public boolean isFeatureActive(String featureName) {
        return features != null && features.contains(featureName);
    }

    public boolean isFeatureActive(WebcertFeature feature) {
        Assert.notNull(feature);
        return isFeatureActive(feature.getName());
    }

    @JsonIgnore
    public String getAsJson() {
        try {
            return new CustomObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }

    /**
     * Returns the privileges granted to the user. Cannot return <code>null</code>.
     *
     * @return the privileges, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Map<String, Privilege> getAuthorities() {
        return this.authorities;
    }

    /**
     * Set the authorities/privileges granted to a user.
     */
    @Override
    public void setAuthorities(Map<String, Privilege> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getForskrivarkod() {
        return forskrivarkod;
    }

    public void setForskrivarkod(String forskrivarkod) {
        this.forskrivarkod = forskrivarkod;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    @JsonIgnore
    public List<String> getIdsOfAllVardenheter() {
        List<String> allIds = new ArrayList<>();
        for (Vardgivare v : getVardgivare()) {
            allIds.addAll(v.getHsaIds());
        }
        return allIds;
    }

    @JsonIgnore
    public List<String> getIdsOfSelectedVardenhet() {

        SelectableVardenhet selected = getValdVardenhet();

        if (selected == null) {
            return Collections.emptyList();
        }

        return selected.getHsaIds();
    }

    @JsonIgnore
    public List<String> getIdsOfSelectedVardgivare() {

        SelectableVardenhet selected = getValdVardgivare();

        if (selected == null) {
            return Collections.emptyList();
        }

        return selected.getHsaIds();
    }

    public List<String> getLegitimeradeYrkesgrupper() {
        if (legitimeradeYrkesgrupper == null) {
            legitimeradeYrkesgrupper = Collections.emptyList();
        }

        return legitimeradeYrkesgrupper;
    }

    public void setLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        this.legitimeradeYrkesgrupper = legitimeradeYrkesgrupper;
    }

    /**
     * Returns the name to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the name (never <code>null</code>)
     */
    @Override
    public String getNamn() {
        return this.namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    /**
     * Returns the personal identifier used to authenticate the user.
     *
     * @return the personal identifier
     */
    @Override
    public String getPersonId() {
        return this.personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * Returns the role granted to the user. Cannot return <code>null</code>.
     *
     * @return the role, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Map<String, Role> getRoles() {
        return this.roles;
    }

    /**
     * Set the roles granted to a user.
     */
    @Override
    public void setRoles(Map<String, Role> roles) {
        this.roles = roles;
    }

    public List<String> getSpecialiseringar() {
        if (specialiseringar == null) {
            specialiseringar = Collections.emptyList();
        }

        return specialiseringar;
    }

    public void setSpecialiseringar(List<String> specialiseringar) {
        this.specialiseringar = specialiseringar;
    }

    public List<String> getBefattningar() {
        if (befattningar == null) {
            befattningar = Collections.emptyList();
        }
        return befattningar;
    }


    public void setBefattningar(List<String> befattningar) {
        this.befattningar = befattningar;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public int getTotaltAntalVardenheter() {
        return getIdsOfAllVardenheter().size();
    }

    public SelectableVardenhet getValdVardenhet() {
        return valdVardenhet;
    }

    public void setValdVardenhet(SelectableVardenhet valdVardenhet) {
        this.valdVardenhet = valdVardenhet;
    }

    public SelectableVardenhet getValdVardgivare() {
        return valdVardgivare;
    }

    public void setValdVardgivare(SelectableVardenhet valdVardgivare) {
        this.valdVardgivare = valdVardgivare;
    }

    public List<Vardgivare> getVardgivare() {
        if (vardgivare == null) {
            vardgivare = Collections.emptyList();
        }

        return vardgivare;
    }

    public void setVardgivare(List<Vardgivare> vardgivare) {
        this.vardgivare = vardgivare;
    }




    /**
     * Determines if the user's roles contains a lakare role or not.
     * <ul>
     * The following roles are considered lakare:
     * <li>ROLE_LAKARE</li>
     * <li>ROLE_PRIVATLAKARE</li>
     * <li>ROLE_TANDLAKARE</li>
     * </ul>
     * Note: This construct smells a bit, as it's somewhat ambigous what isLakare could be interpreted as?
     * @return true if role is one the above, otherwise false
     */
    public boolean isLakare() {
        return roles.containsKey(ROLE_LAKARE) || roles.containsKey(ROLE_PRIVATLAKARE) || roles.containsKey(ROLE_TANDLAKARE);
    }

    public boolean isPrivatLakare() {
        return roles.containsKey(ROLE_PRIVATLAKARE);
    }

    public boolean isPrivatLakareAvtalGodkand() {
        return privatLakareAvtalGodkand;
    }

    public void setPrivatLakareAvtalGodkand(boolean privatLakareAvtalGodkand) {
        this.privatLakareAvtalGodkand = privatLakareAvtalGodkand;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return hsaId + " [authScheme=" + authenticationScheme + ", lakare=" + isLakare() + "]";
    }

}
