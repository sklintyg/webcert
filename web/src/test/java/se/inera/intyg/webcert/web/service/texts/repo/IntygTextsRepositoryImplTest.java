package se.inera.intyg.webcert.web.service.texts.repo;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import se.inera.intyg.webcert.web.service.texts.model.IntygTexts;

public class IntygTextsRepositoryImplTest {

    private static final String DEFAULT_INTYGSTYP = "test";
    private static final String DEFAULT_VERSION = "1.0";
    private IntygTextsRepositoryImpl repo;

    @Before
    public void setup() {
        repo = new IntygTextsRepositoryImpl();
    }

    @Test
    public void testGetLatestVersion() {
        repo.intygTexts = new HashSet<IntygTexts>() {
            {
                add(new IntygTexts("1.0", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1.1", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1.1.0.1", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1", DEFAULT_INTYGSTYP, null, null, null, null));
            }
        };
        String result = repo.getLatestVersion(DEFAULT_INTYGSTYP);
        assertEquals("should return max version", "1.1.0.1", result);
    }

    @Test
    public void testGetLatestVersionNull() {
        repo.intygTexts = new HashSet<IntygTexts>() {
            {
            }
        };
        String result = repo.getLatestVersion(DEFAULT_INTYGSTYP);
        assertEquals("should return null", null, result);
    }
    @Test
    public void testGetLatestVersionValidFromFilter() {
        repo.intygTexts = new HashSet<IntygTexts>() {
            {
                add(new IntygTexts("1.0", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1.1", DEFAULT_INTYGSTYP, LocalDate.now().minusDays(1), null, null, null));
                add(new IntygTexts("1.1.0.1", DEFAULT_INTYGSTYP, LocalDate.now().plusDays(1), null, null, null));
                add(new IntygTexts("1", DEFAULT_INTYGSTYP, null, null, null, null));
            }
        };
        String result = repo.getLatestVersion(DEFAULT_INTYGSTYP);
        assertEquals("should return max version", "1.1", result);
    }

    @Test
    public void testGetLatestVersionTypeFilter() {
        repo.intygTexts = new HashSet<IntygTexts>() {
            {
                add(new IntygTexts("1.0", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1.1", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("1.1.0.1", "wrong-type", null, null, null, null));
                add(new IntygTexts("1", DEFAULT_INTYGSTYP, null, null, null, null));
                add(new IntygTexts("2", "wrong-type", null, null, null, null));
            }
        };
        String result = repo.getLatestVersion(DEFAULT_INTYGSTYP);
        assertEquals("should return max version", "1.1", result);
    }

    @Test
    public void testGetTextsSuccessful() {
        IntygTexts testData = new IntygTexts(DEFAULT_VERSION, DEFAULT_INTYGSTYP, null, null, null, null);
        repo.intygTexts = new HashSet<IntygTexts>() {{
            add(testData);
        }};
        assertEquals("should return the IntygText in set", testData, repo.getTexts(DEFAULT_INTYGSTYP, DEFAULT_VERSION));
    }

    @Test
    public void testGetTextsNull() {
        repo.intygTexts = new HashSet<>();
        assertNull("if no version of specified type exists it should return null", repo.getTexts(DEFAULT_INTYGSTYP, DEFAULT_VERSION));
    }

    @Test
    public void testGetTextsNotCareAboutValidFrom() {
        IntygTexts testData = new IntygTexts(DEFAULT_VERSION, DEFAULT_INTYGSTYP, LocalDate.now().plusYears(3001), null, null, null);
        repo.intygTexts = new HashSet<IntygTexts>() {{
            add(testData);
        }};
        assertEquals("should return the IntygText in set", testData, repo.getTexts(DEFAULT_INTYGSTYP, DEFAULT_VERSION));
    }
}
