package se.inera.webcert.pu.services;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.population.residentmaster.v1.JaNejTYPE;
import se.inera.population.residentmaster.v1.LookupResidentForFullProfileResponderInterface;
import se.inera.population.residentmaster.v1.NamnTYPE;
import se.inera.population.residentmaster.v1.ResidentType;
import se.inera.population.residentmaster.v1.SvenskAdressTYPE;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookUpSpecificationType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileResponseType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileType;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;

import com.google.common.annotations.VisibleForTesting;

public class PUServiceImpl implements PUService {

    private static final Logger LOG = LoggerFactory.getLogger(PUServiceImpl.class);

    @Autowired
    private LookupResidentForFullProfileResponderInterface service;

    @Value("${putjanst.logicaladdress}")
    private String logicaladdress;

    @Override
    @Cacheable(value = "personCache",
               key = "#personId",
               unless = "#result.status == T(se.inera.webcert.pu.model.PersonSvar$Status).ERROR")
    public PersonSvar getPerson(Personnummer personId) {

        LOG.debug("Looking up person '{}'", personId.getPnrHash());
        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.setLookUpSpecification(new LookUpSpecificationType());
        parameters.getPersonId().add(personId.getPersonnummerWithoutDash());
        try {
            LookupResidentForFullProfileResponseType response = service.lookupResidentForFullProfile(logicaladdress, parameters);
            if (response.getResident().isEmpty()) {
                LOG.warn("No person '{}' found", personId.getPnrHash());
                return new PersonSvar(null, PersonSvar.Status.NOT_FOUND);
            }

            ResidentType resident = response.getResident().get(0);

            NamnTYPE namn = resident.getPersonpost().getNamn();

            SvenskAdressTYPE adress = resident.getPersonpost().getFolkbokforingsadress();

            String adressRader = buildAdress(adress);
            Person person = new Person(personId, resident.getSekretessmarkering() == JaNejTYPE.J, namn.getFornamn(),
                    namn.getMellannamn(), namn.getEfternamn(), adressRader, adress.getPostNr(), adress.getPostort());
            LOG.debug("Person '{}' found", personId.getPnrHash());

            return new PersonSvar(person, PersonSvar.Status.FOUND);
        } catch (SOAPFaultException e) {
            LOG.warn("Error occured, no person '{}' found", personId.getPnrHash());
            return new PersonSvar(null, PersonSvar.Status.ERROR);
        }
    }

    @Override
    @VisibleForTesting
    @CacheEvict(value = "personCache", allEntries = true)
    public void clearCache() {
        LOG.debug("personCache cleared");
    }

    private String buildAdress(SvenskAdressTYPE adress) {
        return joinIgnoreNulls(", ", adress.getCareOf(), adress.getUtdelningsadress1(), adress.getUtdelningsadress2());
    }

    private String joinIgnoreNulls(String separator, String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null) {
                if (builder.length() > 0) {
                    builder.append(separator);
                }
                builder.append(value);
            }
        }
        return builder.toString();
    }
}
