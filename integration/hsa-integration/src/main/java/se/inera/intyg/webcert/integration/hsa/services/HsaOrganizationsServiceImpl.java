package se.inera.intyg.webcert.integration.hsa.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.integration.hsa.client.AuthorizationManagementService;
import se.inera.intyg.webcert.integration.hsa.client.OrganizationUnitService;
import se.inera.intyg.webcert.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Mottagning;
import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;
import se.inera.intyg.webcert.integration.hsa.stub.Medarbetaruppdrag;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.HealthCareUnitMemberType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.HealthCareUnitMembersType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitResponseType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.UnitType;
import se.riv.infrastructure.directory.v1.AddressType;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.CredentialInformationType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Provides HSA organization services through TJK over NTjP.
 *
 * infrastructure:directory:organization and
 * infrastructure:directory:authorizationmanagement
 *
 * @author andreaskaltenbach, eriklupander
 */
@Service
public class HsaOrganizationsServiceImpl implements HsaOrganizationsService {

    private static final String DEFAULT_ARBETSPLATSKOD = "0000000";

    private static final Logger LOG = LoggerFactory.getLogger(HsaOrganizationsServiceImpl.class);

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    @Autowired
    private OrganizationUnitService organizationUnitService;

    @Override
    public Vardenhet getVardenhet(String careUnitHsaId) {

        LOG.debug("Getting info on vardenhet '{}'", careUnitHsaId);

        UnitType unit = getUnit(careUnitHsaId);

        Vardenhet vardenhet = new Vardenhet(unit.getUnitHsaId(), unit.getPublicName(), unit.getUnitStartDate(), unit.getUnitEndDate());
        attachMottagningar(vardenhet);
        updateWithContactInformation(vardenhet, unit);

        // TODO figure out how to get the group prescriptioncode - and if we need it at all.
        vardenhet.setArbetsplatskod(DEFAULT_ARBETSPLATSKOD);

        return vardenhet;
    }

    @Override
    public List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId) {
        List<Vardgivare> vardgivareList = new ArrayList<>();

        GetCredentialsForPersonIncludingProtectedPersonResponseType response = authorizationManagementService.getAuthorizationsForPerson(hosPersonHsaId, null, null);

        if (response.getResultCode() == ResultCodeEnum.OK) {
            List<CredentialInformationType> credentialInformationList = response.getCredentialInformation();
            for (CredentialInformationType credentialInformation : credentialInformationList) {
                List<CommissionType> commissions = credentialInformation.getCommission()
                        .stream()
                        .filter(commissionType -> Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(commissionType.getCommissionPurpose()))
                        .collect(Collectors.toList());

                LOG.debug("User '{}' has a total of {} medarbetaruppdrag", hosPersonHsaId, commissions.size());

                List<Vardgivare> vardgivare = commissions.stream()
                        .filter(ct -> isActive(ct.getHealthCareProviderStartDate(), ct.getHealthCareProviderEndDate()))
                        .map(ct -> new Vardgivare(ct.getHealthCareProviderHsaId(), ct.getHealthCareProviderName()))
                        .distinct()
                        .collect(Collectors.toList());


                for (Vardgivare vg : vardgivare) {

                    for (CommissionType ct : commissions) {

                        if (ct.getHealthCareProviderHsaId().equals(vg.getId()) && isActive(ct.getHealthCareUnitStartDate(), ct.getHealthCareUnitEndDate())) {
                            Vardenhet vardenhet = new Vardenhet(ct.getHealthCareUnitHsaId(), ct.getHealthCareUnitName());
                            vardenhet.setStart(ct.getHealthCareUnitStartDate());
                            vardenhet.setEnd(ct.getHealthCareUnitEndDate());
                            vardenhet.setArbetsplatskod(credentialInformation.getGroupPrescriptionCode().size() > 0 ? credentialInformation.getGroupPrescriptionCode().get(0) : null);

                            // I don't like this, but we need to do an extra call to infrastructure:directory:organization:getUnit for adress related stuff.
                            updateWithContactInformation(vardenhet, getUnit(vardenhet.getId()));

                            // Mottagningar
                            attachMottagningar(vardenhet);

                            if (!vg.getVardenheter().contains(vardenhet)) {
                                vg.getVardenheter().add(vardenhet);
                            }
                        }
                    }
                    vg.setVardenheter(vg.getVardenheter().stream()
                            .sorted((ve1, ve2) -> ve1.getNamn().compareTo(ve2.getNamn()))
                            .collect(Collectors.toList())
                    );
                    vardgivareList.add(vg);
                }
            }

            return vardgivareList.stream()
                    .sorted((vg1, vg2) -> {
                        if (vg1.getNamn() == null) {
                            return -1;
                        }
                        if (vg2.getNamn() == null) {
                            return 1;
                        }

                        return vg1.getNamn().compareTo(vg2.getNamn());
                    }).collect(Collectors.toList());
        } else {
            LOG.error("getAuthorizationsForPerson failed with code '{}' and message '{}'", response.getResultCode().value(), response.getResultText());
            return vardgivareList; // Empty
        }
    }
    private boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = new LocalDateTime();

        if (fromDate != null && now.isBefore(fromDate)) {
            return false;
        }
        return !(toDate != null && now.isAfter(toDate));
    }

    private void attachMottagningar(Vardenhet vardenhet) {
        GetHealthCareUnitMembersResponseType response = organizationUnitService.getHealthCareUnitMembers(vardenhet.getId());
        if (response == null || response.getResultCode() == ResultCodeEnum.ERROR) {
            LOG.error("Could not fetch mottagningar for unit {}, null or error response: ", vardenhet.getId(), response != null ? response.getResultText() : "Response was null");
            return;
        }
        HealthCareUnitMembersType healthCareUnitMembers = response.getHealthCareUnitMembers();

        for (HealthCareUnitMemberType member : healthCareUnitMembers.getHealthCareUnitMember()) {

            if (!isActive(member.getHealthCareUnitMemberStartDate(), member.getHealthCareUnitMemberEndDate())) {
                LOG.debug("Mottagning '{}' is not active right now", member.getHealthCareUnitMemberHsaId());
                continue;
            }

            Mottagning mottagning = new Mottagning(member.getHealthCareUnitMemberHsaId(), member.getHealthCareUnitMemberName(), member.getHealthCareUnitMemberStartDate(), member.getHealthCareUnitMemberEndDate());
            mottagning.setPostadress(member.getHealthCareUnitMemberpostalAddress().getAddressLine()
                    .stream()
                    .collect(Collectors.joining(" "))
            );
            mottagning.setPostnummer(member.getHealthCareUnitMemberpostalCode());
            mottagning.setTelefonnummer(member.getHealthCareUnitMemberTelephoneNumber().stream().collect(Collectors.joining(", ")));
            mottagning.setArbetsplatskod(member.getHealthCareUnitMemberPrescriptionCode().size() > 0 ? member.getHealthCareUnitMemberPrescriptionCode().get(0) : "0000000");

            vardenhet.getMottagningar().add(mottagning);
            LOG.debug("Attached mottagning '{}' to vardenhet '{}'", mottagning.getId(), vardenhet.getId());
        }
        vardenhet.setMottagningar(vardenhet.getMottagningar().stream().sorted().collect(Collectors.toList()));

    }



    private UnitType getUnit(String careUnitHsaId) {
        GetUnitResponseType unitResponse = organizationUnitService.getUnit(careUnitHsaId);
        return unitResponse.getUnit();
    }

    private void updateWithContactInformation(AbstractVardenhet vardenhet, UnitType response) {
        vardenhet.setEpost(response.getMail());
        if (response.getTelephoneNumber() != null && !response.getTelephoneNumber().isEmpty()) {
            vardenhet.setTelefonnummer(response.getTelephoneNumber().get(0));
        }
        AddressType address = response.getPostalAddress();
        if (address == null) {
            return;
        }

        vardenhet.setPostnummer(response.getPostalCode());
        StringBuilder postaAddress = new StringBuilder();
        List<String> lines = address.getAddressLine();
        for (int i = 0; i < lines.size() - 1; i++) {
            if (lines.get(i) != null) {
                postaAddress.append(lines.get(i).trim());
            }
        }

        vardenhet.setPostadress(postaAddress.toString());
        String lastLine = lines.get(lines.size() - 1);
        final int shortestLengthToIncludeBothPnrAndPostort = 7;
        if (lastLine != null && lastLine.length() > shortestLengthToIncludeBothPnrAndPostort && Character.isDigit(lastLine.charAt(0))) {
            final int startPostort = 6;
            vardenhet.setPostort(lastLine.substring(startPostort).trim());
        } else {
            vardenhet.setPostort(lastLine != null ? lastLine.trim() : "");
        }
    }
}
