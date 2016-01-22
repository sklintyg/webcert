package se.inera.intyg.webcert.web.service.texts;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.service.texts.repo.IntygTextsRepository;

@RunWith(MockitoJUnitRunner.class)
public class IntygTextsServiceImplTest {

    @Mock
    private IntygTextsRepository repo;

    @Mock
    private CustomObjectMapper mapper;

    @InjectMocks
    private IntygTextsServiceImpl service = new IntygTextsServiceImpl();

    @Test
    public void testGetVersion() {
        when(repo.getLatestVersion(any(String.class))).thenReturn("1.0");
        String result = service.getLatestVersion("LISU");
        verify(repo, times(1)).getLatestVersion("LISU");
        assertEquals("result should be what repo returns", result, "1.0");
    }

    @Test
    public void testGetVersionNull() {
        when(repo.getLatestVersion(any(String.class))).thenReturn(null);
        String result = service.getLatestVersion("LISU");
        verify(repo, times(1)).getLatestVersion("LISU");
        assertEquals("result should be what repo returns", result, null);
    }

    @Test
    public void testGetTexts() throws JsonProcessingException {
        when(repo.getTexts(any(String.class), any(String.class))).thenReturn(null);
        when(mapper.writeValueAsString(any())).thenReturn("null");
        String result = service.getIntygTexts("LISU", "0.9");
        verify(repo, times(1)).getTexts("LISU", "0.9");
        verify(mapper, times(1)).writeValueAsString(null);
        assertEquals("result should be what mapper returns", result, "null");
    }
}
