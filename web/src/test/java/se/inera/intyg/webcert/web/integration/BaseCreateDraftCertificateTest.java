package se.inera.intyg.webcert.web.integration;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-09-27.
 */
public abstract class BaseCreateDraftCertificateTest {

    protected static final String FK7263 = "fk7263";
    protected static final String TSBAS = "ts-bas";

    protected static final String LOGICAL_ADDR = "1234567890";

    protected static final String USER_HSAID = "SE1234567890";
    protected static final String UNIT_HSAID = "SE0987654321";
    protected static final String CAREGIVER_HSAID = "SE0000112233";

    protected static final String UTKAST_ID = "abc123";
    protected static final String UTKAST_VERSION = "1";
    protected static final String UTKAST_TYPE = "fk7263";
    protected static final String UTKAST_JSON = "A bit of text representing json";

    public static final String FULLSTANDIGT_NAMN = "Abel Baker";
    public static final String INVARTES_MEDICIN = "Invärtes medicin";
    public static final String TITLE_CODE = "203010";
    public static final String TITLE_NAME = "Läkare";
    public static final String ALLMAN_MEDICIN = "Allmänmedicin";

    @Mock
    protected WebcertUserDetailsService webcertUserDetailsService;

    public void setup() {
        when(webcertUserDetailsService.loadUserByHsaId(USER_HSAID)).thenReturn(buildWebCertUser());
    }

    protected WebCertUser buildWebCertUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT));
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + FK7263,
                        WebcertFeature.HANTERA_INTYGSUTKAST.getName() + "." + TSBAS));
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());
        user.setBefattningar(Arrays.asList(TITLE_CODE));
        user.setSpecialiseringar(Arrays.asList(ALLMAN_MEDICIN, INVARTES_MEDICIN));

        user.setVardgivare(Arrays.asList(createVardgivare()));
        return user;
    }

    private Privilege createPrivilege(String privilege) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(WebCertUserOriginType.DJUPINTEGRATION.name());
        requestOrigin.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(Arrays.asList(FK7263, TSBAS));
        return priv;
    }

    private Vardenhet createVardenhet(Vardgivare vardgivare) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setId(UNIT_HSAID);
        vardenhet.setNamn("Vardenheten");
        vardenhet.setVardgivareHsaId(vardgivare.getId());
        vardenhet.setArbetsplatskod("12345");
        vardenhet.setPostadress("Gatan 1");
        vardenhet.setPostnummer("54321");
        vardenhet.setPostort("Vardmåla");
        vardenhet.setTelefonnummer("123-456789");
        return vardenhet;
    }

    private Vardgivare createVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId(CAREGIVER_HSAID);
        vardgivare.setNamn("Vardgivaren");
        vardgivare.setVardenheter(Arrays.asList(createVardenhet(vardgivare)));
        return vardgivare;
    }
}
