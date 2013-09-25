package se.inera.webcert.hsa.stub;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.hsaws.v3.HsaWsFault;
import se.inera.ifv.hsaws.v3.HsaWsResponderInterface;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitListResponseType;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitMembersResponseType;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHospPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHospPersonType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetInformationListResponseType;
import se.inera.ifv.hsawsresponder.v3.GetInformationListType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.GetPriceUnitsForAuthResponseType;
import se.inera.ifv.hsawsresponder.v3.GetPriceUnitsForAuthType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupResponseType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupType;
import se.inera.ifv.hsawsresponder.v3.IsAuthorizedToSystemResponseType;
import se.inera.ifv.hsawsresponder.v3.IsAuthorizedToSystemType;
import se.inera.ifv.hsawsresponder.v3.LookupHsaObjectType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.hsawsresponder.v3.PingResponseType;
import se.inera.ifv.hsawsresponder.v3.PingType;
import se.inera.ifv.hsawsresponder.v3.VpwGetPublicUnitsResponseType;
import se.inera.ifv.hsawsresponder.v3.VpwGetPublicUnitsType;

import javax.jws.WebParam;

/**
 * @author johannesc
 */
public class HsaWebServiceStub implements HsaWsResponderInterface {

    @Autowired
    private HsaServiceStub hsaService;

    @Override
    public GetHsaUnitResponseType getHsaUnit(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetHsaUnit", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") LookupHsaObjectType parameters) throws HsaWsFault {
        GetHsaUnitResponseType response = null;
        HsaUnitStub hsaUnit = hsaService.getHsaUnit(parameters.getHsaIdentity());
        if (hsaUnit != null) {
            response = convertHsaUnitToResponse(hsaUnit);
        }
        return response;
    }

    private GetHsaUnitResponseType convertHsaUnitToResponse(HsaUnitStub hsaUnit) {
        GetHsaUnitResponseType response = new GetHsaUnitResponseType();
        response.setHsaIdentity(hsaUnit.getHsaId());
        response.setEmail(hsaUnit.getEmail());
        response.setName(hsaUnit.getName());
        return response;
    }

    /**
     * Method used to get miuRights for a HoS Person
     */
    @Override
    public GetMiuForPersonResponseType getMiuForPerson(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetMiuForPerson", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") GetMiuForPersonType parameters) throws HsaWsFault {
        GetMiuForPersonResponseType response = new GetMiuForPersonResponseType();
        System.out.println("PERSON:" + parameters.getHsaIdentity());
        PersonStub person = hsaService.getPerson(parameters.getHsaIdentity());
        System.out.println("PERSON:" + person.getName());
        if(person != null) {
            for(String unitHsaId : person.getMedarbetaruppdrag()) {
                MiuInformationType miuInfo = new MiuInformationType();
                miuInfo.setHsaIdentity(person.getHsaId());
                miuInfo.setMiuPurpose("Vård och behandling");
                miuInfo.setCareUnitHsaIdentity(unitHsaId);
                HsaUnitStub unit = hsaService.getHsaUnit(unitHsaId);
                miuInfo.setCareUnitName(unit.getName());
                miuInfo.setCareGiver(unit.getVardgivarid());
                miuInfo.setCareGiverName(unit.getVardgivarid() + " - namn");
                System.out.println(response.getMiuInformation());
                response.getMiuInformation().add(miuInfo);
            }
        }
        return response;
    }


    /**
     * Returns work place code
     */
    @Override
    public HsawsSimpleLookupResponseType hsawsSimpleLookup(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "HsawsSimpleLookup", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") HsawsSimpleLookupType parameters) throws HsaWsFault {
        return null;
    }

    /**
     * Method to retrieve data for a hsa unit
     */
    @Override
    public GetCareUnitResponseType getCareUnit(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetCareUnit", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") LookupHsaObjectType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public IsAuthorizedToSystemResponseType isAuthorizedToSystem(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "IsAuthorizedToSystem", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") IsAuthorizedToSystemType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public VpwGetPublicUnitsResponseType vpwGetPublicUnits(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "VpwGetPublicUnits", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") VpwGetPublicUnitsType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetCareUnitListResponseType getCareUnitList(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetCareUnitList", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") LookupHsaObjectType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetPriceUnitsForAuthResponseType getPriceUnitsForAuth(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetPriceUnitsForAuth", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") GetPriceUnitsForAuthType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetHsaPersonResponseType getHsaPerson(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetHsaPerson", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") GetHsaPersonType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public PingResponseType ping(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "Ping", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") PingType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetCareUnitMembersResponseType getCareUnitMembers(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetCareUnitMembers", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") LookupHsaObjectType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetHospPersonResponseType getHospPerson(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetHospPerson", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") GetHospPersonType parameters) throws HsaWsFault {
        return null;
    }

    @Override
    public GetInformationListResponseType getInformationList(@WebParam(partName = "LogicalAddress", name = "To", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType logicalAddress, @WebParam(partName = "Id", name = "MessageID", targetNamespace = "http://www.w3.org/2005/08/addressing", header = true) AttributedURIType id, @WebParam(partName = "parameters", name = "GetInformationList", targetNamespace = "urn:riv:hsa:HsaWsResponder:3") GetInformationListType parameters) throws HsaWsFault {
        return null;
    }
}
