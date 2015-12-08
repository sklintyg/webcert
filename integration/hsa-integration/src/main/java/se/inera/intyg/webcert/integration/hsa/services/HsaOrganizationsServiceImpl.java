package se.inera.intyg.webcert.integration.hsa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * @author andreaskaltenbach
 */
@Service
public class HsaOrganizationsServiceImpl implements HsaOrganizationsService {

    private static final String HSA_SEARCH_BASE = "c=SE";
    private static final String HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER = "hsaHealthCareUnitMember";
    private static final String HSA_ATR_UNIT_PRESCRIPTION_CODE = "unitPrescriptionCode";
    private static final String HSA_ATR_HSA_IDENTITY = "hsaIdentity";

    private static final String DEFAULT_ARBETSPLATSKOD = "0000000";
    private static final String DEFAULT_POSTNR = "XXXXX";

    private static final Logger LOG = LoggerFactory.getLogger(HsaOrganizationsServiceImpl.class);

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    @Autowired
    private OrganizationUnitService organizationUnitService;

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
                    vg.setVardenheter(vg.getVardenheter().stream().sorted((ve1, ve2) -> ve1.getNamn().compareTo(ve2.getNamn())).collect(Collectors.toList()));
                    vardgivareList.add(vg);
                }
            }

            // In a final processing step, we need to merge identical vardgivare contents
            Map<Vardgivare, List<Vardenhet>> map = new HashMap<>();
            for (Vardgivare vg : vardgivareList) {
                if (!map.containsKey(vg)) {
                    map.put(vg, vg.getVardenheter());
                } else {
                    for (Vardenhet ve : vg.getVardenheter()) {
                        if (!map.get(vg).contains(ve)) {
                            map.get(vg).add(ve);
                        }
                    }
                }
            }

            Iterator<Map.Entry<Vardgivare, List<Vardenhet>>> i = map.entrySet().iterator();
            List<Vardgivare> finalList = new ArrayList<>();
            while (i.hasNext()) {
                Map.Entry<Vardgivare, List<Vardenhet>> next = i.next();
                next.getKey().setVardenheter(next.getValue());
                finalList.add(next.getKey());
            }

            return finalList.stream()
                    .sorted((vg1, vg2) -> {
                        if (vg1.getNamn() == null) return -1;
                        if (vg2.getNamn() == null) return 1;

                        return vg1.getNamn().compareTo(vg2.getNamn());
                    }).collect(Collectors.toList());
        } else {
            return null;
        }
    }

//        List<MiuInformationType> miuInformation = response.getMiuInformation();
//        LOG.debug("User '{}' has a total of {} medarbetaruppdrag", hosPersonHsaId, miuInformation.size());
//
//        // filter by syfte. Only 'Vård och behandling' assignments are relevant for WebCert.
//        Iterable<MiuInformationType> filteredMius = Iterables.filter(miuInformation,
//                new Predicate<MiuInformationType>() {
//                    @Override
//                    public boolean apply(MiuInformationType miuInformationType) {
//                        return Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(miuInformationType
//                                .getMiuPurpose());
//                    }
//                });

        // group medarbetaruppdrag by vardgivare ID
//        ImmutableListMultimap<String, MiuInformationType> vardgivareIdToMiuInformation = Multimaps.index(filteredMius,
//                new Function<MiuInformationType, String>() {
//                    @Override
//                    public String apply(MiuInformationType miuInformation) {
//                        return miuInformation.getCareGiver();
//                    }
//                });
//
//        LOG.debug("User '{}' has {} VoB medarbetaruppdrag for {} vårdgivare", hosPersonHsaId,
//                vardgivareIdToMiuInformation.size(), vardgivareIdToMiuInformation.keySet().size());
//
//        for (String vardgivareId : vardgivareIdToMiuInformation.keySet()) {
//            Vardgivare vardgivare = createVardgivareFromMIU(vardgivareIdToMiuInformation.get(vardgivareId));
//            if (vardgivare != null) {
//                vardgivareList.add(vardgivare);
//            }
//        }
//
//        Collections.sort(vardgivareList);
//
//        return vardgivareList;


//    private List<Vardenhet> fetchAllEnheter(Vardgivare vardgivare, MiuInformationType miuInformationType) {
//
//        List<Vardenhet> vardenheter = new ArrayList<>();
//
//        String enhetHsaId = miuInformationType.getCareUnitHsaIdentity();
//        String enhetNamn = miuInformationType.getCareUnitName();
//
//        String enhetDn = fetchDistinguishedName(enhetHsaId, enhetNamn);
//
//        if (enhetDn == null) {
//            return vardenheter;
//        }
//
//        LOG.debug("DN for unit '{}' is '{}'", enhetHsaId, enhetDn);
//
//        List<CareUnitType> careUnits = fetchSubEnheter(vardgivare, enhetDn);
//
//        for (CareUnitType careUnit : careUnits) {
//            Vardenhet vardenhet = new Vardenhet(careUnit.getHsaIdentity(), careUnit.getCareUnitName(),
//                    careUnit.getCareUnitStartDate(), careUnit.getCareUnitEndDate());
//
//            // only add enhet if it is currently active
//            if (!isActive(vardenhet.getStart(), vardenhet.getEnd())) {
//                LOG.debug("Vårdenhet '{}' is not active right now", vardenhet.getId());
//                continue;
//            }
//
//            GetHsaUnitResponseType response = client.callGetHsaunit(vardenhet.getId());
//            updateWithContactInformation(vardenhet, response);
//
//            vardenhet.setArbetsplatskod(getWorkplaceCode(vardenhet.getId()));
//
//            attachMottagningar(vardenhet);
//
//            vardenheter.add(vardenhet);
//        }
//
//        return vardenheter;
//    }
//
    private boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = new LocalDateTime();

        if (fromDate != null && now.isBefore(fromDate)) {
            return false;
        }
        return !(toDate != null && now.isAfter(toDate));
    }
//
//    private List<CareUnitType> fetchSubEnheter(Vardgivare vardgivare, String enhetDn) {
//        LookupHsaObjectType parameters = new LookupHsaObjectType();
//        parameters.setHsaIdentity(vardgivare.getId());
//        parameters.setSearchBase(enhetDn);
//        GetCareUnitListResponseType response = client.callGetCareUnitList(parameters);
//        LOG.debug("Enhet '{}' has {} enheter beneath", enhetDn, response.getCareUnits().getCareUnit().size());
//        return response.getCareUnits().getCareUnit();
//    }
//
//    private String fetchDistinguishedName(String hsaId, String namn) {
//        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();
//        ExactType exactType = new ExactType();
//        exactType.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
//        exactType.setSearchOperator(SearchOperatorExact.EXACT);
//        exactType.setValue(hsaId);
//
//        AttributeListType attributeList = new AttributeListType();
//        attributeList.getAttribute().add(HSA_ATR_HSA_IDENTITY);
//        lookupType.setAttributes(attributeList);
//        lookupType.setLookup(exactType);
//
//        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);
//        switch (lookupResponse.getResponseValues().size()) {
//        case 0:
//            LOG.error(LogMarkers.HSA, "Unit '{}' with hsaId '{}' is missing", namn, hsaId);
//            return null;
//        case 1:
//            return lookupResponse.getResponseValues().get(0).getDN();
//        default:
//            LOG.warn(LogMarkers.HSA, "The hsaId '{}' is used for more than one unit. This is probably due to a misconfiguration in HSA", hsaId);
//            return lookupResponse.getResponseValues().get(0).getDN();
//        }
//    }
//
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

//        List<String> mottagningsIds = fetchMottagningsHsaId(vardenhet.getId());
//        for (String mottagningsId : mottagningsIds) {
//            Mottagning mottagning = fetchMottagning(mottagningsId);
//
//            if (mottagning == null) {
//                LOG.error(LogMarkers.HSA, "Could not attach mottagning '{}' to vardenhet '{}'", mottagningsId, vardenhet.getId());
//                continue;
//            }
//
//            // only add mottagning if it is active
//            if (!isActive(mottagning.getStart(), mottagning.getEnd())) {
//                LOG.debug("Mottagning '{}' is not active right now", mottagning.getId());
//                continue;
//            }
//
//            mottagning.setArbetsplatskod(getWorkplaceCode(mottagning.getId()));
//
//            vardenhet.getMottagningar().add(mottagning);
//            LOG.debug("Attached mottagning '{}' to vardenhet '{}'", mottagning.getId(), vardenhet.getId());
//        }

       // Collections.sort(vardenhet.getMottagningar());
    }
//
//    private String getWorkplaceCode(String careUnitHsaId) {
//        LOG.debug("Fetching arbetsplatskod from HSA for enhet '{}'", careUnitHsaId);
//
//        HsawsSimpleLookupType parameters = new HsawsSimpleLookupType();
//
//        ExactType lookupValue = new ExactType();
//        lookupValue.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
//        lookupValue.setSearchOperator(SearchOperatorExact.EXACT);
//        lookupValue.setValue(careUnitHsaId);
//        parameters.setLookup(lookupValue);
//
//        AttributeListType attributes = new AttributeListType();
//        attributes.getAttribute().add(HSA_ATR_UNIT_PRESCRIPTION_CODE);
//
//        parameters.setAttributes(attributes);
//
//        HsawsSimpleLookupResponseType response = client.callHsawsSimpleLookup(parameters);
//
//        // Read response and pick values
//        for (AttributeValueListType attValList : response.getResponseValues()) {
//            for (AttributeValuePairType attValue : attValList.getResponse()) {
//                if (attValue.getAttribute().equalsIgnoreCase(HSA_ATR_UNIT_PRESCRIPTION_CODE)) {
//                    if (!attValue.getValue().isEmpty()) {
//                        String value = attValue.getValue().get(0);
//                        if (value != null && !value.isEmpty()) {
//                            return value;
//                        } else {
//                            return DEFAULT_ARBETSPLATSKOD;
//                        }
//                    }
//                }
//            }
//        }
//
//        return DEFAULT_ARBETSPLATSKOD;
//    }
//
//    private Mottagning fetchMottagning(String mottagningsHsaId) {
//
//        GetHsaUnitResponseType response = client.callGetHsaunit(mottagningsHsaId);
//
//        LOG.debug("Fetching data for mottagning '{}'", mottagningsHsaId);
//
//        if (response == null) {
//            LOG.error(LogMarkers.HSA, "Mottagning '{}' was not found in HSA. Inconsistent data in HSA", mottagningsHsaId);
//            return null;
//        }
//
//        Mottagning mottagning = new Mottagning(response.getHsaIdentity(), response.getName(), response.getStartDate(), response.getEndDate());
//        updateWithContactInformation(mottagning, response);
//
//        return mottagning;
//    }
//
//    private void updateWithContactInformation(AbstractVardenhet vardenhet, GetHsaUnitResponseType response) {
//        vardenhet.setEpost(response.getEmail());
//        if (response.getTelephoneNumbers() != null && !response.getTelephoneNumbers().getTelephoneNumber().isEmpty()) {
//            vardenhet.setTelefonnummer(response.getTelephoneNumbers().getTelephoneNumber().get(0));
//        }
//        AddressType address = response.getPostalAddress();
//        if (address == null) {
//            return;
//        }
//        StringBuilder postaAddress = new StringBuilder();
//        List<String> lines = address.getAddressLine();
//        for (int i = 0; i < lines.size() - 1; i++) {
//            postaAddress.append(lines.get(i).trim());
//        }
//        vardenhet.setPostadress(postaAddress.toString());
//        String lastLine = lines.get(lines.size() - 1);
//        final int shortestLengthToIncludeBothPnrAndPostort = 7;
//        if (lastLine.length() > shortestLengthToIncludeBothPnrAndPostort && Character.isDigit(lastLine.charAt(0))) {
//            final int startPostort = 6;
//            vardenhet.setPostnummer(lastLine.substring(0, startPostort).trim());
//            vardenhet.setPostort(lastLine.substring(startPostort).trim());
//        } else {
//            vardenhet.setPostnummer(DEFAULT_POSTNR);
//            vardenhet.setPostort(lastLine.trim());
//        }
//    }
//
//    private List<String> fetchMottagningsHsaId(String vardenhetsId) {
//        List<String> mottagningIds = new ArrayList<>();
////
////        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();
////
////        ExactType exactType = new ExactType();
////        exactType.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
////        exactType.setSearchOperator(SearchOperatorExact.EXACT);
////        exactType.setValue(vardenhetsId);
////
////        AttributeListType attributeList = new AttributeListType();
////        attributeList.getAttribute().add(HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER);
////        lookupType.setAttributes(attributeList);
////        lookupType.setSearchBase(HSA_SEARCH_BASE);
////        lookupType.setLookup(exactType);
////
////        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);
//
//
//
//        if (lookupResponse.getResponseValues().isEmpty()) {
//            LOG.debug("Vardenhet '{}' has 0 mottagningar", vardenhetsId);
//            return mottagningIds;
//        }
//
//        List<AttributeValuePairType> attributes = lookupResponse.getResponseValues().get(0).getResponse();
//
//        for (AttributeValuePairType attribute : attributes) {
//            if (HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER.equals(attribute.getAttribute())) {
//                mottagningIds.addAll(attribute.getValue());
//            }
//        }
//
//        LOG.debug("Vardenhet '{}' has {} mottagningar", vardenhetsId, mottagningIds.size());
//
//        return mottagningIds;
//    }
//
//    private Vardgivare createVardgivareFromMIU(List<MiuInformationType> miuInformationTypes) {
//
//        Vardgivare vardgivare = new Vardgivare(miuInformationTypes.get(0).getCareGiver(), miuInformationTypes.get(0).getCareGiverName());
//
//        for (MiuInformationType miuInformationType : miuInformationTypes) {
//            vardgivare.getVardenheter().addAll(fetchAllEnheter(vardgivare, miuInformationType));
//        }
//
//        Collections.sort(vardgivare.getVardenheter());
//
//        if (vardgivare.getVardenheter().isEmpty()) {
//            return null;
//        }
//
//        LOG.debug("Created Vardgivare {}", vardgivare);
//
//        return vardgivare;
//    }

    @Override
    public Vardenhet getVardenhet(String careUnitHsaId) {

        LOG.debug("Getting info on vardenhet '{}'", careUnitHsaId);

        UnitType unit = getUnit(careUnitHsaId);

        Vardenhet vardenhet = new Vardenhet(unit.getUnitHsaId(), unit.getPublicName(), unit.getUnitStartDate(), unit.getUnitEndDate());
        updateWithContactInformation(vardenhet, unit);
        vardenhet.setArbetsplatskod(DEFAULT_ARBETSPLATSKOD);
//
//        // get the basic details
//        GetHsaUnitResponseType response = client.callGetHsaunit(careUnitHsaId);
//
//        Vardenhet vardenhet = new Vardenhet(careUnitHsaId, response.getName(),
//                response.getStartDate(), response.getEndDate());
//
//        // update with unit contact details
//        updateWithContactInformation(vardenhet, response);
//
//        // update with the workplace code for this unit
//        String unitWorkplaceCode = getWorkplaceCode(vardenhet.getId());
//        vardenhet.setArbetsplatskod(unitWorkplaceCode);

        return vardenhet;
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
            //vardenhet.setPostnummer(lastLine.substring(0, startPostort).trim());
            vardenhet.setPostort(lastLine.substring(startPostort).trim());
        } else {
            //vardenhet.setPostnummer(DEFAULT_POSTNR);
            vardenhet.setPostort(lastLine != null ? lastLine.trim() : "");
        }
    }
}
