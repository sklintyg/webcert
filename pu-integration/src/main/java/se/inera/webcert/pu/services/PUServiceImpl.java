package se.inera.webcert.pu.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.inera.population.residentmaster.v1.LookupResidentForFullProfileResponderInterface;
import se.inera.population.residentmaster.v1.NamnTYPE;
import se.inera.population.residentmaster.v1.ResidentType;
import se.inera.population.residentmaster.v1.SvenskAdressTYPE;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookUpSpecificationType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileResponseType;
import se.inera.population.residentmaster.v1.lookupresidentforfullprofile.LookupResidentForFullProfileType;
import se.inera.webcert.pu.model.Person;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.Arrays;

public class PUServiceImpl implements PUService {

    @Autowired
    private LookupResidentForFullProfileResponderInterface service;

    @Value("${putjanst.logicaladdress}")
    private String logicaladdress;

    @Override
    public Person getPerson(String personId) {
        String normalizedId = normalizeId(personId);

        LookupResidentForFullProfileType parameters = new LookupResidentForFullProfileType();
        parameters.setLookUpSpecification(new LookUpSpecificationType());
        parameters.getPersonId().add(normalizedId);
        try {
            LookupResidentForFullProfileResponseType response = service.lookupResidentForFullProfile(logicaladdress, parameters);
            ResidentType resident = response.getResident().get(0);
            NamnTYPE namn = resident.getPersonpost().getNamn();

            SvenskAdressTYPE adress = resident.getPersonpost().getFolkbokforingsadress();

            String adressRader = buildAdress(adress);
            Person person = new Person(namn.getFornamn(), namn.getEfternamn(), adressRader, adress.getPostNr(), adress.getPostort());
            return person;
        } catch (SOAPFaultException e) {
            throw e;
        }
    }

    private String normalizeId(String personId) {
        return personId.substring(0,8) + personId.substring(9);
    }

    private String buildAdress(SvenskAdressTYPE adress) {
        return joinIgnoreNulls(",", adress.getCareOf(), adress.getUtdelningsadress1(), adress.getUtdelningsadress2());
    }

    private String joinIgnoreNulls(String separator, String...values) {
        StringBuilder builder = new StringBuilder();
        for (String value: values) {
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
