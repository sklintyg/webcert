package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import se.inera.intyg.webcert.integration.fmb.services.FmbService;
import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbCallType;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

public class FmbApiControllerTest {

    @InjectMocks
    private FmbApiController controller;

    @Mock
    private FmbRepository fmbRepository;

    @Mock
    private FmbService fmbService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetFmbForIcd10HandlesNull() throws Exception {
        Response response = controller.getFmbForIcd10(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetFmbForIcd10HandlesEmptyInput() throws Exception {
        Response response = controller.getFmbForIcd10("");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetFmbForIcd10IsReturningCorrectIcdCode() throws Exception {
        // Given
        String icd10 = "asdf";

        // When
        FmbResponse response = (FmbResponse) controller.getFmbForIcd10(icd10).getEntity();

        // Then
        assertEquals(icd10.toUpperCase(), response.getIcd10Code());
    }

    @Test
    public void testGetFmbForIcd10HandlesNullResponseFromRepositoryCorrectAndTriesToUpdateFmbData() throws Exception {
        // Given
        Mockito.doReturn(null).when(fmbRepository).findByIcd10AndTyp(anyString(), any(FmbType.class));

        // When
        FmbResponse response = (FmbResponse) controller.getFmbForIcd10("A10").getEntity();

        // Then
        assertEquals(0, response.getForms().size());
    }

    @Test
    public void testGetFmbForIcd10HandlesAddsTextForOneRow() throws Exception {
        // Given
        ArrayList<Fmb> fmbs = new ArrayList<>();
        String text = "testtext";
        fmbs.add(new Fmb("A10", FmbType.FALT4, FmbCallType.FMB, text, "1"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByIcd10AndTyp(anyString(), any(FmbType.class));

        // When
        FmbResponse response = (FmbResponse) controller.getFmbForIcd10("A10").getEntity();

        // Then
        assertEquals(FmbFormName.values().length, response.getForms().size());

        List<FmbForm> forms = response.getForms();
        for (FmbForm form : forms) {
            List<FmbContent> content = form.getContent();
            for (FmbContent fmbContent : content) {
                assertEquals(text, fmbContent.getText());
                assertNull(fmbContent.getList());
            }
        }
    }

    @Test
    public void testGetFmbForIcd10HandlesAddsListOfTextsForSeveralRows() throws Exception {
        // Given
        ArrayList<Fmb> fmbs = new ArrayList<>();
        String testtext = "testtext";
        fmbs.add(new Fmb("A10", FmbType.FALT4, FmbCallType.FMB, testtext, "1"));
        fmbs.add(new Fmb("A10", FmbType.FALT4, FmbCallType.FMB, testtext, "1"));
        Mockito.doReturn(fmbs).when(fmbRepository).findByIcd10AndTyp(anyString(), any(FmbType.class));

        // When
        FmbResponse response = (FmbResponse) controller.getFmbForIcd10("A10").getEntity();

        // Then
        assertEquals(FmbFormName.values().length, response.getForms().size());

        List<FmbForm> forms = response.getForms();
        for (FmbForm form : forms) {
            List<FmbContent> content = form.getContent();
            for (FmbContent fmbContent : content) {
                assertNull(fmbContent.getText());
                List<String> texts = fmbContent.getList();
                for (String text : texts) {
                    assertEquals(testtext, text);
                }
            }
        }
    }

}
