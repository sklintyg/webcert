package se.inera.intyg.webcert.web.service.intyginfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;
import se.inera.intyg.webcert.web.web.controller.internalapi.IntygInfoHistory;
import se.inera.intyg.webcert.web.web.controller.internalapi.IntygInfoResponse;

@Service
public class IntygInfoService {

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Value("${job.utkastlock.locked.after.day}")
    private int lockedAfterDay;

    @Transactional(readOnly = true)
    public IntygInfoResponse getIntygInfo(String intygId) {
        Utkast utkast = utkastRepository.getOne(intygId);

        IntygInfoResponse response = new IntygInfoResponse();

        response.setIntygId(utkast.getIntygsId());
        response.setIntygType(utkast.getIntygsTyp());
        response.setIntygVersion(utkast.getIntygTypeVersion());

        response.setDraftCreated(utkast.getSkapad());

        response.setSentToRecipient(utkast.getSkickadTillMottagareDatum());

        response.setCareUnitHsaId(utkast.getEnhetsId());
        response.setCareUnitName(utkast.getEnhetsNamn());

        response.setCareGiverHsaId(utkast.getVardgivarId());
        response.setCareGiverName(utkast.getVardgivarNamn());

        if (utkast.getSignatur() != null) {
            Signatur signatur = utkast.getSignatur();

            response.setSignedDate(signatur.getSigneringsDatum());
            response.setSignedByHsaId(signatur.getSigneradAv());

            try {
                ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
                Utlatande utlatande = moduleApi.getUtlatandeFromJson(utkast.getModel());

                response.setSignedByName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());

            } catch (ModuleNotFoundException | ModuleException | IOException e) {
                e.printStackTrace();
            }
        }

        if (UtkastStatus.SIGNED.equals(utkast.getStatus())) {
            addArendeInformation(response);
        }

        addHistory(utkast, response);

        return response;
    }

    private void addHistory(Utkast utkast, IntygInfoResponse response) {
        List<IntygInfoHistory> history = response.getHistory();

        // Created by
        IntygInfoHistory createdBy = new IntygInfoHistory();
        createdBy.setDate(utkast.getSkapad());
        createdBy.setText("Utkastet skapades av " + utkast.getSkapadAv().getNamn());
        history.add(createdBy);

        // Locked
        if (UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
            IntygInfoHistory locked = new IntygInfoHistory();
            locked.setDate(utkast.getSkapad().plusDays(lockedAfterDay)); // TODO: Inte optimalt
            locked.setText("Utkastet låstes");
            history.add(locked);
        }

        if (UtkastStatus.SIGNED.equals(utkast.getStatus())) {
            // Signed
            IntygInfoHistory signed = new IntygInfoHistory();
            signed.setDate(utkast.getSignatur().getSigneringsDatum());
            signed.setText("Intyget signerades av " + response.getSignedByName());
            history.add(signed);
        }
    }

    private void addArendeInformation(IntygInfoResponse response) {
        List<Arende> arenden = arendeService.getArendenInternal(response.getIntygId());

        // Kompletteringar
        List<Arende> kompletteringarQuestions = arenden.stream()
            .filter(a -> ArendeAmne.KOMPLT.equals(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.FRAGA))
            .collect(Collectors.toList());
        long answered = kompletteringarQuestions.stream().filter(a -> Status.CLOSED.equals(a.getStatus())).count();

        response.setKomletteingar(kompletteringarQuestions.size());
        response.setKomletteingarAnswered((int) answered);

        List<Arende> adminQuestions = arenden.stream()
            .filter(a -> Arrays.asList(ArendeAmne.AVSTMN, ArendeAmne.KONTKT, ArendeAmne.OVRIGT).contains(a.getAmne()))
            .filter(a -> ArendeViewConverter.getArendeType(a).equals(ArendeType.FRAGA))
            .collect(Collectors.toList());
        Set<Status> answeredOrClosed = Stream.of(Status.ANSWERED, Status.CLOSED).collect(Collectors.toSet());

        // Admin frågor skickade
        List<Arende> adminQuestionsSent = adminQuestions.stream()
            .filter(a -> FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsSentAnswered = adminQuestionsSent.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdminQuestionsSent(adminQuestionsSent.size());
        response.setAdminQuestionsSentAnswered((int) adminQuestionsSentAnswered);

        // Admin frågor mottagna
        List<Arende> adminQuestionsRecieved = adminQuestions.stream()
            .filter(a -> !FrageStallare.WEBCERT.getKod().equals(a.getSkickatAv()))
            .collect(Collectors.toList());
        long adminQuestionsRecievedAnswered = adminQuestionsRecieved.stream().filter(a -> answeredOrClosed.contains(a.getStatus())).count();

        response.setAdminQuestionsReceived(adminQuestionsRecieved.size());
        response.setAdminQuestionsReceivedAnswered((int) adminQuestionsRecievedAnswered);


        // TODO: Add history

        arenden.forEach(arende -> {
            String text = null;

            switch (arende.getAmne()) {
                case KOMPLT:
                    text = arende.getSkickatAv() + " skickade en kompletteringsbegäran";
                    break;
                case AVSTMN:
                case KONTKT:
                case OVRIGT:
                    if (FrageStallare.WEBCERT.getKod().equals(arende.getSkickatAv())) {
                        text = " skickade en administrativ fråga till " + arende.getSkickatAv(); // TODO: Vem skickade?
                    } else {
                        text = arende.getSkickatAv() + " skickade en administrativ fråga";
                    }
                    break;
                default:

            }

            if (text != null) {
                IntygInfoHistory item = new IntygInfoHistory();
                item.setDate(arende.getSkickatTidpunkt());
                item.setText(text);

                response.getHistory().add(item);
            }
        });
    }
}
