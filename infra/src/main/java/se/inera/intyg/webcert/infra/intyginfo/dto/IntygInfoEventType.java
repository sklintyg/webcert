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
package se.inera.intyg.webcert.infra.intyginfo.dto;

public enum IntygInfoEventType {
  IS001, // Intygsutkastet skapades
  IS003, // Intygsutkast låstes
  IS004, // Intyget signerades
  IS005, // Intygstjänsten tog emot intyget
  IS006, // Intyget skickades till intygsmottagaren
  IS007, // Intyget förnyades
  IS008, // Intyget ersattes
  IS009, // Intyget makulerades
  IS010, // Intygsutkastet ändrades
  IS011, // Kompletteringsbegäran kom in från intygsmottagaren
  IS012, // Administrativ fråga kom in från intygsmottagaren
  IS013, // Administrativ fråga skickades till intygsmottagaren
  IS014, // Intyget kompletterades med nytt intyg av vården
  IS015, // Intyget kompletterades med meddelande av vården
  IS016, // Komplettering hanterades av vården
  IS017, // Administrativ fråga hanterades av vården
  IS018, // Intygsutkast markerades som klart för signering
  IS019, // Utkastet skapades för att förnya intyg
  IS020, // Utkastet skapades för att ersätta intyg
  IS021, // Utkastet skapades för att komplettera intyg
  IS022, // Utkastet skapades som en kopia på
  IS023, // Administrativ fråga besvarad av vården
  IS024, // Administrativ fråga besvarades av intygsmottagaren
  IS025, // Administrativ fråga från vården markerades som hanterad av vården
  IS026, // Utkastet kopierades av

  IS101, // Notifiering SKAPAT skickades till journalsystem
  IS102, // Notifiering ANDRAT skickades till journalsystem
  IS103, // Notifiering RADERA skickades till journalsystem
  IS104, // Notifiering LAST skickades till journalsystem
  IS105, // Notifiering KFSIGN skickades till journalsystem
  IS106, // Notifiering SIGNAT skickades till journalsystem
  IS107, // Notifiering SKICKA skickades till journalsystem
  IS108, // Notifiering MAKULE skickades till journalsystem
  IS109, // Notifiering NYFRFM skickades till journalsystem
  IS110, // Notifiering NYFRFV skickades till journalsystem
  IS111, // Notifiering NYSVFM skickades till journalsystem
  IS112, // Notifiering HANFRFM skickades till journalsystem
  IS113, // Notifiering HANFRFV skickades till journalsystem
}
