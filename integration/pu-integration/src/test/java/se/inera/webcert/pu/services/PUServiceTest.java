package se.inera.webcert.pu.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:PUServiceTest/test-context.xml")
public class PUServiceTest {

    @Autowired
    private PUService service;

    @Test
    public void checkExistingPersonWithFullAddress() {
        Person person = service.getPerson("19121212-1212").getPerson();
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Svensson, Storgatan 1, PL 1234", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }

    @Test
    public void checkExistingPersonWithMinimalAddress() {
        Person person = service.getPerson("20121212-1212").getPerson();
        assertEquals("Lilltolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("Storgatan 1", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }

    @Test
    public void checkExistingPersonWithMellannamn() {
        Person person = service.getPerson("19520614-2597").getPerson();
        assertEquals("Per Peter", person.getFornamn());
        assertEquals("Pärsson", person.getEfternamn());
        assertEquals("Svensson", person.getMellannamn());
    }
    
    @Test
    public void checkNonExistingPerson() {
        Person person = service.getPerson("19121212-7169").getPerson();
        assertNull(person);
    }

    @Test
    public void checkConfidentialPerson() {
        Person person = service.getPerson("19540123-2540").getPerson();
        assertEquals("Maj", person.getFornamn());
        assertEquals("Pärsson", person.getEfternamn());
        assertEquals("KUNGSGATAN 5", person.getPostadress());
        assertEquals("41234", person.getPostnummer());
        assertEquals("GÖTEBORG", person.getPostort());
        assertTrue(person.isSekretessmarkering());
    }
}