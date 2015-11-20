package se.inera.intyg.webcert.web.security;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.model.TitleCode;
import se.inera.webcert.persistence.roles.repository.TitleCodeRepository;

@RunWith(MockitoJUnitRunner.class)
public class LookupUserRoleTest {

    @InjectMocks
    private WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

    @Mock
    private TitleCodeRepository titleCodeRepository;

    @Before
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void lookupUserRoleWhenTitleIsDoctor() {
        // given
        List<String> titles = Collections.singletonList("Läkare");
        // when
        UserRole userRole = userDetailsService.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertTrue(UserRole.ROLE_LAKARE.equals(userRole));
    }

    @Test
    public void lookupUserRoleWhenMultipleTitlesAndOneIsDoctor() {
        // given
        List<String> titles = Arrays.asList("Läkare", "Barnmorska", "Sjuksköterska");
        // when
        UserRole userRole = userDetailsService.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertTrue(UserRole.ROLE_LAKARE.equals(userRole));
    }

    @Test
    public void lookupUserRoleWhenMultipleTitlesAndNoDoctor() {
        // given
        List<String> titles = Arrays.asList("Barnmorska", "Sjuksköterska");
        // when
        UserRole userRole = userDetailsService.lookupUserRoleByLegitimeradeYrkesgrupper(titles);
        // then
        assertNull(userRole);
    }

    @Test
    public void lookupUserRoleWhenTitleCodeIs204010() {
        // given
        List<String> befattningsKoder = Collections.singletonList("204010");
        // when
        UserRole userRole = userDetailsService.lookupUserRoleByBefattningskod(befattningsKoder);
        // then
        assertTrue(UserRole.ROLE_LAKARE.equals(userRole));
    }

    @Test
    public void lookupUserRoleWhenTitleCodeIsNot204010() {
        // given
        List<String> befattningsKoder = Arrays.asList("203090", "204090");
        // when
        UserRole userRole = userDetailsService.lookupUserRoleByBefattningskod(befattningsKoder);
        // then
        assertNull(userRole);
    }

    @Test
    public void lookupUserRoleByTitleCodeAndGroupPrescriptionCode() {
        // given
        List<String> befattningsKoder = Arrays.asList("204010", "203090", "204090");
        List<String> gruppforskrivarKoder = Arrays.asList("9300005", "9100009");

        UserRole[][] userRoleMatrix = new UserRole[3][2];

        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("204010", "9300005")).thenReturn(null);
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("204010", "9100009")).thenReturn(null);
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("203090", "9300005")).thenReturn(returnTitleCode("203090", "9300005", UserRole.ROLE_LAKARE));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("203090", "9100009")).thenReturn(returnTitleCode("203090", "9100009", UserRole.ROLE_LAKARE));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("204090", "9300005")).thenReturn(null);
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode("204090", "9100009")).thenReturn(returnTitleCode("204090", "9100009", UserRole.ROLE_LAKARE));

        // when
        for (int i = 0; i < befattningsKoder.size(); i++) {
            for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
                UserRole userRole = userDetailsService.lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningsKoder.get(i), gruppforskrivarKoder.get(j));
                userRoleMatrix[i][j] = userRole;
            }
        }

        // then
        for (int i = 0; i < befattningsKoder.size(); i++) {
            for (int j = 0; j < gruppforskrivarKoder.size(); j++) {
                if ((i == 0) && ((j == 0) || (j == 1))) {
                    assertNull(userRoleMatrix[i][j]);
                } else if ((i == 2) && (j == 0)) {
                    assertNull(userRoleMatrix[i][j]);
                } else {
                    assertTrue(UserRole.ROLE_LAKARE.equals(userRoleMatrix[i][j]));
                }
            }
        }
    }

    private TitleCode returnTitleCode(String befattningsKod, String gruppforskrivarKod, UserRole userRole) {
        return new TitleCode(befattningsKod, gruppforskrivarKod, new Role(userRole.name(), userRole.text()));
    }

}
