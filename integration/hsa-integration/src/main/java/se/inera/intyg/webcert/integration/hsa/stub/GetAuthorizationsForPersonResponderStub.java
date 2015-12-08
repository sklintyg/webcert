package se.inera.intyg.webcert.integration.hsa.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonType;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.CredentialInformationType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-03.
 */
public class GetAuthorizationsForPersonResponderStub implements GetCredentialsForPersonIncludingProtectedPersonResponderInterface {

    @Autowired
    private HsaServiceStub serviceStub;

    @Override
    public GetCredentialsForPersonIncludingProtectedPersonResponseType getCredentialsForPersonIncludingProtectedPerson(String logicalAddress, GetCredentialsForPersonIncludingProtectedPersonType parameters) {
        GetCredentialsForPersonIncludingProtectedPersonResponseType response = new GetCredentialsForPersonIncludingProtectedPersonResponseType();
        response.setResultCode(ResultCodeEnum.OK);

        if (serviceStub.getMedarbetaruppdrag().size() > 0) {
            for (Medarbetaruppdrag miu : serviceStub.getMedarbetaruppdrag()) {
                if (miu.getHsaId().equals(parameters.getPersonHsaId())) {
                    response.getCredentialInformation().addAll(miuInformationTypesForEnhetsIds(miu, parameters.getPersonHsaId()));
                }
            }
        }

        return response;
    }


    private List<CredentialInformationType> miuInformationTypesForEnhetsIds(Medarbetaruppdrag medarbetaruppdrag, String hsaPersonId) {
        List<CredentialInformationType> informationTypes = new ArrayList<>();
        CredentialInformationType cit = new CredentialInformationType();
        cit.setPersonHsaId(hsaPersonId);

        for (Vardgivare vardgivare : serviceStub.getVardgivare()) {
            for (Vardenhet enhet : vardgivare.getVardenheter()) {
                if (enhet.getId().endsWith("-finns-ej")) {
                    continue;
                }
                for (Medarbetaruppdrag.Uppdrag uppdrag : medarbetaruppdrag.getUppdrag()) {
                    if (uppdrag.getEnhet().endsWith("-finns-ej")) {
                        continue;
                    }
                    if (uppdrag.getEnhet().equals(enhet.getId())) {
                        for (String andamal : uppdrag.getAndamal()) {
                            CommissionType miuInfo = new CommissionType();
                            miuInfo.setCommissionHsaId(medarbetaruppdrag.getHsaId());
                            miuInfo.setCommissionPurpose(andamal);
                            miuInfo.setHealthCareUnitHsaId(enhet.getId());
                            miuInfo.setHealthCareUnitName(enhet.getNamn());
                            miuInfo.setHealthCareUnitStartDate(enhet.getStart());
                            miuInfo.setHealthCareUnitEndDate(enhet.getEnd());

                            miuInfo.setHealthCareProviderHsaId(vardgivare.getId());
                            miuInfo.setHealthCareProviderName(vardgivare.getNamn());


                            cit.getCommission().add(miuInfo);
                        }
                        cit.getGroupPrescriptionCode().add(enhet.getArbetsplatskod() != null ? enhet.getArbetsplatskod() : "0000000");
                    }
                }
            }

        }

        // Detta är någon slags extragrej som skall lägga till enhet för ett medarbetaruppgrag kopplat till en enhet
        // som ej skall finnas.
//        for (Medarbetaruppdrag.Uppdrag uppdrag : medarbetaruppdrag.getUppdrag()) {
//            if (uppdrag.getEnhet().endsWith("-finns-ej")) {
//                for (String andamal : uppdrag.getAndamal()) {
//                    CommissionType miuInfo = new CommissionType();
//                    miuInfo.setCommissionHsaId(medarbetaruppdrag.getHsaId());
//                    miuInfo.setCommissionPurpose(andamal);
//                    miuInfo.setHealthCareUnitHsaId(uppdrag.getEnhet());
//                    miuInfo.setHealthCareUnitName("Enhet som inte finns");
//                    miuInfo.setHealthCareProviderHsaId(uppdrag.getVardgivare());
//                    cit.getCommission().add(miuInfo);
//                }
//            }
//        }
        informationTypes.add(cit);
        return informationTypes;
    }
}
