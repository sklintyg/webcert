/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.intygstyper.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.intygstyper.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.*;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

public class UtkastBootstrapBean {

    @Autowired
    private IntygModuleRegistry registry;

    @Autowired
    private UtkastRepository utkastRepo;

    @Autowired
    private FragaSvarRepository fragaRepo;

    @Autowired
    private ArendeRepository arendeRepo;

    private CustomObjectMapper mapper = new CustomObjectMapper();

    private List<Amne> fsAmnen = Arrays.asList(Amne.ARBETSTIDSFORLAGGNING, Amne.AVSTAMNINGSMOTE, Amne.KONTAKT, Amne.OVRIGT);
    private List<ArendeAmne> arendeAmnen = Arrays.asList(ArendeAmne.ARBTID, ArendeAmne.AVSTMN, ArendeAmne.KONTKT, ArendeAmne.OVRIGT);
    private Random rand = new Random();

    public static final Logger LOG = LoggerFactory.getLogger(UtkastBootstrapBean.class);

    @PostConstruct
    public void init() throws IOException {
        for (Resource resource : getResourceListing("classpath*:module-bootstrap-certificate/*.xml")) {
            try {
                String moduleName = resource.getFilename().split("__")[0];
                LOG.info("Bootstrapping certificate '{}' from module ", resource.getFilename(), moduleName);
                Utlatande utlatande = registry.getModuleApi(moduleName).getUtlatandeFromXml(IOUtils.toString(resource.getInputStream()));
                if (utkastRepo.findOne(utlatande.getId()) == null) {
                    utkastRepo.save(createUtkast(utlatande));
                    switch (utlatande.getTyp()) {
                    case Fk7263EntryPoint.MODULE_ID:
                        fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, true, false));
                        fragaRepo.save(createFragaSvar(utlatande, FrageStallare.WEBCERT, false, false));
                        fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, false, true));
                        fragaRepo.save(createFragaSvar(utlatande, FrageStallare.FORSAKRINGSKASSAN, false, false));
                        break;
                    case TsBasEntryPoint.MODULE_ID:
                    case TsDiabetesEntryPoint.MODULE_ID:
                        // These certificates does not support arende or fragaSvar
                        break;
                    default: // SIT certificates
                        setupArende(utlatande, true, true, FrageStallare.FORSAKRINGSKASSAN);
                        setupArende(utlatande, false, false, FrageStallare.WEBCERT);
                        setupArende(utlatande, false, false, FrageStallare.FORSAKRINGSKASSAN);
                        break;
                    }
                }
            } catch (ModuleException | ModuleNotFoundException | IOException e) {
                LOG.error("Could not bootstrap {}", resource.getFilename(), e);
            }
        }
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
            arende.setKomplettering(Arrays.asList(ma));
        }
        arende.setMeddelande("Meddelandetext");
        String meddelandeId = UUID.randomUUID().toString();
        arende.setMeddelandeId(meddelandeId);
        arende.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        arende.setPatientPersonId(utlatande.getGrundData().getPatient().getPersonId().getPersonnummerWithoutDash());
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
        fs.setIntygsReferens(
                new IntygsReferens(utlatande.getId(), utlatande.getTyp(), utlatande.getGrundData().getPatient().getPersonId(),
                        utlatande.getGrundData().getPatient().getFullstandigtNamn(), LocalDateTime.now()));
        if (komplettering) {
            fs.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
            Komplettering kompl1 = new Komplettering();
            kompl1.setFalt("fält");
            kompl1.setText("kompletteringstext");
            fs.setKompletteringar(new HashSet<>(Arrays.asList(kompl1)));
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
        return fs;
    }

    private Utkast createUtkast(Utlatande json) throws JsonProcessingException {
        Utkast utkast = new Utkast();
        utkast.setEnhetsId(json.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        utkast.setEnhetsNamn(json.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        utkast.setIntygsId(json.getId());
        utkast.setIntygsTyp(json.getTyp());
        utkast.setModel(mapper.writeValueAsString(json));
        utkast.setPatientEfternamn(json.getGrundData().getPatient().getEfternamn());
        // This is a required field. However we do not supply it in RegisterMedicalCertificate so we put empty string if
        // this is null.
        utkast.setPatientFornamn(json.getGrundData().getPatient().getFornamn() == null ? "" : json.getGrundData().getPatient().getFornamn());
        utkast.setPatientMellannamn(json.getGrundData().getPatient().getMellannamn());
        utkast.setPatientPersonnummer(json.getGrundData().getPatient().getPersonId());
        utkast.setRelationIntygsId(null);
        utkast.setRelationKod(null);

        // Used for both senastSparadAv and skapadAv
        VardpersonReferens vardRef = new VardpersonReferens();
        vardRef.setHsaId(json.getGrundData().getSkapadAv().getPersonId());
        vardRef.setNamn(json.getGrundData().getSkapadAv().getFullstandigtNamn());
        utkast.setSenastSparadAv(vardRef);
        utkast.setSkapadAv(vardRef);

        utkast.setSignatur(new Signatur(LocalDateTime.now(), json.getGrundData().getSkapadAv().getPersonId(), json.getId(), "intygData",
                "intygHash", "signatur"));
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSenastSparadDatum(LocalDateTime.now());
        utkast.setSkickadTillMottagare("FK");
        utkast.setSkickadTillMottagareDatum(LocalDateTime.now());
        utkast.setVardgivarId(json.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        utkast.setVardgivarNamn(json.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());
        utkast.setVersion(1);
        utkast.setVidarebefordrad(false);
        return utkast;
    }

    private List<Resource> getResourceListing(String classpathResourcePath) throws IOException {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return Arrays.asList(r.getResources(classpathResourcePath));
    }
}
