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
package se.inera.ifv.webcert.spi.authorization.impl;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.hsaws.v3.HsaWsResponderInterface;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitListResponseType;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupResponseType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupType;
import se.inera.ifv.hsawsresponder.v3.LookupHsaObjectType;
import se.inera.ifv.hsawsresponder.v3.PingResponseType;
import se.inera.ifv.hsawsresponder.v3.PingType;

public class HSAWebServiceCalls {

    @Autowired
    private HsaWsResponderInterface serverInterface;

    private static final Logger log = LoggerFactory.getLogger(HSAWebServiceCalls.class);

    private AttributedURIType logicalAddressHeader = new AttributedURIType();

    private AttributedURIType messageId = new AttributedURIType();

    private String hsaLogicalAddress = "SE165565594230-1000";

    /**
     * @param hsaLogicalAddress
     *            the hsaLogicalAddress to set
     */
    public void setHsaLogicalAddress(String hsaLogicalAddress) {
        this.hsaLogicalAddress = hsaLogicalAddress;
        logicalAddressHeader.setValue(hsaLogicalAddress);
    }

    /**
     * Help method to test access to HSA
     * 
     * @throws Exception
     */
    public void callPing() throws Exception {

        try {
            PingType pingtype = new PingType();
            PingResponseType response = serverInterface.ping(logicalAddressHeader, messageId, pingtype);
            log.debug("Response:" + response.getMessage());

        } catch (Throwable ex) {
            log.warn("Exception={}", ex.getMessage(), ex);
            throw new Exception(ex);
        }
    }

    /**
     * Method used to get miuRights for a HoS Person
     * 
     * @param parameters
     * @return
     * @throws Exception
     */
    public GetMiuForPersonResponseType callMiuRights(GetMiuForPersonType parameters) {
        try {
            GetMiuForPersonResponseType response = serverInterface.getMiuForPerson(logicalAddressHeader, messageId,
                    parameters);
            return response;
        } catch (Throwable ex) {
            log.error("Failed to call getMiuForPerson", ex);
            Throwables.propagate(ex);
            return null;
        }
    }

    /**
     * Method to retrieve data for a hsa unit
     * 
     * @param hsaId
     * @throws Exception
     */
    public GetCareUnitResponseType callGetCareunit(String hsaId) {
        LookupHsaObjectType parameters = new LookupHsaObjectType();
        parameters.setHsaIdentity(hsaId);

        try {
            GetCareUnitResponseType response = serverInterface.getCareUnit(logicalAddressHeader, messageId, parameters);
            return response;
        } catch (Throwable ex) {
            log.error("Failed to call getCareUnit", ex);
            Throwables.propagate(ex);
            return null;
        }
    }

    /**
     * Method to retrieve the caregiver for a hsa unit
     * 
     * @param hsaId
     */
    public GetHsaUnitResponseType callGetHsaunit(String hsaId) {
        LookupHsaObjectType parameters = new LookupHsaObjectType();
        parameters.setHsaIdentity(hsaId);

        try {
            return serverInterface.getHsaUnit(logicalAddressHeader, messageId, parameters);
        } catch (Throwable ex) {
            log.error("Failed to call getHsaUnit", ex);
            Throwables.propagate(ex);
            return null;
        }
    }

    /**
     * Method to retrieve attributes for a HoS Person
     * 
     * @param parameters
     * @return
     * @throws Exception
     */
    public HsawsSimpleLookupResponseType callHsawsSimpleLookup(HsawsSimpleLookupType parameters) {
        try {
            HsawsSimpleLookupResponseType response = serverInterface.hsawsSimpleLookup(logicalAddressHeader, messageId,
                    parameters);
            return response;
        } catch (Throwable ex) {
            log.error("Failed to call hsawsSimpleLookup", ex);
            Throwables.propagate(ex);
            return null;
        }
    }

    public GetCareUnitListResponseType callGetCareUnitList(LookupHsaObjectType parameters) {
        try {
            GetCareUnitListResponseType response = serverInterface.getCareUnitList(logicalAddressHeader, messageId,
                    parameters);
            return response;
        } catch (Throwable ex) {
            log.error("Failed to call getCareUnitList", ex);
            Throwables.propagate(ex);
            return null;
        }
    }

}
