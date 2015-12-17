package se.inera.intyg.webcert.integration.hsa.stub;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Mottagning;
import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.riv.infrastructure.directory.organization.getunit.v1.rivtabp21.GetUnitResponderInterface;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitResponseType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.UnitType;
import se.riv.infrastructure.directory.v1.AddressType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-03.
 */
public class GetUnitResponderStub implements GetUnitResponderInterface {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Override
    public GetUnitResponseType getUnit(String logicalAddress, GetUnitType parameters) {
        if (parameters.getUnitHsaId().endsWith("-finns-ej")) {
            return null;
        }

        GetUnitResponseType response = new GetUnitResponseType();
        Vardenhet vardenhet = hsaServiceStub.getVardenhet(parameters.getUnitHsaId());
        if  (vardenhet != null) {

            UnitType unit = abstractVardenhetToUnitType(vardenhet);
            response.setUnit(unit);
        } else {
            Mottagning mottagning = hsaServiceStub.getMottagning(parameters.getUnitHsaId());
            UnitType unit = abstractVardenhetToUnitType(mottagning);
            response.setUnit(unit);
        }
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

    private UnitType abstractVardenhetToUnitType(AbstractVardenhet vardenhet) {
        UnitType unit = new UnitType();
        unit.setUnitName(vardenhet.getNamn());
        unit.setUnitHsaId(vardenhet.getId());
        unit.setMail(vardenhet.getEpost());
        unit.setPostalCode(vardenhet.getPostnummer());
        unit.getTelephoneNumber().add(vardenhet.getTelefonnummer());

        AddressType addressType = new AddressType();
        addressType.getAddressLine().add(vardenhet.getPostadress());
        addressType.getAddressLine().add(vardenhet.getPostort());
        unit.setPostalAddress(addressType);
        return unit;
    }
}
