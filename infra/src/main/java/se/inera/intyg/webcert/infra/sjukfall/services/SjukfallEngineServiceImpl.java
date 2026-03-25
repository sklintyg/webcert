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
package se.inera.intyg.webcert.infra.sjukfall.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.Patient;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.infra.sjukfall.dto.SjukfallPatient;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.infra.sjukfall.dto.Vardgivare;
import se.inera.intyg.infra.sjukfall.engine.SjukfallIntygEnhetCreator;
import se.inera.intyg.infra.sjukfall.engine.SjukfallIntygEnhetResolver;
import se.inera.intyg.infra.sjukfall.engine.SjukfallIntygPatientCreator;
import se.inera.intyg.infra.sjukfall.engine.SjukfallIntygPatientResolver;
import se.inera.intyg.infra.sjukfall.engine.SjukfallLangdCalculator;

/**
 * @author Magnus Ekstrand on 2017-02-10.
 */
@Service("sjukfallEngineService")
public class SjukfallEngineServiceImpl implements SjukfallEngineService {

  private static final Logger LOG = LoggerFactory.getLogger(SjukfallEngineServiceImpl.class);

  protected Clock clock;

  private SjukfallIntygEnhetResolver resolverEnhet;
  private SjukfallIntygPatientResolver resolverPatient;

  public SjukfallEngineServiceImpl() {
    clock = Clock.system(ZoneId.systemDefault());
    resolverEnhet = new SjukfallIntygEnhetResolver(new SjukfallIntygEnhetCreator());
    resolverPatient = new SjukfallIntygPatientResolver(new SjukfallIntygPatientCreator());
  }

  // api

  @Override
  public List<SjukfallEnhet> beraknaSjukfallForEnhet(
      List<IntygData> intygsData, IntygParametrar parameters) {
    LOG.debug("Start calculation of sjukfall for health care unit...");

    LocalDate aktivtDatum = parameters.getAktivtDatum();

    Map<String, List<SjukfallIntyg>> resolvedIntygsData =
        resolverEnhet.resolve(intygsData, parameters);

    // Assemble SjukfallEnhet objects
    List<SjukfallEnhet> result =
        assembleSjukfallEnhetList(resolvedIntygsData, aktivtDatum, parameters);

    LOG.debug("...stop calculation of sjukfall for health care unit.");
    return result;
  }

  @Override
  public List<SjukfallPatient> beraknaSjukfallForPatient(
      List<IntygData> intygData, IntygParametrar parameters) {
    LOG.debug("Start calculation of sjukfall for a patient...");

    int maxIntygsGlapp = parameters.getMaxIntygsGlapp();
    LocalDate aktivtDatum = parameters.getAktivtDatum();

    Map<Integer, List<SjukfallIntyg>> resolvedIntygsData =
        resolverPatient.resolve(intygData, maxIntygsGlapp, aktivtDatum);

    // Assemble SjukfallPatient objects
    List<SjukfallPatient> result =
        assembleSjukfallPatientList(resolvedIntygsData, aktivtDatum, parameters);

    LOG.debug("...stop calculation of sjukfall for a patient.");
    return result;
  }

  // package scope

  SjukfallEnhet buildSjukfallEnhet(
      List<SjukfallIntyg> values,
      SjukfallIntyg aktivtIntyg,
      LocalDate aktivtDatum,
      IntygParametrar intygParametrar) {
    SjukfallEnhet sjukfallEnhet = new SjukfallEnhet();
    sjukfallEnhet.setVardgivare(getVardgivare(aktivtIntyg));
    sjukfallEnhet.setVardenhet(getVardenhet(aktivtIntyg));
    sjukfallEnhet.setLakare(getLakare(aktivtIntyg));
    sjukfallEnhet.setPatient(getPatient(aktivtIntyg));
    sjukfallEnhet.setDiagnosKod(aktivtIntyg.getDiagnosKod());
    sjukfallEnhet.setBiDiagnoser(aktivtIntyg.getBiDiagnoser());
    sjukfallEnhet.setStart(getMinimumDate(values));
    sjukfallEnhet.setSlut(getMaximumDate(values));
    sjukfallEnhet.setDagar(
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(
            values, intygParametrar.getMaxIntygsGlapp()));
    sjukfallEnhet.setIntyg(values.size());
    sjukfallEnhet.setIntygLista(
        values.stream().map(IntygData::getIntygId).collect(Collectors.toList()));
    sjukfallEnhet.setGrader(getGrader(aktivtIntyg.getFormagor()));
    if (!aktivtIntyg.isNyligenAvslutat()) {
      sjukfallEnhet.setAktivGrad(getAktivGrad(aktivtIntyg.getFormagor(), aktivtDatum));
    }
    sjukfallEnhet.setAktivIntygsId(aktivtIntyg.getIntygId());
    sjukfallEnhet.setSysselsattning(aktivtIntyg.getSysselsattning());
    return sjukfallEnhet;
  }

  SjukfallPatient buildSjukfallPatient(
      List<SjukfallIntyg> values, IntygParametrar intygParametrar) {

    Patient patient = getPatient(values.get(0));
    DiagnosKod diagnosKod = resolveDiagnosKod(values);

    SjukfallPatient sjukfallPatient = new SjukfallPatient();
    sjukfallPatient.setPatient(patient);
    sjukfallPatient.setDiagnosKod(diagnosKod);
    sjukfallPatient.setStart(getMinimumDate(values));
    sjukfallPatient.setSlut(getMaximumDate(values));
    sjukfallPatient.setDagar(
        SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(
            values, intygParametrar.getMaxIntygsGlapp()));
    sjukfallPatient.setSjukfallIntygList(sortIntyg(values));

    return sjukfallPatient;
  }

  private List<SjukfallIntyg> sortIntyg(List<SjukfallIntyg> intyg) {
    // Make sort order descending
    Comparator<SjukfallIntyg> dateComparator =
        Comparator.comparing(SjukfallIntyg::getStartDatum, Comparator.reverseOrder());

    return intyg.stream().sorted(dateComparator).collect(Collectors.toList());
  }

  private DiagnosKod resolveDiagnosKod(List<SjukfallIntyg> intyg) {
    // Rules:
    // 1. If several intyg in list are active, choose the active
    //    intyg with latest signeringsTidpunkt
    // 2. If list doesn't have an active intyg, choose the one
    //    with latest signeringsTidpunkt
    List<SjukfallIntyg> list =
        intyg.stream().filter(SjukfallIntyg::isAktivtIntyg).collect(Collectors.toList());
    if (list.isEmpty()) {
      list.addAll(intyg);
    }

    return list.stream()
        .max(Comparator.comparing(SjukfallIntyg::getSigneringsTidpunkt))
        .get()
        .getDiagnosKod();
  }

  // private scope

  private Patient getPatient(SjukfallIntyg sjukfallIntyg) {
    String id = StringUtils.trim(sjukfallIntyg.getPatientId());
    String namn = sjukfallIntyg.getPatientNamn();

    return Patient.create(id, namn);
  }

  private List<SjukfallEnhet> assembleSjukfallEnhetList(
      Map<String, List<SjukfallIntyg>> intygsData,
      LocalDate aktivtDatum,
      IntygParametrar intygParametrar) {
    LOG.debug("  - Assembling 'sjukfall for healt care unit'");

    return intygsData.entrySet().stream()
        .map(e -> toSjukfallEnhet(e.getValue(), aktivtDatum, intygParametrar))
        .collect(Collectors.toList());
  }

  private List<SjukfallPatient> assembleSjukfallPatientList(
      Map<Integer, List<SjukfallIntyg>> intygsData,
      LocalDate aktivtDatum,
      IntygParametrar intygParametrar) {
    LOG.debug("  - Assembling 'sjukfall for patient'");

    Comparator<SjukfallPatient> dateComparator =
        Comparator.comparing(SjukfallPatient::getStart, Comparator.reverseOrder());

    // 1. Build sjukfall for patient object
    // 2. Filter out any future sjukfall
    // 3. Sort by start date with descending order
    return intygsData.entrySet().stream()
        .map(e -> buildSjukfallPatient(e.getValue(), intygParametrar))
        .filter(sjukfall -> aktivtDatum.plusDays(1).isAfter(sjukfall.getStart()))
        .sorted(dateComparator)
        .collect(Collectors.toList());
  }

  private SjukfallEnhet toSjukfallEnhet(
      List<SjukfallIntyg> list, LocalDate aktivtDatum, IntygParametrar intygParametrar) {

    // 1. Find the active object
    SjukfallIntyg aktivtIntyg =
        list.stream().filter(SjukfallIntyg::isAktivtIntyg).findFirst().orElse(null);

    if (aktivtIntyg == null) {
      aktivtIntyg =
          list.stream()
              .filter(SjukfallIntyg::isNyligenAvslutat)
              .findFirst()
              .orElseThrow(
                  () ->
                      new SjukfallEngineServiceException(
                          "Unable to find a 'aktivt eller nyligen avslutat intyg'"));
    }

    // 2. Build sjukfall for enhet object
    return buildSjukfallEnhet(list, aktivtIntyg, aktivtDatum, intygParametrar);
  }

  private Vardgivare getVardgivare(SjukfallIntyg sjukfallIntyg) {
    return Vardgivare.create(sjukfallIntyg.getVardgivareId(), sjukfallIntyg.getVardgivareNamn());
  }

  private Vardenhet getVardenhet(SjukfallIntyg sjukfallIntyg) {
    return Vardenhet.create(sjukfallIntyg.getVardenhetId(), sjukfallIntyg.getVardenhetNamn());
  }

  private Lakare getLakare(SjukfallIntyg sjukfallIntyg) {
    return Lakare.create(sjukfallIntyg.getLakareId(), sjukfallIntyg.getLakareNamn());
  }

  private Integer getAktivGrad(List<Formaga> list, LocalDate aktivtDatum) {
    LOG.debug("  - Lookup 'aktiv grad'");
    return list.stream()
        .filter(
            f ->
                f.getStartdatum().compareTo(aktivtDatum) < 1
                    && f.getSlutdatum().compareTo(aktivtDatum) > -1)
        .findFirst()
        .orElseThrow(
            () -> new SjukfallEngineServiceException("Unable to find an active 'arbetsförmåga'"))
        .getNedsattning();
  }

  private List<Integer> getGrader(List<Formaga> list) {
    LOG.debug("  - Lookup all 'aktiva grader'");
    return list.stream()
        .sorted(Comparator.comparing(Formaga::getStartdatum))
        .map(Formaga::getNedsattning)
        .collect(Collectors.toList());
  }

  private LocalDate getMinimumDate(List<SjukfallIntyg> list) {
    return list.stream()
        .min(Comparator.comparing(SjukfallIntyg::getStartDatum))
        .get()
        .getStartDatum();
  }

  private LocalDate getMaximumDate(List<SjukfallIntyg> list) {
    return list.stream()
        .max(Comparator.comparing(SjukfallIntyg::getSlutDatum))
        .get()
        .getSlutDatum();
  }
}
