package se.inera.intyg.webcert.integration.hsa.stub;

import org.springframework.beans.factory.annotation.Autowired;

import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedperson.v1.rivtabp21.GetEmployeeIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonType;
import se.riv.infrastructure.directory.v1.PaTitleType;
import se.riv.infrastructure.directory.v1.PersonInformationType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-03.
 */
public class GetEmployeeResponderStub implements GetEmployeeIncludingProtectedPersonResponderInterface {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployeeIncludingProtectedPerson(String logicalAddress, GetEmployeeIncludingProtectedPersonType getEmployeeIncludingProtectedPersonType) {
        GetEmployeeIncludingProtectedPersonResponseType response = new GetEmployeeIncludingProtectedPersonResponseType();
        String personHsaId = getEmployeeIncludingProtectedPersonType.getPersonHsaId();
        HsaPerson hsaPerson = hsaServiceStub.getHsaPerson(personHsaId);
        if (hsaPerson == null) {
            response.setResultText("Null HsaPerson returned by HsaServiceStub.");
            response.setResultCode(ResultCodeEnum.ERROR);
            return response;
        }

        PersonInformationType person = new PersonInformationType();
        person.setTitle(hsaPerson.getTitel());
        person.setPersonHsaId(hsaPerson.getHsaId());
        person.setGivenName(hsaPerson.getEfterNamn());

        for (String legYrkesGrp : hsaPerson.getLegitimeradeYrkesgrupper()) {
            PaTitleType paTitle = new PaTitleType();
            paTitle.setPaTitleName(legYrkesGrp);
            person.getPaTitle().add(paTitle);
        }

        for (HsaSpecialicering spec : hsaPerson.getSpecialiseringar()) {
            person.getSpecialityCode().add(spec.getKod());
            person.getSpecialityName().add(spec.getNamn());
        }

        response.getPersonInformation().add(person);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }
}
