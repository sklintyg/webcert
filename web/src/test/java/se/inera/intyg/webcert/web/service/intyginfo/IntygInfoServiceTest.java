/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyginfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;

@RunWith(MockitoJUnitRunner.class)
public class IntygInfoServiceTest {

    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private ArendeService arendeService;
    @Mock
    private FragaSvarRepository fragaSvarRepository;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private CertificateRelationService relationService;
    @Mock
    private GetIntygInfoEventsService getIntygInfoEventsService;
    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private IntygInfoService testee;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
    }

    @Test
    public void notFound() {
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(arendeService.getArendenInternal(anyString())).thenReturn(new ArrayList<>());
        when(fragaSvarRepository.findByIntygsReferensIntygsId(anyString())).thenReturn(new ArrayList<>());

        Optional<WcIntygInfo> intygInfo = testee.getIntygInfo("not_found");

        assertFalse(intygInfo.isPresent());
        verifyNoInteractions(moduleRegistry);
        verifyNoInteractions(relationService);
    }

    @Test
    public void onlyHandelser() {
        String intygId = "onlyHandelser";
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(arendeService.getArendenInternal(anyString())).thenReturn(new ArrayList<>());
        when(fragaSvarRepository.findByIntygsReferensIntygsId(anyString())).thenReturn(new ArrayList<>());

        when(getIntygInfoEventsService.get(intygId)).thenReturn(List.of(new IntygInfoEvent(Source.WEBCERT)));

        Optional<WcIntygInfo> optIntygInfo = testee.getIntygInfo(intygId);

        assertTrue(optIntygInfo.isPresent());

        WcIntygInfo intygInfo = optIntygInfo.get();

        assertEquals(1, intygInfo.getEvents().size());
        verifyNoInteractions(moduleRegistry);
        verifyNoInteractions(relationService);
    }

    @Test
    public void onlyArende() {
        String intygId = "onlyArende";
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(fragaSvarRepository.findByIntygsReferensIntygsId(anyString())).thenReturn(new ArrayList<>());

        List<Arende> arende = new ArrayList<>();
        arende.add(createArende(ArendeAmne.KOMPLT, intygId, FrageStallare.FORSAKRINGSKASSAN.getKod(), Status.CLOSED));

        when(arendeService.getArendenInternal(anyString())).thenReturn(arende);

        Optional<WcIntygInfo> optIntygInfo = testee.getIntygInfo(intygId);

        assertTrue(optIntygInfo.isPresent());

        WcIntygInfo intygInfo = optIntygInfo.get();

        assertEquals(2, intygInfo.getEvents().size());
        assertEquals(1, intygInfo.getKompletteringar());
        assertEquals(1, intygInfo.getKompletteringarAnswered());
        verifyNoInteractions(moduleRegistry);
        verifyNoInteractions(relationService);
    }

    @Test
    public void onlyFragaSvar() {
        String intygId = "onlyFragasvar";
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(arendeService.getArendenInternal(anyString())).thenReturn(new ArrayList<>());

        List<FragaSvar> fragaSvars = new ArrayList<>();
        fragaSvars.add(createFragaSvar(Amne.AVSTAMNINGSMOTE, FrageStallare.FORSAKRINGSKASSAN.getKod(), Status.CLOSED));
        when(fragaSvarRepository.findByIntygsReferensIntygsId(anyString())).thenReturn(fragaSvars);

        Optional<WcIntygInfo> optIntygInfo = testee.getIntygInfo(intygId);

        assertTrue(optIntygInfo.isPresent());

        WcIntygInfo intygInfo = optIntygInfo.get();

        assertEquals(2, intygInfo.getEvents().size());
        assertEquals(0, intygInfo.getKompletteringar());
        assertEquals(0, intygInfo.getKompletteringarAnswered());
        assertEquals(1, intygInfo.getAdministrativaFragorReceived());
        assertEquals(1, intygInfo.getAdministrativaFragorReceivedAnswered());
        verifyNoInteractions(moduleRegistry);
        verifyNoInteractions(relationService);
    }

    @Test
    public void existingUtkastLocked() throws IOException, ModuleException {
        // Arrange
        String intygId = "existingIntyg";
        Utkast utkast = createUtkast(intygId, UtkastStatus.DRAFT_LOCKED);
        utkast.setAterkalladDatum(utkast.getSkapad().plusDays(1));

        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));

        when(getIntygInfoEventsService.get(intygId)).thenReturn(
            List.of(new IntygInfoEvent(Source.WEBCERT), new IntygInfoEvent(Source.WEBCERT)));

        // Act
        Optional<WcIntygInfo> optIntygInfo = testee.getIntygInfo(intygId);

        // Assert
        assertTrue(optIntygInfo.isPresent());

        WcIntygInfo intygInfo = optIntygInfo.get();

        assertEquals(intygId, intygInfo.getIntygId());
        assertEquals(utkast.getIntygsTyp(), intygInfo.getIntygType());
        assertEquals(utkast.getIntygTypeVersion(), intygInfo.getIntygVersion());
        assertEquals(utkast.getEnhetsId(), intygInfo.getCareUnitHsaId());
        assertEquals(utkast.getEnhetsNamn(), intygInfo.getCareUnitName());
        assertEquals(utkast.getVardgivarId(), intygInfo.getCareGiverHsaId());
        assertEquals(utkast.getVardgivarNamn(), intygInfo.getCareGiverName());
        assertEquals(utkast.getSkapad(), intygInfo.getDraftCreated());
        assertEquals(utkast.getSkickadTillMottagareDatum(), intygInfo.getSentToRecipient());

        assertEquals(9, intygInfo.getEvents().size());
        assertEquals(0, intygInfo.getKompletteringar());
        assertEquals(0, intygInfo.getKompletteringarAnswered());
        assertEquals(0, intygInfo.getAdministrativaFragorReceived());
        assertEquals(0, intygInfo.getAdministrativaFragorReceivedAnswered());

        verifyNoInteractions(moduleRegistry);
        verify(relationService).findChildRelations(intygId);
        verify(arendeService).getArendenInternal(intygId);
        verify(fragaSvarRepository).findByIntygsReferensIntygsId(intygId);
    }

    @Test
    public void existingUtkastSigned() throws IOException, ModuleException, ModuleNotFoundException {
        // Arrange
        String intygId = "existingIntyg";
        Utkast utkast = createUtkast(intygId, UtkastStatus.SIGNED);

        when(utkastRepository.findById(eq(intygId))).thenReturn(Optional.of(utkast));

        List<Arende> arende = new ArrayList<>();
        arende.add(createArende(ArendeAmne.KOMPLT, intygId, FrageStallare.FORSAKRINGSKASSAN.getKod(), Status.CLOSED));
        when(arendeService.getArendenInternal(anyString())).thenReturn(arende);

        List<FragaSvar> fragaSvars = new ArrayList<>();
        fragaSvars.add(createFragaSvar(Amne.AVSTAMNINGSMOTE, FrageStallare.FORSAKRINGSKASSAN.getKod(), Status.CLOSED));
        when(fragaSvarRepository.findByIntygsReferensIntygsId(anyString())).thenReturn(fragaSvars);

        List<WebcertCertificateRelation> relations = new ArrayList<>();
        relations.add(createRelation(RelationKod.FRLANG, "forId"));
        relations.add(createRelation(RelationKod.ERSATT, "ersattId"));
        relations.add(createRelation(RelationKod.KOMPLT, "kompId"));
        when(relationService.findChildRelations(anyString())).thenReturn(relations);

        Utkast utkast2 = createUtkast("forId", UtkastStatus.SIGNED);
        when(utkastRepository.findById(eq("forId"))).thenReturn(Optional.of(utkast2));

        // Act
        Optional<WcIntygInfo> optIntygInfo = testee.getIntygInfo(intygId);

        // Assert
        assertTrue(optIntygInfo.isPresent());

        WcIntygInfo intygInfo = optIntygInfo.get();

        assertEquals(intygId, intygInfo.getIntygId());
        assertEquals(utkast.getIntygsTyp(), intygInfo.getIntygType());
        assertEquals(utkast.getIntygTypeVersion(), intygInfo.getIntygVersion());
        assertEquals(utkast.getEnhetsId(), intygInfo.getCareUnitHsaId());
        assertEquals(utkast.getEnhetsNamn(), intygInfo.getCareUnitName());
        assertEquals(utkast.getVardgivarId(), intygInfo.getCareGiverHsaId());
        assertEquals(utkast.getVardgivarNamn(), intygInfo.getCareGiverName());
        assertEquals(utkast.getSkapad(), intygInfo.getDraftCreated());
        assertEquals(utkast.getSkickadTillMottagareDatum(), intygInfo.getSentToRecipient());

        assertEquals(12, intygInfo.getEvents().size());
        assertEquals(1, intygInfo.getKompletteringar());
        assertEquals(1, intygInfo.getKompletteringarAnswered());
        assertEquals(1, intygInfo.getAdministrativaFragorReceived());
        assertEquals(1, intygInfo.getAdministrativaFragorReceivedAnswered());

        verify(moduleRegistry).getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
        verify(relationService).findChildRelations(intygId);
        verify(arendeService).getArendenInternal(intygId);
        verify(fragaSvarRepository).findByIntygsReferensIntygsId(intygId);
    }

    private Utkast createUtkast(String intygId, UtkastStatus status) throws IOException, ModuleException {
        LocalDateTime created = LocalDateTime.now();

        Utkast utkast = new Utkast();

        utkast.setStatus(status);
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp("lisjp");
        utkast.setIntygTypeVersion("1.0");
        utkast.setVardgivarId("vg1-id");
        utkast.setVardgivarNamn("vg1");
        utkast.setEnhetsId("ve1-id");
        utkast.setEnhetsNamn("ve1");

        utkast.setRelationIntygsId(intygId + "from");
        utkast.setRelationKod(RelationKod.KOPIA);

        String fakeModel = intygId;

        utkast.setModel(fakeModel);

        utkast.setSkapad(created);
        VardpersonReferens vardpersonReferens = new VardpersonReferens("hsaId", "namn");
        utkast.setSkapadAv(vardpersonReferens);

        utkast.setSenastSparadAv(vardpersonReferens);
        utkast.setSenastSparadDatum(created.plusHours(1));

        utkast.setKlartForSigneringDatum(created.plusHours(2));

        if (status == UtkastStatus.SIGNED) {
            Signatur signatur = new Signatur(created.plusHours(3), "user1", intygId, "", "", "");
            utkast.setSignatur(signatur);
        }

        utkast.setSkickadTillMottagare(LisjpEntryPoint.DEFAULT_RECIPIENT_ID);
        utkast.setSkickadTillMottagareDatum(created.plusHours(4));

        HoSPersonal hoSPersonal = new HoSPersonal();
        hoSPersonal.setFullstandigtNamn("lakare");
        GrundData grundData = new GrundData();
        grundData.setSkapadAv(hoSPersonal);

        Utlatande utlatande = mock(Utlatande.class);
        when(utlatande.getGrundData()).thenReturn(grundData);

        when(moduleApi.getUtlatandeFromJson(eq(fakeModel))).thenReturn(utlatande);

        return utkast;
    }

    private FragaSvar createFragaSvar(Amne amne, String skickatAv, Status status) {
        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setAmne(amne);
        fragaSvar.setFrageSkickadDatum(LocalDateTime.now());
        fragaSvar.setFrageStallare(skickatAv);
        fragaSvar.setStatus(status);
        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsTyp("lisjp");
        fragaSvar.setIntygsReferens(intygsReferens);

        return fragaSvar;
    }

    private Arende createArende(ArendeAmne amne, String intygId, String skickatAv, Status status) {
        Arende arende = new Arende();
        arende.setAmne(amne);
        arende.setSkickatTidpunkt(LocalDateTime.now());
        arende.setIntygsId(intygId);
        arende.setSkickatAv(skickatAv);
        arende.setStatus(status);
        arende.setIntygTyp("lisjp");

        return arende;
    }

    private WebcertCertificateRelation createRelation(RelationKod code, String toIntyg) {
        return new WebcertCertificateRelation(toIntyg, code, LocalDateTime.now(), UtkastStatus.SIGNED, false);
    }
}
