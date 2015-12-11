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
package se.inera.intyg.webcert.integration.hsa.ifv.webcert.spi.authorization.impl;

public class HSAWebServiceCalls {

//    @Autowired
//    private HsaWsResponderInterface serverInterface;
//
//    private static final Logger LOG = LoggerFactory.getLogger(HSAWebServiceCalls.class);
//
//    private AttributedURIType logicalAddressHeader = new AttributedURIType();
//
//    private AttributedURIType messageId = new AttributedURIType();
//
//    /**
//     * @param hsaLogicalAddress the hsaLogicalAddress to set
//     */
//    public void setHsaLogicalAddress(String hsaLogicalAddress) {
//        logicalAddressHeader.setValue(hsaLogicalAddress);
//    }
//
//    /**
//     * Help method to test access to HSA.
//     *
//     * @throws Exception
//     */
//    public void callPing() throws Exception {
//
//        try {
//            PingType pingtype = new PingType();
//            PingResponseType response = serverInterface.ping(logicalAddressHeader, messageId, pingtype);
//            LOG.debug("Response:" + response.getMessage());
//
//        } catch (HsaWsFault ex) {
//            LOG.warn("Exception={}", ex.getMessage());
//            throw new Exception(ex);
//        }
//    }
//
//    /**
//     * Method used to get miuRights for a HoS Person.
//     *
//     * @param parameters
//     * @return
//     * @throws Exception
//     */
//    public GetMiuForPersonResponseType callMiuRights(GetMiuForPersonType parameters) {
//        try {
//            GetMiuForPersonResponseType response = serverInterface.getMiuForPerson(logicalAddressHeader, messageId,
//                    parameters);
//            return response;
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call getMiuForPerson for hsaId '{}'", parameters.getHsaIdentity());
//            Throwables.propagate(ex);
//            return null;
//        }
//    }
//
//    /**
//     * Method to retrieve data for a hsa unit.
//     *
//     * @param hsaId
//     * @throws Exception
//     */
//    public GetCareUnitResponseType callGetCareunit(String hsaId) {
//        try {
//            LookupHsaObjectType parameters = new LookupHsaObjectType();
//            parameters.setHsaIdentity(hsaId);
//            GetCareUnitResponseType response = serverInterface.getCareUnit(logicalAddressHeader, messageId, parameters);
//            return response;
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call getCareUnit for hsaId '{}'", hsaId);
//            Throwables.propagate(ex);
//            return null;
//        }
//    }
//
//    /**
//     * Method to retrieve the caregiver for a hsa unit.
//     *
//     * @param hsaId
//     */
//    public GetHsaUnitResponseType callGetHsaunit(String hsaId) {
//        try {
//            LookupHsaObjectType parameters = new LookupHsaObjectType();
//            parameters.setHsaIdentity(hsaId);
//            return serverInterface.getHsaUnit(logicalAddressHeader, messageId, parameters);
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call getHsaUnit for hsaId '{}'", hsaId);
//            Throwables.propagate(ex);
//            return null;
//        }
//    }
//
//    /**
//     * Method to retrieve attributes for a HoS Person.
//     *
//     * @param parameters
//     * @return
//     * @throws Exception
//     */
//    public HsawsSimpleLookupResponseType callHsawsSimpleLookup(HsawsSimpleLookupType parameters) {
//        try {
//            HsawsSimpleLookupResponseType response = serverInterface.hsawsSimpleLookup(logicalAddressHeader, messageId, parameters);
//            return response;
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call hsawsSimpleLookup with attributes {}", parameters.getAttributes().getAttribute().toArray());
//            Throwables.propagate(ex);
//            return null;
//        }
//    }
//
//    public GetCareUnitListResponseType callGetCareUnitList(LookupHsaObjectType parameters) {
//        try {
//            GetCareUnitListResponseType response = serverInterface.getCareUnitList(logicalAddressHeader, messageId,
//                    parameters);
//            return response;
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call getCareUnitList for careunit ID '{}'", parameters.getHsaIdentity());
//            Throwables.propagate(ex);
//            return null;
//        }
//    }
//
//    public GetHsaPersonResponseType callGetHsaPerson(GetHsaPersonType parameters) {
//        try {
//            GetHsaPersonResponseType response = serverInterface.getHsaPerson(logicalAddressHeader, messageId, parameters);
//            return response;
//        } catch (HsaWsFault ex) {
//            LOG.error("Failed to call callGetHsaPerson with hsaId '{}'", parameters.getHsaIdentity());
//            Throwables.propagate(ex);
//            return null;
//        }
//    }

}
