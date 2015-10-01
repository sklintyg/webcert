package se.inera.webcert.security;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.webcert.common.security.authority.UserRole;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LookupUserRoleTest {

        private static final String PERSONAL_HSA_ID = "TST5565594230-106J";
        private static final String ENHET_HSA_ID = "IFV1239877878-103H";
        private static final String HEAD_DOCTOR = "Överläkare";

        @InjectMocks
        private WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

        @Test
        public void lookupUserRoleByOnlyTitle() {
            // given
            List<String> titles = Arrays.asList(new String[]{"Läkare"});
            // when
            UserRole userRole = userDetailsService.lookupUserRoleByTitel(titles);
            // then
            assertTrue(UserRole.ROLE_LAKARE.equals(userRole));
        }

        @Test
        public void lookupUserRoleByMultipleTitles() {
            // given
            List<String> titles = Arrays.asList(new String[] {"Läkare", "Barnmorska", "Sjuksköterska"});
            // when
            UserRole userRole = userDetailsService.lookupUserRoleByTitel(titles);
            // then
            assertTrue(UserRole.ROLE_LAKARE.equals(userRole));
        }

        @Test
        public void lookupUserRoleByMultipleTitlesAndNoDoctor() {
            // given
            List<String> titles = Arrays.asList(new String[] {"Barnmorska", "Sjuksköterska"});
            // when
            UserRole userRole = userDetailsService.lookupUserRoleByTitel(titles);
            // then
            assertTrue(userRole == null);
        }

    }
