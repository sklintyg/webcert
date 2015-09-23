package se.inera.webcert.service.user.dto;

import static se.inera.webcert.common.security.authority.UserRole.ROLE_LAKARE;
import static se.inera.webcert.common.security.authority.UserRole.ROLE_LAKARE_DJUPINTEGRERAD;
import static se.inera.webcert.common.security.authority.UserRole.ROLE_LAKARE_UTHOPP;
import static se.inera.webcert.common.security.authority.UserRole.ROLE_PRIVATLAKARE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.common.model.UserDetails;
import se.inera.webcert.common.security.authority.SimpleGrantedAuthority;
import se.inera.webcert.hsa.model.AuthenticationMethod;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.Vardgivare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author andreaskaltenbach
 */
public class WebCertUser implements UserDetails {

    private static final long serialVersionUID = -2624303818412468774L;

    // private boolean lakare;
    // private boolean privatLakare;
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

    private Map<String, String> roles;
    private Map<String, String> authorities;

    private AuthenticationMethod authenticationMethod; // TODO - temporary hack. BANKID, NETID

    /**
     * Construct the <code>WebCertUser</code> with its authority details.
     *
     * @param role
     *            the role that should be granted to the caller. Not null.
     * @param authorities
     *            the privileges that should be granted to the caller. Not null.
     */
    public WebCertUser(GrantedAuthority role, Collection<? extends GrantedAuthority> authorities) {
        this.roles = getGrantedRoles(role);
        this.authorities = getGrantedAuthorities(authorities);
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
            aktivaFunktioner = new HashSet<String>();
        }

        return aktivaFunktioner;
    }

    public void setAktivaFunktioner(Set<String> aktivaFunktioner) {
        this.aktivaFunktioner = aktivaFunktioner;
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
    public Map<String, String> getAuthorities() {
        return this.authorities;
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
    public Map<String, String> getRoles() {
        return this.roles;
    }

    /**
     * Set the roles granted to a user.
     *
     * @param roles
     */
    @Override
    public void setRoles(Map<String, String> roles) {
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
     * @param role the role to check
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
     * @param roles The roles to check.
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

    public boolean isLakare() {
        return roles.containsKey(ROLE_LAKARE.name()) || roles.containsKey(ROLE_LAKARE_DJUPINTEGRERAD.name())
                || roles.containsKey(ROLE_LAKARE_UTHOPP.name()) || roles.containsKey(ROLE_PRIVATLAKARE.name());
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

    @JsonIgnore
    @Override
    public String toString() {
        return hsaId + " [authScheme=" + authenticationScheme + ", lakare=" + isLakare() + "]";
    }


    // ~ Private scope
    // ============================================================================

    /*
     * Ensure array iteration order is predictable
     */
    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        SortedSet<GrantedAuthority> sortedAuthorities =
               new TreeSet<GrantedAuthority>(new AuthorityComparator());

        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }

        return sortedAuthorities;
    }

    private SimpleGrantedAuthority castGrantedAuthority(GrantedAuthority authority) {
        SimpleGrantedAuthority sga;

        if (authority instanceof SimpleGrantedAuthority) {
            sga = (SimpleGrantedAuthority) authority;
        } else {
            sga = new SimpleGrantedAuthority(authority.getAuthority(), "");
        }

        return sga;
    }

    private Map<String, String> getGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Map<String, String> authorityMap = new HashMap<>();

        SortedSet<GrantedAuthority> sortedSet = sortAuthorities(authorities);
        for (GrantedAuthority ga : sortedSet) {
            SimpleGrantedAuthority sga = castGrantedAuthority(ga);
            authorityMap.put(sga.getAuthority(), sga.getDescription());
        }

        return Collections.unmodifiableMap(authorityMap);
    }

    private Map<String, String> getGrantedRoles(GrantedAuthority role) {
        SimpleGrantedAuthority sga = castGrantedAuthority(role);

        Map<String, String> roles = new HashMap<>();
        roles.put(sga.getAuthority(), sga.getDescription());

        return Collections.unmodifiableMap(roles);
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to the set.
            // If the authority is null, it is a custom authority and should precede others.
            if (g2.getAuthority() == null) {
                return -1;
            }

            if (g1.getAuthority() == null) {
                return 1;
            }

            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }

}
