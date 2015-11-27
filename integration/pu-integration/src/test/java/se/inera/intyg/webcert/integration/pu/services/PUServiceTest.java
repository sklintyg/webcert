package se.inera.intyg.webcert.integration.pu.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookUpSpecificationType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileResponseType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v11.LookupResidentForFullProfileResponderInterface;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:PUServiceTest/test-context.xml")
public class PUServiceTest {

    @Autowired
    private PUService service;

    @Autowired
    private LookupResidentForFullProfileResponderInterface residentService;

    @Before
    public void setup() {
        service.clearCache();
    }

    @Test
    public void checkExistingPersonWithFullAddress() {
        Person person = service.getPerson(new Personnummer("19121212-1212")).getPerson();
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }

    @Test
    public void checkExistingPersonWithMinimalAddress() {
        Person person = service.getPerson(new Personnummer("20121212-1212")).getPerson();
        assertEquals("Lilltolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Storgatan 1", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }

    @Test
    public void checkExistingPersonWithMellannamn() {
        Person person = service.getPerson(new Personnummer("19520614-2597")).getPerson();
        assertEquals("Per Peter", person.getFornamn());
        assertEquals("Pärsson", person.getEfternamn());
        assertEquals("Svensson", person.getMellannamn());
    }
    
    @Test
    public void checkNonExistingPerson() {
        Person person = service.getPerson(new Personnummer("19121212-7169")).getPerson();
        assertNull(person);
    }

    @Test
    public void checkConfidentialPerson() {
        Person person = service.getPerson(new Personnummer("19540123-2540")).getPerson();
        assertEquals("Maj", person.getFornamn());
        assertEquals("Pärsson", person.getEfternamn());
        assertEquals("KUNGSGATAN 5", person.getPostadress());
        assertEquals("41234", person.getPostnummer());
        assertEquals("GÖTEBORG", person.getPostort());
        assertTrue(person.isSekretessmarkering());
    }

    @Test
    public void checkCachedPerson() throws Exception {
        String logicalAddress = "${putjanst.logicaladdress}";

        // Create mock
        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.setLookUpSpecification(new LookUpSpecificationType());
        parameters.getPersonId().add("191212121212");

        LookupResidentForFullProfileType parameters2 = new LookupResidentForFullProfileType();
        parameters2.setLookUpSpecification(new LookUpSpecificationType());
        parameters2.getPersonId().add("191212121212");

        System.err.println("Are they equal: " + parameters.equals(parameters2));

        LookupResidentForFullProfileResponseType response = residentService.lookupResidentForFullProfile(logicalAddress, parameters);
        LookupResidentForFullProfileResponderInterface mockResidentService = mock(LookupResidentForFullProfileResponderInterface.class);

        when(mockResidentService.lookupResidentForFullProfile(logicalAddress, parameters)).thenReturn(response);
        ReflectionTestUtils.setField(((Advised) service).getTargetSource().getTarget(), "service", mockResidentService);

        // First request should call the lookup service
        Person person = service.getPerson(new Personnummer("19121212-1212")).getPerson();
        verify(mockResidentService).lookupResidentForFullProfile(logicalAddress, parameters);
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());

        // This request should be cached
        person = service.getPerson(new Personnummer("19121212-1212")).getPerson();
        // lookupResidentForFullProfile should still only be called once
        verify(mockResidentService).lookupResidentForFullProfile(logicalAddress, parameters);
        // person information should still be the same
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());

        ReflectionTestUtils.setField(((Advised)service).getTargetSource().getTarget(), "service", residentService);
    }

    @Test
    public void dontCachePersonLookupError() throws Exception {
        String logicalAddress = "${putjanst.logicaladdress}";

        // Create mock
        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.setLookUpSpecification(new LookUpSpecificationType());
        parameters.getPersonId().add("191212121212");

        LookupResidentForFullProfileResponseType response = residentService.lookupResidentForFullProfile(logicalAddress, parameters);
        LookupResidentForFullProfileResponderInterface mockResidentService = mock(LookupResidentForFullProfileResponderInterface.class);
        SOAPFaultException soapException = null;
        try {
            soapException = new SOAPFaultException(SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault());
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        when(mockResidentService.lookupResidentForFullProfile(logicalAddress, parameters))
                .thenThrow(soapException)
                .thenThrow(soapException)
                .thenReturn(response);
        ReflectionTestUtils.setField(((Advised) service).getTargetSource().getTarget(), "service", mockResidentService);

        // First request should call the lookup service
        PersonSvar personsvar = service.getPerson(new Personnummer("19121212-1212"));
        verify(mockResidentService).lookupResidentForFullProfile(logicalAddress, parameters);
        assertEquals(personsvar.getStatus(), PersonSvar.Status.ERROR);
        assertNull(personsvar.getPerson());

        // since first request returned an error this request should call the lookup service again
        personsvar = service.getPerson(new Personnummer("19121212-1212"));
        // lookupResidentForFullProfile should still only be called once
        verify(mockResidentService, times(2)).lookupResidentForFullProfile(logicalAddress, parameters);
        assertEquals(personsvar.getStatus(), PersonSvar.Status.ERROR);
        assertNull(personsvar.getPerson());

        // the third attempt will go through and should return real data
        personsvar = service.getPerson(new Personnummer("19121212-1212"));
        // lookupResidentForFullProfile should still only be called once
        verify(mockResidentService, times(3)).lookupResidentForFullProfile(logicalAddress, parameters);
        assertEquals(personsvar.getStatus(), PersonSvar.Status.FOUND);
        Person person = personsvar.getPerson();
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());

        // the fourth attempt will return cached data, lookupResidentForFullProfile should only be called 3 times total
        personsvar = service.getPerson(new Personnummer("19121212-1212"));
        // lookupResidentForFullProfile should still only be called once
        verify(mockResidentService, times(3)).lookupResidentForFullProfile(logicalAddress, parameters);
        assertEquals(personsvar.getStatus(), PersonSvar.Status.FOUND);
        person = personsvar.getPerson();
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());

        ReflectionTestUtils.setField(((Advised) service).getTargetSource().getTarget(), "service", residentService);
    }
}
