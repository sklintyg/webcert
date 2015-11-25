package se.inera.intyg.webcert.web.service.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.common.model.UserDetails;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se.inera.intyg.webcert.common.common.security.authority.UserRole.ROLE_LAKARE;
import static se.inera.intyg.webcert.common.common.security.authority.UserRole.ROLE_LAKARE_DJUPINTEGRERAD;
import static se.inera.intyg.webcert.common.common.security.authority.UserRole.ROLE_LAKARE_UTHOPP;
import static se.inera.intyg.webcert.common.common.security.authority.UserRole.ROLE_PRIVATLAKARE;
import static se.inera.intyg.webcert.common.common.security.authority.UserRole.ROLE_TANDLAKARE;

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
    private List<String> specialiseringar;
    private List<String> legitimeradeYrkesgrupper;

    private Set<String> aktivaFunktioner;

    private SelectableVardenhet valdVardenhet;
    private SelectableVardenhet valdVardgivare;

    private Map<String, UserRole> roles;
    private Map<String, UserPrivilege> authorities;

    private AuthenticationMethod authenticationMethod;

    /** The sole constructor. */
    public WebCertUser() {
    }

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

    public Set<String> getAktivaFunktioner() {
        if (aktivaFunktioner == null) {
            aktivaFunktioner = new HashSet<>();
        }

        return aktivaFunktioner;
    }

    public void setAktivaFunktioner(Set<String> aktivaFunktioner) {
        this.aktivaFunktioner = aktivaFunktioner;
    }

    public boolean isFeatureActive(WebcertFeature feature) {
        if (aktivaFunktioner == null) {
            return false;
        }
        final String featureName = feature.getName();
        return aktivaFunktioner.contains(featureName);
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
    public Map<String, UserPrivilege> getAuthorities() {
        return this.authorities;
    }

    /**
     * Set the authorities/privileges granted to a user.
     */
    @Override
    public void setAuthorities(Map<String, UserPrivilege> authorities) {
        this.authorities = authorities;
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
    public Map<String, UserRole> getRoles() {
        return this.roles;
    }

    /**
     * Set the roles granted to a user.
     */
    @Override
    public void setRoles(Map<String, UserRole> roles) {
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

    public boolean hasAktivFunktion(String aktivFunktion) {
        return getAktivaFunktioner().contains(aktivFunktion);
    }

    /**
     * Checks if a user has been granted a specific role.
     *
     * @param role
     *            the role to check
     * @return true if user has the role, otherwise false
     */
    public boolean hasRole(String role) {
        if (role == null) {
            return false;
        }

        return getRoles().containsKey(role);
    }

    /**
     * Checks if a user has been granted a specific role.
     * Method will return true if one role matches user's granted roles.
     *
     * @param roles
     *            The roles to check.
     * @return true If user has one of the roles, otherwise false.
     */
    public boolean hasRole(String[] roles) {
        if (roles == null) {
            return false;
        }

        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the user's authorities map contains the specified
     * {@link se.inera.intyg.webcert.common.common.security.authority.UserPrivilege}.
     */
    public boolean hasPrivilege(UserPrivilege privilege) {
        if (authorities == null) {
            return false;
        }
        return authorities.containsKey(privilege.name());
    }

    /**
     * Determines if the user's roles contains a lakare role or not.
     * <ul>
     * The following roles are considered lakare:
     * <li>ROLE_LAKARE</li>
     * <li>ROLE_LAKARE_DJUPINTEGRERAD</li>
     * <li>ROLE_LAKARE_UTHOPP</li>
     * <li>ROLE_PRIVATLAKARE</li>
     * <li>ROLE_TANDLAKARE</li>
     * </ul>
     * Note: This construct smells a bit, as it's somewhat ambigous what isLakare could be interpreted as?
     * @return true if role is one the above, otherwise false
     */
    public boolean isLakare() {
        return roles.containsKey(ROLE_LAKARE.name()) || roles.containsKey(ROLE_LAKARE_DJUPINTEGRERAD.name())
                || roles.containsKey(ROLE_LAKARE_UTHOPP.name()) || roles.containsKey(ROLE_PRIVATLAKARE.name())
                || roles.containsKey(ROLE_TANDLAKARE.name());
    }

    public boolean isPrivatLakare() {
        return roles.containsKey(ROLE_PRIVATLAKARE.name());
    }

    public boolean isPrivatLakareAvtalGodkand() {
        return privatLakareAvtalGodkand;
    }

    public void setPrivatLakareAvtalGodkand(boolean privatLakareAvtalGodkand) {
        this.privatLakareAvtalGodkand = privatLakareAvtalGodkand;
    }

    public boolean isRoleUthopp() {
        return roles.containsValue(UserRole.ROLE_LAKARE_UTHOPP) || roles.containsValue(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return hsaId + " [authScheme=" + authenticationScheme + ", lakare=" + isLakare() + "]";
    }

    /**
     * Iterates over all roles and flatmaps distinct intygstyper into a set of strings.
     */
    @JsonIgnore
    public Set<String> getIntygsTyper() {
        Set<String> set = new HashSet<>();
        if (roles == null || roles.isEmpty()) {
            return set;
        }
        for (Map.Entry<String, UserRole> entry : roles.entrySet()) {
            if (entry.getValue() != null) {
                set.addAll(entry.getValue().getAuthorizedIntygsTyper());
            }
        }
        return set;
    }

}
