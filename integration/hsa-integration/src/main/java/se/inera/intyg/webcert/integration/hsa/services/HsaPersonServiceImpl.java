package se.inera.intyg.webcert.integration.hsa.services;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.integration.hsa.client.AuthorizationManagementService;
import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.inera.intyg.webcert.integration.hsa.stub.Medarbetaruppdrag;

import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

/**
 * @author nikpet
 */
@Service
public class HsaPersonServiceImpl implements HsaPersonService {

    private static final Logger LOG = LoggerFactory.getLogger(HsaPersonServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.integration.hsa.services.HsaPersonService#getHsaPersonInfo(java.lang.String)
     */
    @Override
    public List<PersonInformationType> getHsaPersonInfo(String personHsaId) {

        LOG.debug("Getting info from HSA for person '{}'", personHsaId);

        GetEmployeeIncludingProtectedPersonResponseType responseType = employeeService.getEmployee(personHsaId, null,  null);
        return responseType.getPersonInformation();

    }

    public List<CommissionType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId) {

        LOG.debug("Checking if person with HSA id '{}' has MIUs on unit '{}'", hosPersonHsaId, unitHsaId);

        GetCredentialsForPersonIncludingProtectedPersonResponseType response = authorizationManagementService.getAuthorizationsForPerson(hosPersonHsaId, null, null);
        List<CommissionType> commissions = response.getCredentialInformation().stream()
                .flatMap(ci -> ci.getCommission().stream())
                .collect(Collectors.toList());

        List<CommissionType> filteredMuisOnUnit = commissions.stream()
                .filter(ct -> ct.getHealthCareUnitHsaId().equals(unitHsaId))
                .filter(ct -> (ct.getHealthCareUnitEndDate() != null) ? ct.getHealthCareUnitEndDate().isAfter(LocalDateTime.now()) : true)
                .filter(ct -> (ct.getCommissionPurpose() != null) ? Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(ct.getCommissionPurpose() ) : false)
                .collect(Collectors.toList());

        LOG.debug("Person has {} MIUs on unit '{}'", filteredMuisOnUnit.size(), hosPersonHsaId);

        return filteredMuisOnUnit;
//        GetMiuForPersonType parameters = new GetMiuForPersonType();
//        parameters.setHsaIdentity(hosPersonHsaId);
//
//        GetMiuForPersonResponseType response = client.callMiuRights(parameters);
//
//        if (response == null) {
//            LOG.debug("Response from HSA was null, no valid MIUs was found for person '{}'", hosPersonHsaId);
//            return Lists.newArrayList();
//        }
//
//        List<MiuInformationType> miusForPerson = response.getMiuInformation();
//
//        if (miusForPerson.isEmpty()) {
//            LOG.debug("Response from HSA was empty, no MIUs was found for person '{}'", hosPersonHsaId);
//            return Lists.newArrayList();
//        }
//
//        LOG.debug("Person has a total of {} MIUs", miusForPerson.size());
//
//        Predicate<MiuInformationType> predicate = new Predicate<MiuInformationType>() {
//            @Override
//            public boolean apply(MiuInformationType miu) {
//
//                String miuHsaId = miu.getHsaIdentity();
//
//                LOG.debug("Checking MIU '{}' for unit '{}'", miuHsaId, miu.getCareUnitHsaIdentity());
//
//                if (checkMiuMatch(miu.getCareUnitHsaIdentity())) {
//
//                    if (!checkMiuPurpose(miu.getMiuPurpose())) {
//                        LOG.debug("- MIU '{}' is not '{}'", miuHsaId, Medarbetaruppdrag.VARD_OCH_BEHANDLING);
//                        return false;
//                    }
//
//                    if (!checkMiuNotExpired(miu.getCareUnitEndDate())) {
//                        LOG.debug("- MIU '{}' is expired", miuHsaId);
//                        return false;
//                    }
//
//                    LOG.debug("+ MIU '{}' is for the right unit and valid", miuHsaId);
//                    return true;
//                }
//                LOG.debug("- MIU '{}' is for another unit", miuHsaId);
//                return false;
//            }
//
//            // careUnitEndDate can be null, which does not mean that the thing expired
//            private boolean checkMiuNotExpired(LocalDateTime careUnitEndDate) {
//                return (careUnitEndDate != null) ? careUnitEndDate.isAfter(LocalDateTime.now()) : true;
//            }
//
//            private boolean checkMiuPurpose(String miuPurpose) {
//                return (miuPurpose != null) ? Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(miuPurpose) : false;
//            }
//
//            private boolean checkMiuMatch(String miuUnitHsaId) {
//                return (unitHsaId.equals(miuUnitHsaId));
//            }
//        };
//
//        List<MiuInformationType> miusOnUnit = Lists.newArrayList(Collections2.filter(miusForPerson, predicate));


    }
}
