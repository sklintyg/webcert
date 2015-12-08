/*
 * Inera Medcert - Sjukintygsapplikation
 *
 * Copyright (C) 2010-2011 Inera AB (http://www.inera.se)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package se.inera.intyg.webcert.integration.hsa.ifv.webcert.spi.authorization.impl.hsaws;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.inera.intyg.webcert.integration.hsa.client.OrganizationUnitService;
import se.inera.intyg.webcert.integration.hsa.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.riv.itintegration.monitoring.pingforconfigurationresponder.v1.PingForConfigurationResponseType;


/**
 * @author Pehr Assarsson
 *
 */
public class HSAWebServiceCallsTestClientJUnit {

    private ApplicationContext ctx;
   // private HSAWebServiceCalls client;
    private OrganizationUnitService organizationUnitService;


    @Before
    public void init() {
        ctx = new ClassPathXmlApplicationContext(new String[] {"HSAWebServiceCallsTest-applicationContext.xml", "hsa-services-config.xml"});
        organizationUnitService = (OrganizationUnitService) ctx.getBean("organizationUnitService");
    }

    @Test
    public void testHSAPing() throws Exception {
        PingForConfigurationResponseType ping = organizationUnitService.ping();
        System.out.println(ping.getPingDateTime());
        //client.callPing();

      //  GetHsaUnitResponseType response = client.callGetHsaunit("IFV1239877878-103F");
        //System.out.println(response);
    }

}
