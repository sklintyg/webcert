//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package se.inera.intyg.webcert.web.service.mail.stubs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HsaServiceStub {
    private final List<String> readOnlyCareProvider = new ArrayList();
    private final Map<String, HsaPerson> hsaPersonMap = new HashMap();
    private final Map<String, CredentialInformation> credentialInformationMap = new HashMap();
    private final Map<String, CareProviderStub> careProviderMap = new HashMap();
    private final Map<String, CareUnitStub> careUnitMap = new HashMap();
    private final Map<String, SubUnitStub> subUnitMap = new HashMap();
    private LocalDateTime lastHospUpdate = LocalDateTime.now(ZoneId.systemDefault());

    public HsaServiceStub() {
    }

    public void addCredentialInformation(CredentialInformation credentialInformation) {
        add(credentialInformation.getHsaId(), credentialInformation, this.credentialInformationMap);
    }

    public void updateCredentialInformation(CredentialInformation credentialInformation) {
        CredentialInformation information = (CredentialInformation)get(credentialInformation.getHsaId(), this.credentialInformationMap);
        if (information != null) {
            information.getCommissionList().addAll(credentialInformation.getCommissionList());
        } else {
            this.addCredentialInformation(credentialInformation);
        }

    }

    public void deleteCredentialInformation(String hsaId) {
        remove(hsaId, this.credentialInformationMap);
    }

    public Collection<CredentialInformation> getCredentialInformation() {
        return this.credentialInformationMap.values();
    }

    public CredentialInformation getCredentialInformation(String hsaId) {
        return (CredentialInformation)get(hsaId, this.credentialInformationMap);
    }

    public void addHsaPerson(HsaPerson hsaPerson) {
        add(hsaPerson.getHsaId(), hsaPerson, this.hsaPersonMap);
        add(hsaPerson.getPersonalIdentityNumber(), hsaPerson, this.hsaPersonMap);
    }

    public void deleteHsaPerson(String id) {
        remove(id, this.hsaPersonMap);
    }

    public Collection<HsaPerson> getHsaPerson() {
        return this.hsaPersonMap.values();
    }

    public HsaPerson getHsaPerson(String id) {
        return (HsaPerson)get(id, this.hsaPersonMap);
    }

    public void addCareProvider(CareProviderStub careProviderStub) {
        if (careProviderStub != null) {
            add(careProviderStub.getId(), careProviderStub, this.careProviderMap);
            if (careProviderStub.getCareUnits() != null && !careProviderStub.getCareUnits().isEmpty()) {
                Iterator<CareUnitStub> var2 = careProviderStub.getCareUnits().iterator();

                while (true) {
                    CareUnitStub careUnitStub;
                    do {
                        do {
                            if (!var2.hasNext()) {
                                return;
                            }

                            careUnitStub = (CareUnitStub)var2.next();
                            careUnitStub.setCareProviderHsaId(careProviderStub.getId());
                            add(careUnitStub.getId(), careUnitStub, this.careUnitMap);
                        } while (careUnitStub.getSubUnits() == null);
                    } while (careUnitStub.getSubUnits().isEmpty());

                    for (SubUnitStub subUnit : careUnitStub.getSubUnits()) {
                        subUnit.setParentHsaId(careUnitStub.getId());
                        add(subUnit.getId(), subUnit, this.subUnitMap);
                    }
                }
            }
        }

    }

    public void addCareUnit(CareUnitStub careUnitStub) {
        add(careUnitStub.getId(), careUnitStub, this.careUnitMap);
    }

    public void deleteCareUnit(String id) {
        remove(id, this.careUnitMap);
    }

    public void addSubUnit(SubUnitStub subUnitStub) {
        add(subUnitStub.getId(), subUnitStub, this.subUnitMap);
    }

    public void deleteSubUnit(String id) {
        remove(id, this.subUnitMap);
    }

    public void deleteCareProvider(String hsaId) {
        CareProviderStub careProvider = (CareProviderStub)get(hsaId, this.careProviderMap);
        if (careProvider != null) {
            List<CareUnitStub> careUnits = careProvider.getCareUnits();
            CareUnitStub careUnitStub;
            if (careUnits != null) {
                for (Iterator<CareUnitStub> var4 = careUnits.iterator(); var4.hasNext(); remove(careUnitStub.getId(), this.careUnitMap)) {
                    careUnitStub = (CareUnitStub)var4.next();
                    List<SubUnitStub> subUnits = careUnitStub.getSubUnits();
                    if (subUnits != null) {

                        for (SubUnitStub subUnit : subUnits) {
                            remove(subUnit.getId(), this.subUnitMap);
                        }
                    }
                }
            }

            remove(hsaId, this.careProviderMap);
        }

    }

    public Collection<CareProviderStub> getCareProvider() {
        return this.careProviderMap.values();
    }

    public CareProviderStub getCareProvider(String hsaId) {
        return (CareProviderStub)get(hsaId, this.careProviderMap);
    }

    public CareUnitStub getCareUnit(String hsaId) {
        return (CareUnitStub)get(hsaId, this.careUnitMap);
    }

    public SubUnitStub getSubUnit(String hsaId) {
        return (SubUnitStub)get(hsaId, this.subUnitMap);
    }

    public void markAsReadOnly(String hsaId) {
        if (hsaId != null) {
            this.readOnlyCareProvider.add(hsaId.toUpperCase());
        }

    }

    public boolean isCareProviderReadOnly(String hsaId) {
        return hsaId != null && this.readOnlyCareProvider.contains(hsaId.toUpperCase());
    }

    public LocalDateTime getHospLastUpdate() {
        return this.lastHospUpdate;
    }

    public void resetHospLastUpdate() {
        this.lastHospUpdate = LocalDateTime.now(ZoneId.systemDefault());
    }

    private static <T> void add(String id, T value, Map<String, T> map) {
        if (id != null && value != null && map != null) {
            map.put(formatId(id), value);
        }

    }

    private static <T> void remove(String id, Map<String, T> map) {
        if (id != null && map != null) {
            map.remove(formatId(id));
        }

    }

    private static <T> T get(String id, Map<String, T> map) {
        if (id != null && map != null) {
            String formatId = formatId(id);
            return isNullOrShouldNotExistInHsa(formatId) ? null : map.get(formatId);
        } else {
            return null;
        }
    }

    private static String formatId(String id) {
        return StringUtils.trimAllWhitespace(id.toUpperCase());
    }

    private static boolean isNullOrShouldNotExistInHsa(String hsaId) {
        return hsaId == null || hsaId.startsWith("EJHSA") || "UTANENHETSID".equals(hsaId) || hsaId.endsWith("-FINNS-EJ");
    }
}
