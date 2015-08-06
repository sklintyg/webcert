package se.inera.webcert.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionertermsresponder.v1.ApprovePrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionertermsresponder.v1.ApprovePrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.terms.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalGodkannandeType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-08-06.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApprovePrivatePractitionerTermsResponderImplTest {

    private static final String AVTAL_TEXT = "Avtalstext";
    private static final String USER_ID = "19121212-1212";

    @Mock
    AvtalService avtalService;

    @InjectMocks
    ApprovePrivatePractitionerTermsResponderImpl testee;

    @Test
    public void testApproveAvtal() {
        when(avtalService.getLatestAvtal()).thenReturn(buildAvtal(1));

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();
        AvtalGodkannandeType avtalGodkannande = new AvtalGodkannandeType();
        avtalGodkannande.setAvtalVersion(1);
        PersonId personIdValue = new PersonId();
        personIdValue.setExtension(USER_ID);
        avtalGodkannande.setPersonId(personIdValue);
        request.setAvtalGodkannande(avtalGodkannande);

        ApprovePrivatePractitionerTermsResponseType response = testee.approvePrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.OK, response.getResultCode());
    }

    @Test
    public void testApproveAvtalFailsWhenAvtalServiceThrowsException() {
        doThrow(new RuntimeException("Some exception that can occur")).when(avtalService).approveLatestAvtal(USER_ID);

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();
        AvtalGodkannandeType avtalGodkannande = new AvtalGodkannandeType();
        avtalGodkannande.setAvtalVersion(1);
        PersonId personIdValue = new PersonId();
        personIdValue.setExtension(USER_ID);
        avtalGodkannande.setPersonId(personIdValue);
        request.setAvtalGodkannande(avtalGodkannande);

        ApprovePrivatePractitionerTermsResponseType response = testee.approvePrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    @Test
    public void testApproveAvtalFailsWithMissingAvtalGodkannande() {

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();

        ApprovePrivatePractitionerTermsResponseType response = testee.approvePrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    @Test
    public void testApproveAvtalFailsWithMissingPersonId() {

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();
        AvtalGodkannandeType avtalGodkannande = new AvtalGodkannandeType();
        avtalGodkannande.setAvtalVersion(1);
        avtalGodkannande.setPersonId(null);
        request.setAvtalGodkannande(avtalGodkannande);

        ApprovePrivatePractitionerTermsResponseType response = testee.approvePrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    @Test
    public void testApproveAvtalFailsWithSubZeroAvtalVersion() {

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();
        AvtalGodkannandeType avtalGodkannande = new AvtalGodkannandeType();
        avtalGodkannande.setAvtalVersion(-1);
        PersonId personIdValue = new PersonId();
        personIdValue.setExtension(USER_ID);
        avtalGodkannande.setPersonId(personIdValue);

        request.setAvtalGodkannande(avtalGodkannande);

        ApprovePrivatePractitionerTermsResponseType response = testee.approvePrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    private Avtal buildAvtal(int version) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(version);
        avtal.setAvtalText(AVTAL_TEXT);
        return avtal;
    }
}
