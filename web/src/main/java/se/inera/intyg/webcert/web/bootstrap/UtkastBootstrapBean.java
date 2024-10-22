/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.bootstrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

public class UtkastBootstrapBean {

    public static final Logger LOG = LoggerFactory.getLogger(UtkastBootstrapBean.class);

    @Autowired
    private IntygModuleRegistry registry;
    @Autowired
    private UtkastRepository utkastRepo;
    @Autowired
    private FragaSvarRepository fragaRepo;
    @Autowired
    private ArendeRepository arendeRepo;

    private final Random rand = new Random();
    private final CustomObjectMapper mapper = new CustomObjectMapper();
    private final List<ArendeAmne> arendeAmnen = Arrays.asList(ArendeAmne.AVSTMN, ArendeAmne.KONTKT, ArendeAmne.OVRIGT);
    private final List<Amne> fsAmnen = Arrays.asList(Amne.ARBETSTIDSFORLAGGNING, Amne.AVSTAMNINGSMOTE, Amne.KONTAKT, Amne.OVRIGT);

    @PostConstruct
    public void init() throws IOException {
        for (Resource resource : getResourceListing("classpath*:module-bootstrap-certificate/*.xml")) {
            final var filename = resource.getFilename();

            if (filename == null) {
                continue;
            }

            try {
                final var moduleName = filename.split("__")[0];
                final var intygMajorTypeVersion = filename.split("\\.")[1];
                final var utlatande = buildUtlatande(resource, moduleName, intygMajorTypeVersion);

                if (utkastRepo.findById(utlatande.getId()).isPresent()) {
                    LOG.info("Bootstrapping certificate '{}' from module {} (version {}) skipped. Already in database.", filename,
                        moduleName, intygMajorTypeVersion);

                } else {
                    LOG.info("Bootstrapping certificate '{}' from module {} (version {})", filename, moduleName, intygMajorTypeVersion);
                    final var status = filename.contains("locked") ? UtkastStatus.DRAFT_LOCKED : UtkastStatus.SIGNED;
                    final var draft = createUtkast(utlatande, status);
                    addSignatureIfSignedCertificate(status, draft, utlatande);
                    utkastRepo.save(draft);

                    addArendeIfFKCertificate(utlatande);
                }

            } catch (ModuleException | ModuleNotFoundException | IOException e) {
                LOG.error("Could not bootstrap {}", filename, e);
            }
        }
    }

    private void addArendeIfFKCertificate(Utlatande utlatande) {
        if (utlatande.getTyp().equals(Fk7263EntryPoint.MODULE_ID)) {
            fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, true, false));
            fragaRepo.save(createFragaSvar(utlatande, FrageStallare.WEBCERT, false, false));
            fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, false, true));
            fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, false, false));
            fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, false, false, "Test person"));
        }

        if (utlatande.getTyp().equals(LuaefsEntryPoint.MODULE_ID) || utlatande.getTyp().equals(LuaenaEntryPoint.MODULE_ID)
            || utlatande.getTyp().equals(LisjpEntryPoint.MODULE_ID) || utlatande.getTyp().equals(LuseEntryPoint.MODULE_ID)) {
            setupArende(utlatande, true, true, FrageStallare.FORSAKRINGSKASSAN);
            setupArende(utlatande, false, false, FrageStallare.WEBCERT);
            setupArende(utlatande, false, false, FrageStallare.FORSAKRINGSKASSAN);
        }
    }

    private void addSignatureIfSignedCertificate(UtkastStatus status, Utkast draft, Utlatande utlatande) {
        if (status != UtkastStatus.SIGNED) {
            return;
        }

        final var certificateId = utlatande.getId();
        final var signDate = utlatande.getGrundData().getSigneringsdatum();
        final var createdBy = utlatande.getGrundData().getSkapadAv().getPersonId();
        final var sentDate = utlatande.getGrundData().getSigneringsdatum().plusMinutes(2);
        final var signature = new Signatur(signDate, createdBy, certificateId, "intygData", "intygHash", "signatur", SignaturTyp.LEGACY);
        draft.setSignatur(signature);
        draft.setSkickadTillMottagare("FKASSA");
        draft.setSkickadTillMottagareDatum(sentDate);
    }

    // INTYG-4086: An incredibly ugly hack to mitigate the fact that we're populating test-data using the XML format
    // and also directly to WC instead of storing in IT where these actually belong...
    private Utlatande buildUtlatande(Resource resource, String moduleName, String intygTypeVersion)
        throws ModuleException, ModuleNotFoundException, IOException {

        final var xml = Resources.toString(resource.getURL(), Charsets.UTF_8);
        final var utlatande = registry.getModuleApi(moduleName, intygTypeVersion).getUtlatandeFromXml(xml);

        switch (moduleName) {
            case "luse":
            case "luae_fs":
            case "luae_na":
            case "lisjp":
                RegisterCertificateType jaxbObject = JAXB.unmarshal(new StringReader(Resources.toString(resource.getURL(), Charsets.UTF_8)),
                    RegisterCertificateType.class);
                Patient patient = jaxbObject.getIntyg().getPatient();
                utlatande.getGrundData().getPatient().setFornamn(patient.getFornamn());
                utlatande.getGrundData().getPatient().setMellannamn(patient.getMellannamn());
                utlatande.getGrundData().getPatient().setEfternamn(patient.getEfternamn());
                utlatande.getGrundData().getPatient().setFullstandigtNamn(
                    IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));
                break;
            case "fk7263":
                RegisterMedicalCertificateType jaxbObject2 = JAXB.unmarshal(
                    new StringReader(Resources.toString(resource.getURL(), Charsets.UTF_8)), RegisterMedicalCertificateType.class);
                PatientType patient2 = jaxbObject2.getLakarutlatande().getPatient();
                utlatande.getGrundData().getPatient().setEfternamn(patient2.getFullstandigtNamn());
                utlatande.getGrundData().getPatient().setFullstandigtNamn(patient2.getFullstandigtNamn());
                break;
            case "ts-bas":
            case "ts-diabetes":
                break;
        }

        return utlatande;
    }

    private void setupArende(Utlatande utlatande, boolean komplettering, boolean paminnelse, FrageStallare fragestallare) {
        ArendeAmne amne;
        if (komplettering) {
            amne = ArendeAmne.KOMPLT;
        } else {
            amne = arendeAmnen.get(rand.nextInt(arendeAmnen.size()));
        }
        String meddelandeId = createArende(utlatande, komplettering, null, fragestallare, amne);
        if (paminnelse) {
            createArende(utlatande, false, meddelandeId, fragestallare, ArendeAmne.PAMINN);
        }
    }

    private String createArende(Utlatande utlatande, boolean komplettering, String paminnelseMeddelandeId, FrageStallare fragestallare,
        ArendeAmne amne) {
        Arende arende = new Arende();
        arende.setAmne(amne);
        arende.setEnhetId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        arende.setEnhetName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        arende.setIntygsId(utlatande.getId());
        arende.setIntygTyp(utlatande.getTyp());
        if (komplettering) {
            MedicinsktArende ma = new MedicinsktArende();
            // This question always exist.
            ma.setFrageId("1");
            ma.setInstans(1);
            ma.setText("Kompletteringstext");
            arende.setKomplettering(List.of(ma));
        }
        arende.setMeddelande("Meddelandetext\n\nblablablablabla\n\n\nArmen");
        String meddelandeId = UUID.randomUUID().toString();
        arende.setMeddelandeId(meddelandeId);
        arende.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        arende.setPatientPersonId(utlatande.getGrundData().getPatient().getPersonId().getPersonnummer());
        arende.setReferensId("referens");
        arende.setRubrik(arende.getAmne().getDescription());
        arende.setSenasteHandelse(LocalDateTime.now());
        arende.setSigneratAv(utlatande.getGrundData().getSkapadAv().getPersonId());
        arende.setSigneratAvName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        arende.setSkickatAv(fragestallare.getKod());
        arende.setSkickatTidpunkt(LocalDateTime.now());
        if (fragestallare.equals(FrageStallare.WEBCERT)) {
            arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
        } else {
            arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            arende.setSistaDatumForSvar(LocalDate.now().plusWeeks(2));
        }
        arende.setSvarPaId(null);
        arende.setSvarPaReferens(null);
        arende.setTimestamp(LocalDateTime.now());
        arende.setVardaktorName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        arende.setVardgivareName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());
        arende.setVidarebefordrad(false);
        arendeRepo.save(arende);
        return meddelandeId;
    }

    private FragaSvar createFragaSvar(Utlatande utlatande, FrageStallare fragestallare, boolean komplettering, boolean paminnelse) {
        return createFragaSvar(utlatande, fragestallare, komplettering, paminnelse, null);
    }

    private FragaSvar createFragaSvar(Utlatande utlatande, FrageStallare fragestallare, boolean komplettering, boolean paminnelse,
        String vardPersonName) {
        FragaSvar fs = new FragaSvar();
        fs.setFrageSigneringsDatum(LocalDateTime.now());
        fs.setFrageSkickadDatum(LocalDateTime.now());
        if (fragestallare.equals(FrageStallare.FORSAKRINGSKASSAN)) {
            fs.setStatus(Status.PENDING_INTERNAL_ACTION);
            fs.setExternaKontakter(new HashSet<>(Arrays.asList("Testperson1 FK", "Testperson2 FK")));
            fs.setExternReferens("Extern referens");
            fs.setSistaDatumForSvar(LocalDate.now().plusWeeks(2));
        } else {
            fs.setStatus(Status.PENDING_EXTERNAL_ACTION);
        }
        fs.setFrageStallare(fragestallare.getKod());
        fs.setFrageText("Detta är frågan");
        fs.setIntygsReferens(new IntygsReferens(utlatande.getId(), utlatande.getTyp(), utlatande.getGrundData().getPatient().getPersonId(),
            null, utlatande.getGrundData().getSigneringsdatum()));
        if (komplettering) {
            fs.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            Komplettering kompl1 = new Komplettering();
            kompl1.setFalt("fält");
            kompl1.setText("kompletteringstext");
            fs.setKompletteringar(new HashSet<>(List.of(kompl1)));
        } else if (paminnelse) {
            fs.setAmne(Amne.PAMINNELSE);
        } else {
            fs.setAmne(fsAmnen.get(rand.nextInt(fsAmnen.size())));
        }
        fs.setMeddelandeRubrik("Rubrik");
        fs.setVardAktorHsaId(utlatande.getGrundData().getSkapadAv().getPersonId());
        fs.setVardAktorNamn(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        fs.setVardperson(FragaSvarConverter.convert(utlatande.getGrundData().getSkapadAv()));
        fs.setVidarebefordrad(false);

        if (!Objects.isNull(vardPersonName)) {
            fs.getVardperson().setNamn(vardPersonName);
        }

        return fs;
    }

    private Utkast createUtkast(Utlatande json, UtkastStatus status) throws JsonProcessingException {
        final var utkast = new Utkast();
        utkast.setIntygsId(json.getId());
        utkast.setIntygsTyp(json.getTyp());
        utkast.setIntygTypeVersion(json.getTextVersion() == null ? "1.0" : json.getTextVersion());

        utkast.setEnhetsId(json.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        utkast.setEnhetsNamn(json.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        utkast.setVardgivarId(json.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        utkast.setVardgivarNamn(json.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

        final var vardRef = new VardpersonReferens();
        vardRef.setHsaId(json.getGrundData().getSkapadAv().getPersonId());
        vardRef.setNamn(json.getGrundData().getSkapadAv().getFullstandigtNamn());
        utkast.setSenastSparadAv(vardRef);
        utkast.setSkapadAv(vardRef);

        utkast.setPatientEfternamn(json.getGrundData().getPatient().getEfternamn());
        utkast.setPatientFornamn(json.getGrundData().getPatient().getFornamn());
        utkast.setPatientMellannamn(json.getGrundData().getPatient().getMellannamn());
        utkast.setPatientPersonnummer(json.getGrundData().getPatient().getPersonId());

        final var signedDate = json.getGrundData().getSigneringsdatum();
        utkast.setSkapad(signedDate.minusMinutes(2));
        utkast.setSenastSparadDatum(signedDate);
        utkast.setModel(mapper.writeValueAsString(json));
        utkast.setVersion(1);
        utkast.setStatus(status);
        if (status != UtkastStatus.SIGNED) {
            json.getGrundData().setSigneringsdatum(null);
        }

        utkast.setVidarebefordrad(false);
        utkast.setRelationIntygsId(null);
        utkast.setRelationKod(null);
        return utkast;
    }

    private List<Resource> getResourceListing(String classpathResourcePath) throws IOException {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return Arrays.asList(r.getResources(classpathResourcePath));
    }
}
