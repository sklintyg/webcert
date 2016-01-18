/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.integration.pu.services;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookUpSpecificationType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileResponseType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v1.LookupResidentForFullProfileType;
import se.riv.population.residentmaster.lookupresidentforfullprofileresponder.v11.LookupResidentForFullProfileResponderInterface;
import se.riv.population.residentmaster.types.v1.JaNejTYPE;
import se.riv.population.residentmaster.types.v1.NamnTYPE;
import se.riv.population.residentmaster.types.v1.ResidentType;
import se.riv.population.residentmaster.types.v1.SvenskAdressTYPE;

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
               unless = "#result.status == T(se.inera.intyg.webcert.integration.pu.model.PersonSvar$Status).ERROR")
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
