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
package se.inera.ifv.webcert.spi.authorization.impl.hsaws;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;


/**
 * @author Pehr Assarsson
 *
 */
public class HSAWebServiceCallsTestClientJUnit {

    ApplicationContext ctx;
    HSAWebServiceCalls client;


    @Before
    public void init(){
        ctx = new ClassPathXmlApplicationContext(new String[] { "HSAWebServiceCallsTest-applicationContext.xml", "services-config.xml"});
        client = (HSAWebServiceCalls)ctx.getBean("wsCalls");
    }

    @Test
    @Ignore
    public void testHSAPing() throws Exception{
        client.callPing();
    }

}
