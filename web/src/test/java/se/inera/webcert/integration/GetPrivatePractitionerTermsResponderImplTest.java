package se.inera.webcert.integration;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.format.datetime.DateTimeFormatAnnotationFormatterFactory;
import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-08-06.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetPrivatePractitionerTermsResponderImplTest {

    private static final String AVTAL_TEXT = "Avtalstext";
    private static final String AVTAL_DATE = "2015-09-30T14:24:00.000";

    @Mock
    AvtalService avtalService;

    @InjectMocks
    GetPrivatePractitionerTermsResponderImpl testee;

    @Test
    public void testGetAvtal() {
        when(avtalService.getLatestAvtal()).thenReturn(buildAvtal(1));
        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();
        GetPrivatePractitionerTermsResponseType response = testee.getPrivatePractitionerTerms("", request);
        assertEquals(AVTAL_TEXT, response.getAvtal().getAvtalText());
        assertEquals(AVTAL_DATE, response.getAvtal().getAvtalVersionDatum().toString());
        assertEquals(1, response.getAvtal().getAvtalVersion());
    }

    @Test
    public void testGetAvtalNoAvtalExists() {
        when(avtalService.getLatestAvtal()).thenReturn(null);
        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();
        GetPrivatePractitionerTermsResponseType response = testee.getPrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    private Avtal buildAvtal(int version) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(version);
        avtal.setAvtalText(AVTAL_TEXT);
        avtal.setVersionDatum(LocalDateTime.parse(AVTAL_DATE));
        return avtal;
    }
}
