/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.srs.services;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Bedomningsunderlag;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosintyg;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.GetSRSInformationRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.GetSRSInformationResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.GetSRSInformationResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Individ;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Individfaktorer;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Prediktionsfaktorer;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgard;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendation;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardsrekommendationstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Diagnosstatistik;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Statistikstatus;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.infra.srs.model.SrsCertificate;
import se.inera.intyg.webcert.infra.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsPrediction;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestion;
import se.inera.intyg.webcert.infra.srs.model.SrsQuestionResponse;
import se.inera.intyg.webcert.infra.srs.model.SrsRecommendation;
import se.inera.intyg.webcert.infra.srs.model.SrsResponse;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.CVType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Diagnos;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

@Service
public class SrsInfraServiceImpl implements SrsInfraService {

  private static final int POSTNUMMER_LENGTH = 5;

  private static final String HSA_ROOT = "1.2.752.129.2.1.4.1";
  private static final String CONSUMER_HSA_ID = "SE5565594230-B31";
  private static final String DIAGNOS_CODE_SYSTEM = "1.2.752.116.1.1.1.1.3";

  @Autowired
  private GetSRSInformationResponderInterface getSRSInformation;
  @Autowired
  private GetPredictionQuestionsResponderInterface getPrediction;
  @Autowired
  private GetConsentResponderInterface getConsent;
  @Autowired
  private SetConsentResponderInterface setConsent;
  @Autowired
  private GetDiagnosisCodesResponderInterface getDiagnosisCodes;
  @Autowired
  private GetSRSInformationForDiagnosisResponderInterface getSRSInformationForDiagnosis;
  @Autowired
  private SetOwnOpinionResponderInterface setOwnOpinion;

  @Override
  public SrsResponse getSrs(
      IntygUser user,
      Personnummer personnummer,
      List<SrsCertificate> certDiags,
      Utdatafilter filter,
      List<SrsQuestionResponse> questions,
      Integer daysIntoSickLeave)
      throws InvalidPersonNummerException {

    if (questions == null || questions.isEmpty()) {
      throw new IllegalArgumentException("Answers are required to construct a valid request.");
    }
    if (daysIntoSickLeave == null) {
      daysIntoSickLeave = 15;
    }
    GetSRSInformationResponseType response =
        getSRSInformation.getSRSInformation(
            createRequest(user, personnummer, certDiags, filter, questions, daysIntoSickLeave));

    if (response.getResultCode() != ResultCodeEnum.OK) {
      throw new IllegalArgumentException("Bad data from SRS");
    }

    // Schema mandates that this is of 1..*
    Bedomningsunderlag underlag = response.getBedomningsunderlag().get(0);

    String statistikStatusCode = null;
    List<Integer> statistikNationellStatistik = null;
    String atgarderDiagnosisCode = null;
    String statistikDiagnosisCode = null;
    List<SrsRecommendation> atgarderObs = null;
    List<SrsRecommendation> atgarderRek = null;
    List<SrsRecommendation> atgarderFrl = null;
    List<SrsRecommendation> atgarderReh = null;
    String atgarderStatusCode = null;
    List<SrsPrediction> predictions = new ArrayList<>();
    if (underlag == null
        || underlag.getPrediktion() == null
        || underlag.getPrediktion().getDiagnosprediktion().isEmpty()) {
      SrsPrediction srsPrediction = new SrsPrediction();
      srsPrediction.setStatusCode(Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS.value());
      predictions.add(srsPrediction);
    } else {
      underlag
          .getPrediktion()
          .getDiagnosprediktion()
          .forEach(
              dp -> {
                SrsPrediction srsPrediction = new SrsPrediction();
                if (dp == null) {
                  srsPrediction = null;
                } else if (dp.getDiagnosprediktionstatus()
                    == Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS) {
                  srsPrediction.setStatusCode(
                      Diagnosprediktionstatus.PREDIKTIONSMODELL_SAKNAS.value());
                } else {
                  // Always add prevalence if we have it regardless if the user requested prediction
                  // on a personal level
                  srsPrediction.setPrevalence(dp.getPrevalens());
                  srsPrediction.setCertificateId(dp.getIntygId().getExtension());
                  srsPrediction.setDiagnosisCode(
                      Optional.ofNullable(dp.getDiagnos()).map(CVType::getCode).orElse(null));
                  // Also check if we have a prediction
                  if (dp.getSannolikhetOvergransvarde() != null) {
                    srsPrediction.setDaysIntoSickLeave(dp.getSjukskrivningsdag());
                    srsPrediction.setModelVersion(dp.getPrediktionsmodellversion());
                    srsPrediction.setLevel(dp.getRisksignal().getRiskkategori());
                    srsPrediction.setDescription(dp.getRisksignal().getBeskrivning());
                    srsPrediction.setStatusCode(dp.getDiagnosprediktionstatus().value());
                    srsPrediction.setProbabilityOverLimit(dp.getSannolikhetOvergransvarde());
                    srsPrediction.setTimestamp(dp.getBerakningstidpunkt());
                    if (dp.getPrediktionsfaktorer() != null) {
                      srsPrediction.setQuestionsResponses(
                          ImmutableList.copyOf(
                              dp.getPrediktionsfaktorer().getFragasvar().stream()
                                  .map(
                                      (fs) ->
                                          SrsQuestionResponse.create(
                                              fs.getFrageidSrs(), fs.getSvarsidSrs()))
                                  .collect(Collectors.toList())));
                    }
                    if (dp.getLakarbedomningRisk() != null) {
                      srsPrediction.setPhysiciansOwnOpinionRisk(dp.getLakarbedomningRisk().value());
                    }
                  }
                }
                predictions.add(srsPrediction);
              });
    }

    if (filter.isAtgardsrekommendation()) {
      atgarderDiagnosisCode =
          underlag.getAtgardsrekommendationer().getRekommendation().stream()
              .map(Atgardsrekommendation::getDiagnos)
              .filter(Objects::nonNull)
              .map(CVType::getCode)
              .findAny()
              .orElse(null);
      Map<Atgardstyp, List<Atgard>> tmp =
          underlag.getAtgardsrekommendationer().getRekommendation().stream()
              .flatMap(a -> a.getAtgard().stream())
              .collect(Collectors.groupingBy(Atgard::getAtgardstyp));
      if (tmp.containsKey(Atgardstyp.OBS)) {
        atgarderObs =
            tmp.get(Atgardstyp.OBS).stream()
                .sorted(Comparator.comparing(Atgard::getPrioritet))
                .map(
                    (atgard) ->
                        SrsRecommendation.create(
                            atgard.getAtgardsrubrik(), atgard.getAtgardsforslag()))
                .collect(Collectors.toList());
      } else {
        atgarderObs = Collections.emptyList();
      }

      if (tmp.containsKey(Atgardstyp.REK)) {
        atgarderRek =
            tmp.get(Atgardstyp.REK).stream()
                .sorted(Comparator.comparing(Atgard::getPrioritet))
                .map(
                    (atgard) ->
                        SrsRecommendation.create(
                            atgard.getAtgardsrubrik(), atgard.getAtgardsforslag()))
                .collect(Collectors.toList());
      } else {
        atgarderRek = Collections.emptyList();
      }

      if (tmp.containsKey(Atgardstyp.FRL)) {
        atgarderFrl =
            tmp.get(Atgardstyp.FRL).stream()
                .sorted(Comparator.comparing(Atgard::getPrioritet))
                .map(
                    (atgard) ->
                        SrsRecommendation.create(
                            atgard.getAtgardsrubrik(), atgard.getAtgardsforslag()))
                .collect(Collectors.toList());
      } else {
        atgarderFrl = Collections.emptyList();
      }

      if (tmp.containsKey(Atgardstyp.REH)) {
        atgarderReh =
            tmp.get(Atgardstyp.REH).stream()
                .sorted(Comparator.comparing(Atgard::getPrioritet))
                .map(
                    (atgard) ->
                        SrsRecommendation.create(
                            atgard.getAtgardsrubrik(), atgard.getAtgardsforslag()))
                .collect(Collectors.toList());
      } else {
        atgarderReh = Collections.emptyList();
      }

      // They are all for the same diagnosis and all have the same code.
      atgarderStatusCode =
          underlag.getAtgardsrekommendationer().getRekommendation().stream()
              .map(Atgardsrekommendation::getAtgardsrekommendationstatus)
              .map(Atgardsrekommendationstatus::toString)
              .findAny()
              .orElse(null);
    }

    if (filter.isStatistik()
        && underlag.getStatistik() != null
        && !CollectionUtils.isEmpty(underlag.getStatistik().getDiagnosstatistik())) {
      statistikDiagnosisCode =
          Optional.ofNullable(underlag.getStatistik().getDiagnosstatistik().get(0).getDiagnos())
              .map(CVType::getCode)
              .orElse(null);
      statistikStatusCode =
          underlag.getStatistik().getDiagnosstatistik().get(0).getStatistikstatus().toString();
      statistikNationellStatistik =
          underlag.getStatistik().getDiagnosstatistik().get(0).getData().stream()
              .map((d) -> d.getIndividerAckumulerat().intValue())
              .collect(Collectors.toList());
    }
    return new SrsResponse(
        atgarderObs,
        atgarderRek,
        atgarderFrl,
        atgarderReh,
        predictions,
        atgarderDiagnosisCode,
        atgarderStatusCode,
        statistikDiagnosisCode,
        statistikStatusCode,
        statistikNationellStatistik);
  }

  @Override
  public List<SrsQuestion> getQuestions(String diagnos, String modelVersion) {
    GetPredictionQuestionsRequestType request = new GetPredictionQuestionsRequestType();
    request.setDiagnos(createDiagnos(diagnos));
    if (StringUtils.isNotBlank(modelVersion)) {
      request.setPrediktionsmodellversion(modelVersion);
    }
    GetPredictionQuestionsResponseType response = getPrediction.getPredictionQuestions(request);
    return response.getPrediktionsfraga().stream()
        .map(SrsQuestion::convert)
        .sorted(Comparator.comparing(SrsQuestion::getPriority))
        .collect(Collectors.toList());
  }

  @Override
  public Samtyckesstatus getConsent(String careUnitHsaId, Personnummer personId)
      throws InvalidPersonNummerException {
    GetConsentResponseType response =
        getConsent.getConsent(createGetConsentRequest(careUnitHsaId, personId));
    return response.getSamtyckesstatus();
  }

  @Override
  public ResultCodeEnum setConsent(String careUnitHsaId, Personnummer personId, boolean samtycke)
      throws InvalidPersonNummerException {
    SetConsentResponseType resp =
        setConsent.setConsent(createSetConsentRequest(careUnitHsaId, personId, samtycke));
    return resp.getResultCode();
  }

  @Override
  public ResultCodeEnum setOwnOpinion(
      String careGiverHsaId,
      String careUnitHsaId,
      String certificateId,
      String diagnosisCode,
      EgenBedomningRiskType ownOpinion) {
    SetOwnOpinionResponseType resp =
        setOwnOpinion.setOwnOpinion(
            createSetOwnOpinionRequest(
                careGiverHsaId, careUnitHsaId, certificateId, diagnosisCode, ownOpinion));
    return resp.getResultCode();
  }

  @Override
  public List<String> getAllDiagnosisCodes(String modelVersion) {
    GetDiagnosisCodesRequestType request = new GetDiagnosisCodesRequestType();
    if (StringUtils.isNotBlank(modelVersion)) {
      request.setPrediktionsmodellversion(modelVersion);
    }
    GetDiagnosisCodesResponseType response = getDiagnosisCodes.getDiagnosisCodes(request);
    return response.getDiagnos().stream().map(CVType::getCode).collect(Collectors.toList());
  }

  @Override
  public SrsForDiagnosisResponse getSrsForDiagnose(String diagnosCode) {

    if (diagnosCode == null || diagnosCode.isEmpty()) {
      throw new IllegalArgumentException("diagnosCode is required to construct a valid request.");
    }

    GetSRSInformationForDiagnosisRequestType request =
        new GetSRSInformationForDiagnosisRequestType();
    request.setDiagnos(createDiagnos(diagnosCode));
    GetSRSInformationForDiagnosisResponseType response =
        getSRSInformationForDiagnosis.getSRSInformationForDiagnosis(request);

    if (response.getResultCode() != ResultCodeEnum.OK) {
      throw new IllegalArgumentException("Bad data from SRS");
    }

    return createDiagnoseResponse(response);
  }

  private SrsForDiagnosisResponse createDiagnoseResponse(
      GetSRSInformationForDiagnosisResponseType response) {
    String resultDiagnosCode = null;

    // We need a null-check here in case there were no info available for the requested diagnosis
    // code.
    if (hasAtgardsrekommendationWithDiagnosisCode(response)) {
      resultDiagnosCode = response.getAtgardsrekommendation().getDiagnos().getCode();
    }

    String atgarderStatusCode;
    String statistikStatusCode;
    String statistikDiagnosCode;

    // Ugh. maybe we should create common xsd types for these common subtypes...
    final Atgardsrekommendation atgardsrekommendation = response.getAtgardsrekommendation();

    // filter out all OBS types sorted by priority
    final List<String> atgarderObs =
        atgardsrekommendation.getAtgard().stream()
            .sorted(Comparator.comparing(Atgard::getPrioritet))
            .filter(a -> a.getAtgardstyp().equals(Atgardstyp.OBS))
            .map(Atgard::getAtgardsforslag)
            .collect(Collectors.toList());

    // filter out all REK types sorted by priority
    final List<String> atgarderRek =
        atgardsrekommendation.getAtgard().stream()
            .sorted(Comparator.comparing(Atgard::getPrioritet))
            .filter(a -> a.getAtgardstyp().equals(Atgardstyp.REK))
            .map(Atgard::getAtgardsforslag)
            .collect(Collectors.toList());

    atgarderStatusCode = atgardsrekommendation.getAtgardsrekommendationstatus().name();

    if (response.getStatistik() != null
        && !CollectionUtils.isEmpty(response.getStatistik().getDiagnosstatistik())) {
      final Diagnosstatistik diagnosstatistik =
          response.getStatistik().getDiagnosstatistik().get(0);
      statistikStatusCode = diagnosstatistik.getStatistikstatus().name();
      statistikDiagnosCode =
          diagnosstatistik.getDiagnos() != null ? diagnosstatistik.getDiagnos().getCode() : null;
    } else {
      statistikStatusCode = Statistikstatus.STATISTIK_SAKNAS.name();
      statistikDiagnosCode = null;
    }

    return new SrsForDiagnosisResponse(
        atgarderObs,
        atgarderRek,
        resultDiagnosCode,
        atgarderStatusCode,
        statistikStatusCode,
        statistikDiagnosCode);
  }

  private boolean hasAtgardsrekommendationWithDiagnosisCode(
      GetSRSInformationForDiagnosisResponseType response) {
    return response.getAtgardsrekommendation().getAtgardsrekommendationstatus()
        != Atgardsrekommendationstatus.INFORMATION_SAKNAS
        && response.getAtgardsrekommendation().getDiagnos() != null;
  }

  private GetSRSInformationRequestType createRequest(
      IntygUser user,
      Personnummer personnummer,
      List<SrsCertificate> certDiags,
      Utdatafilter filter,
      List<SrsQuestionResponse> questions,
      Integer daysIntoSickLeave)
      throws InvalidPersonNummerException {

    GetSRSInformationRequestType request = new GetSRSInformationRequestType();
    request.setVersion("2.0");
    request.setKonsumentId(createHsaId(CONSUMER_HSA_ID));

    request.setAnvandareId(createHsaId(user.getHsaId()));

    Prediktionsfaktorer faktorer = new Prediktionsfaktorer();
    faktorer.setSjukskrivningsdag(daysIntoSickLeave);
    faktorer.setPostnummer(getPostnummer(user));
    faktorer
        .getFragasvar()
        .addAll(questions.stream().map(SrsQuestionResponse::convert).collect(Collectors.toList()));
    request.setPrediktionsfaktorer(faktorer);

    Individfaktorer individer = new Individfaktorer();
    Individ individ = new Individ();
    certDiags.forEach(
        cd ->
            individ
                .getDiagnosintyg()
                .add(
                    createDiagnosintyg(
                        cd.getMainDiagnosisCode(),
                        cd.getCertificateId(),
                        user.getValdVardenhet().getId())));
    individ.setPersonId(personnummer.getPersonnummer());
    individer.getIndivid().add(individ);

    request.setUtdatafilter(filter);

    request.setIndivider(individer);
    return request;
  }

  protected String formatPostnummer(String postnummer) {
    if (Strings.isNullOrEmpty(postnummer)) {
      throw new IllegalArgumentException("Postnummer was null or empty");
    }
    String trimmed = postnummer.replace(" ", "");
    if (trimmed.length() != POSTNUMMER_LENGTH) {
      throw new IllegalArgumentException("The trimmed postnummer has incorrect length");
    }
    return trimmed;
  }

  private String getPostnummer(Vardenhet vardenhet) {
    return vardenhet.getPostnummer();
  }

  protected String getPostnummer(Mottagning mottagning, Vardgivare vardgivare) {
    if (!Strings.isNullOrEmpty(mottagning.getPostnummer())) {
      return mottagning.getPostnummer();
    } else {
      String parentHsaId = mottagning.getParentHsaId();
      SelectableVardenhet parent = vardgivare.findVardenhet(parentHsaId);
      if (parent instanceof Vardenhet) {
        return getPostnummer((Vardenhet) parent);
      } else {
        throw new IllegalArgumentException("Parent of Mottagning was not of type Vardenhet");
      }
    }
  }

  protected String getPostnummer(IntygUser user) {
    if (user.getValdVardenhet() instanceof Vardenhet) {
      final String postnummer = getPostnummer((Vardenhet) user.getValdVardenhet());
      return formatPostnummer(postnummer);
    } else if (user.getValdVardenhet() instanceof Mottagning) {
      final String postnummer =
          getPostnummer(
              (Mottagning) user.getValdVardenhet(), (Vardgivare) user.getValdVardgivare());
      return formatPostnummer(postnummer);
    } else {
      throw new IllegalStateException(
          "Selected unit was neither of type Vardenhet nor Mottagning while trying to get postnummer");
    }
  }

  private HsaId createHsaId(String hsaIdCode) {
    HsaId hsaId = new HsaId();
    hsaId.setRoot(HSA_ROOT);
    hsaId.setExtension(hsaIdCode);
    return hsaId;
  }

  private SetOwnOpinionRequestType createSetOwnOpinionRequest(
      String careGiverHsaId,
      String careUnitHsaId,
      String certificateId,
      String diagnosis,
      EgenBedomningRiskType opinion) {
    SetOwnOpinionRequestType request = new SetOwnOpinionRequestType();
    request.setVardgivareId(createHsaId(careGiverHsaId));
    request.setVardenhetId(createHsaId(careUnitHsaId));
    request.setDiagnos(createDiagnos(diagnosis));
    IntygId intyg = new IntygId();
    intyg.setExtension(certificateId);
    intyg.setRoot(careUnitHsaId);
    request.setIntygId(intyg);
    request.setEgenBedomningRisk(opinion);
    return request;
  }

  private SetConsentRequestType createSetConsentRequest(
      String careUnitHsaId, Personnummer personId, boolean samtycke)
      throws InvalidPersonNummerException {
    SetConsentRequestType request = new SetConsentRequestType();
    HsaId hsaId = createHsaId(careUnitHsaId);
    request.setVardenhetId(hsaId);
    request.setPersonId(personId.getPersonnummer());
    request.setSamtycke(samtycke);
    return request;
  }

  private GetConsentRequestType createGetConsentRequest(
      String careUnitHsaId, Personnummer personnummer) throws InvalidPersonNummerException {
    GetConsentRequestType request = new GetConsentRequestType();
    HsaId hsaId = new HsaId();
    hsaId.setExtension(careUnitHsaId);
    hsaId.setRoot(HSA_ROOT);
    request.setVardenhetId(hsaId);
    request.setPersonId(personnummer.getPersonnummer());
    return request;
  }

  private Diagnos createDiagnos(String diagnosisCode) {
    Diagnos diagnos = new Diagnos();
    diagnos.setCode(diagnosisCode);
    diagnos.setCodeSystem(DIAGNOS_CODE_SYSTEM);
    return diagnos;
  }

  private Diagnosintyg createDiagnosintyg(
      String diagnosisCode, String certificateId, String careUnitId) {
    IntygId intygId = new IntygId();
    intygId.setExtension(certificateId);
    intygId.setRoot(careUnitId);

    Diagnosintyg diagnosintyg = new Diagnosintyg();
    diagnosintyg.setDiagnos(createDiagnos(diagnosisCode));
    diagnosintyg.setIntygId(intygId);
    return diagnosintyg;
  }
}