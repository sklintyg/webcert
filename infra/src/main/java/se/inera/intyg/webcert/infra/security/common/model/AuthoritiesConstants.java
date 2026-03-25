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
package se.inera.intyg.webcert.infra.security.common.model;

public final class AuthoritiesConstants {

  // Titles, a.k.a 'legitimerad yrkesgrupp', has a coding system governing these titles. See:
  // HSA Innehåll Legitimerad yrkesgrupp
  // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
  public static final String TITLE_LAKARE = "Läkare";
  public static final String TITLE_TANDLAKARE = "Tandläkare";
  public static final String TITLE_SJUKSKOTERSKA = "Sjuksköterska";
  public static final String TITLE_BARNMORSKA = "Barnmorska";

  // Title codes, a.k.a 'befattningskod', has a coding system governing these codes. See:
  // HSA Innehåll Befattning
  // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
  public static final String TITLECODE_BT_LAKARE = "203020";
  public static final String TITLECODE_AT_LAKARE = "204010";

  // Known roles (these roles are copied from authorities.yaml which is the master authorities
  // configuration)
  public static final String ROLE_LAKARE = "LAKARE";
  public static final String ROLE_PRIVATLAKARE = "PRIVATLAKARE";
  public static final String ROLE_TANDLAKARE = "TANDLAKARE";
  public static final String ROLE_ADMIN = "VARDADMINISTRATOR";
  public static final String ROLE_SJUKSKOTERSKA = "SJUKSKOTERSKA";
  public static final String ROLE_BARNMORSKA = "BARNMORSKA";
  public static final String ROLE_KOORDINATOR = "REHABKOORDINATOR";
  public static final String ROLE_PRIVATLAKARE_OBEHORIG = "PRIVATLAKARE_OBEHORIG";

  // Known privileges (these privileges are copied from authorities.yaml which is the master
  // authorities
  // configuration)
  // Note: not all privileges are mapped, only the ones actually used in backend
  public static final String PRIVILEGE_VISA_INTYG = "VISA_INTYG";
  public static final String PRIVILEGE_SIGNERA_INTYG = "SIGNERA_INTYG";
  public static final String PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA = "BESVARA_KOMPLETTERINGSFRAGA";
  public static final String PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR = "VIDAREBEFORDRA_FRAGASVAR";
  public static final String PRIVILEGE_VIDAREBEFORDRA_UTKAST = "VIDAREBEFORDRA_UTKAST";
  public static final String PRIVILEGE_MAKULERA_INTYG = "MAKULERA_INTYG";
  public static final String PRIVILEGE_FORNYA_INTYG = "FORNYA_INTYG";
  public static final String PRIVILEGE_ERSATTA_INTYG = "ERSATTA_INTYG";
  public static final String PRIVILEGE_SVARA_MED_NYTT_INTYG = "SVARA_MED_NYTT_INTYG";
  public static final String PRIVILEGE_SKRIVA_INTYG = "SKRIVA_INTYG";
  public static final String PRIVILEGE_NOTIFIERING_UTKAST = "NOTIFIERING_UTKAST";
  public static final String PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT =
      "HANTERA_SEKRETESSMARKERAD_PATIENT";
  public static final String PRIVILEGE_GODKANNA_MOTTAGARE = "GODKANNA_MOTTAGARE";
  public static final String PRIVILEGE_SKAPA_NYFRAGA = "SKAPA_NY_FRAGA";
  public static final String PRIVILEGE_LASA_FRAGA = "LASA_FRAGA";
  public static final String PRIVILEGE_BESVARA_FRAGA = "BESVARA_FRAGA";
  public static final String PRIVILEGE_COPY_FROM_CANDIDATE = "KOPIERA_FRAN_KANDIDAT";
  public static final String PRIVILEGE_KOPIERA_LAST_UTKAST = "KOPIERA_LAST_UTKAST";
  public static final String PRIVILEGE_MARKERA_KOMPLETTERING_SOM_HANTERAD =
      "MARKERA_KOMPLETTERING_SOM_HANTERAD";
  public static final String PRIVILEGE_MARKERA_FRAGA_SOM_HANTERAD = "MARKERA_FRAGA_SOM_HANTERAD";

  public static final String FEATURE_JS_LOGGNING = "JS_LOGGING";
  public static final String FEATURE_HANTERA_INTYGSUTKAST = "HANTERA_INTYGSUTKAST";
  public static final String FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN = "HANTERA_INTYGSUTKAST_AVLIDEN";
  public static final String FEATURE_HANTERA_FRAGOR = "HANTERA_FRAGOR";
  public static final String FEATURE_SKAPA_NYFRAGA = "SKAPA_NY_FRAGA";
  public static final String FEATURE_SRS = "SRS";
  public static final String FEATURE_FORNYA_INTYG = "FORNYA_INTYG";
  public static final String FEATURE_SKICKA_INTYG = "SKICKA_INTYG";
  public static final String FEATURE_ARBETSGIVARUTSKRIFT = "ARBETSGIVARUTSKRIFT";
  public static final String FEATURE_UTSKRIFT = "UTSKRIFT";
  public static final String FEATURE_MAKULERA_INTYG = "MAKULERA_INTYG";
  public static final String FEATURE_SIGNERA_SKICKA_DIREKT = "SIGNERA_SKICKA_DIREKT";
  public static final String FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING =
      "MAKULERA_INTYG_KRAVER_ANLEDNING";
  public static final String FEATURE_UNIKT_INTYG = "UNIKT_INTYG";
  public static final String FEATURE_UNIKT_INTYG_INOM_VG = "UNIKT_INTYG_INOM_VG";
  public static final String FEATURE_UNIKT_UTKAST_INOM_VG = "UNIKT_UTKAST_INOM_VG";
  public static final String FEATURE_UNIKT_UNDANTAG_OM_SENASTE_INTYG =
      "FEATURE_UNIKT_UNDANTAG_OM_SENASTE_INTYG";
  public static final String FEATURE_TAK_KONTROLL_TRADKLATTRING = "TAK_KONTROLL_TRADKLATTRING";
  public static final String FEATURE_TAK_KONTROLL = "TAK_KONTROLL";
  public static final String FEATURE_SEKRETESSMARKERING = "SEKRETESSMARKERING";
  public static final String FEATURE_OAUTH_AUTHENTICATION = "OAUTH_AUTHENTICATION";
  public static final String FEATURE_ENABLE_CREATE_DRAFT_PREFILL = "CREATE_DRAFT_PREFILL";

  /** Feature to toggle if patients with testIndicator flag is allowed to be used or not. */
  public static final String FEATURE_ALLOW_TEST_INDICATED_PERSON = "TILLAT_VALIDERINGSPERSON";

  /**
   * Feature to toggle if a u'DJUPINTEGRERAD' care provider should recieve a warning when logging in
   * with origin 'NORMAL'
   */
  public static final String FEATURE_ENABLE_WARNING_ORIGIN_NORMAL = "VARNING_FRISTAENDE";

  /**
   * Feature to toggle if a 'DJUPINTEGRERAD' care provider should block certificate features when
   * logging in with origin 'NORMAL'
   */
  public static final String FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL = "BLOCKERA_FRISTAENDE";

  public static final String FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION =
      "INAKTIVERA_TIDIGARE_HUVUDVERSION";
  public static final String FEATURE_INACTIVE_CERTIFICATE_TYPE = "INAKTIVERAD_INTYGSTYP";
  public static final String FEATURE_USE_ANGULAR_WEBCLIENT = "ANVAND_ANGULAR_WEBKLIENT";
  public static final String FEATURE_USE_REACT_WEBCLIENT_FRISTAENDE =
      "ANVAND_REACT_WEBKLIENT_FRISTAENDE";
  public static final String FEATURE_PRINT_IN_IFRAME = "SKRIV_UT_I_IFRAME";

  private AuthoritiesConstants() {}
}
