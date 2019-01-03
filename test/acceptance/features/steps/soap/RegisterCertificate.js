/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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


'use strict';

const smiIntyg = {
    'Läkarutlåtande för sjukersättning': function(patient, user, intyg) {
        return `<urn2:RegisterCertificate 
               xmlns="urn:riv:clinicalprocess:healthcond:certificate:3" 
                xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3" 
                xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:types:3" 
                xmlns:urn4="urn:riv:itintegration:registry:1">
         <urn2:intyg>
            <intygs-id>
               <urn3:root>` + user.enhetId + `</urn3:root>
               <urn3:extension>` + intyg.id + `</urn3:extension>
            </intygs-id>
            <typ>
               <urn3:code>LUSE</urn3:code>
               <urn3:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn3:codeSystem>
               <urn3:displayName>Läkarutlåtande för sjukersättning</urn3:displayName>
            </typ>
            <version>1.0</version>
            <signeringstidpunkt>2016-12-09T12:53:59</signeringstidpunkt>
            <skickatTidpunkt>2016-12-09T12:53:59</skickatTidpunkt>
            <patient>
               <person-id>
                  <urn3:root>1.2.752.129.2.1.3.1</urn3:root>
                  <urn3:extension>` + patient.id + `</urn3:extension>
               </person-id>
               <fornamn>` + patient.forNamn + ` </fornamn>
               <efternamn>` + patient.efterNamn + `</efternamn>
               <postadress>Hallloj 12</postadress>
               <postnummer>55664</postnummer>
               <postort>Tjo</postort>
            </patient>
            <skapadAv>
               <personal-id>
                  <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                  <urn3:extension>` + user.id + `</urn3:extension>
               </personal-id>
               <fullstandigtNamn>` + user.forNamn + ' ' + user.efterNamn + `</fullstandigtNamn>
               <forskrivarkod>0000000</forskrivarkod>
                 <befattning>
                  <urn3:code>205010</urn3:code>
                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>
                  <!--Optional:-->
                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>
               </befattning>
                   <befattning>
                  <urn3:code>205010</urn3:code>
                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>
                  <!--Optional:-->
                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>
               </befattning>
               <befattning>
                  <urn3:code>205010</urn3:code>
                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>
                  <!--Optional:-->
                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>
               </befattning>
                   <befattning>
                  <urn3:code>205010</urn3:code>
                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>
                  <!--Optional:-->
                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>
               </befattning>
               <enhet>
                  <enhets-id>
                     <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                     <urn3:extension>` + user.enhetId + `</urn3:extension>
                  </enhets-id>
                  <arbetsplatskod>
                     <urn3:root>1.2.752.29.4.71</urn3:root>
                     <urn3:extension>0000000</urn3:extension>
                  </arbetsplatskod>
                  <enhetsnamn>` + user.enhetName + `</enhetsnamn>
                  <postadress>Bryggaregatan 11</postadress>
                  <postnummer>65340</postnummer>
                  <postort>Karlstad</postort>
                  <telefonnummer>054203040</telefonnummer>
                  <epost>intygnmt@nordicmedtest.se</epost>
                  <vardgivare>
                     <vardgivare-id>
                        <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                        <urn3:extension>TSTNMT2321000156-107M</urn3:extension>
                     </vardgivare-id>
                     <vardgivarnamn>nmt_vg1</vardgivarnamn>
                  </vardgivare>
               </enhet>
               <specialistkompetens>
                  <urn3:code>N/A</urn3:code>
                  <urn3:displayName>Medicinsk gastroenterologi och hepatologi</urn3:displayName>
               </specialistkompetens>
            </skapadAv>
            <svar id="1">
               <instans>1</instans>
               <delsvar id="1.1">
                  <urn3:cv>
                     <urn3:code>UNDERSOKNING</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
                     <urn3:displayName>Min undersökning av patienten</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="1.2">2016-12-09</delsvar>
            </svar>
            <svar id="1">
               <instans>2</instans>
               <delsvar id="1.1">
                  <urn3:cv>
                     <urn3:code>JOURNALUPPGIFTER</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
                     <urn3:displayName>Journaluppgifter från den</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="1.2">2016-12-09</delsvar>
            </svar>
            <svar id="1">
               <instans>3</instans>
               <delsvar id="1.1">
                  <urn3:cv>
                     <urn3:code>ANHORIG</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
                     <urn3:displayName>Anhörigs beskrivning av patienten</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="1.2">2016-12-09</delsvar>
            </svar>
            <svar id="1">
               <instans>4</instans>
               <delsvar id="1.1">
                  <urn3:cv>
                     <urn3:code>ANNAT</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
                     <urn3:displayName>Annat</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="1.2">2016-12-09</delsvar>
               <delsvar id="1.3">Journal from down below</delsvar>
            </svar>
            <svar id="2">
               <delsvar id="2.1">2016-12-09</delsvar>
            </svar>
            <svar id="3">
               <delsvar id="3.1">true</delsvar>
            </svar>
            <svar id="4">
               <instans>1</instans>
               <delsvar id="4.1">
                  <urn3:cv>
                     <urn3:code>HABILITERING</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>
                     <urn3:displayName>Underlag från habiliteringen</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="4.2">2016-12-09</delsvar>
               <delsvar id="4.3">Vårdcentralen Smultronet</delsvar>
            </svar>
            <svar id="4">
               <instans>2</instans>
               <delsvar id="4.1">
                  <urn3:cv>
                     <urn3:code>ARBETSTERAPEUT</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>
                     <urn3:displayName>Underlag från arbetsterapeut</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="4.2">2016-12-09</delsvar>
               <delsvar id="4.3">CSK</delsvar>
            </svar>
            <svar id="4">
               <instans>3</instans>
               <delsvar id="4.1">
                  <urn3:cv>
                     <urn3:code>FYSIOTERAPEUT</urn3:code>
                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>
                     <urn3:displayName>Underlag från fysioterapeut</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="4.2">2016-12-09</delsvar>
               <delsvar id="4.3">CentralSjukhuset!!</delsvar>
            </svar>
            <svar id="5">
               <delsvar id="5.1">Patienten har haft besvären  Patienten känner sig trött. och har sedan dess pågått.</delsvar>
            </svar>
            <svar id="6">
               <delsvar id="6.2">
                  <urn3:cv>
                     <urn3:code>S47</urn3:code>
                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
                     <urn3:displayName>Klämskada på skuldra och överarm</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="6.1">Klämskada på skuldra och överarm</delsvar>
               <delsvar id="6.4">
                  <urn3:cv>
                     <urn3:code>F205</urn3:code>
                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
                     <urn3:displayName>Schizofrent resttillstånd</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="6.3">Schizofrent resttillstånd</delsvar>
               <delsvar id="6.6">
                  <urn3:cv>
                     <urn3:code>M659B</urn3:code>
                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
                     <urn3:displayName>Ospecifik synovit/tenosynovit i axelled/överarm</urn3:displayName>
                  </urn3:cv>
               </delsvar>
               <delsvar id="6.5">Ospecifik synovit/tenosynovit i axelled/överarm</delsvar>
            </svar>
            <svar id="7">
               <delsvar id="7.1">I tidernas begynnelse ställdes diagnosen för patienten på Sahlgrenska</delsvar>
            </svar>
            <svar id="45">
               <delsvar id="45.1">true</delsvar>
               <delsvar id="45.2">alla och ingen.</delsvar>
            </svar>
            <svar id="8">
               <delsvar id="8.1">Pga smärtan påverkas intellektet</delsvar>
            </svar>
            <svar id="9">
               <delsvar id="9.1">Får svårt att kommunicera då humöret inte är på topp</delsvar>
            </svar>
            <svar id="10">
               <delsvar id="10.1">Har svårt att koncentrera sig längre än 2 minuter</delsvar>
            </svar>
            <svar id="11">
               <delsvar id="11.1">Påverkar inget annat enligt patienten</delsvar>
            </svar>
            <svar id="12">
               <delsvar id="12.1">Har fått sämre syn med åldern</delsvar>
            </svar>
            <svar id="13">
               <delsvar id="13.1">Det gör ont - Lena PH</delsvar>
            </svar>
            <svar id="14">
               <delsvar id="14.1">Ställer till det en hel del</delsvar>
            </svar>
            <svar id="17">
               <delsvar id="17.1">Patienten kan inte räcka upp armen. Stelhet i axelpartiet</delsvar>
            </svar>
            <svar id="18">
               <delsvar id="18.1">Rheabträning</delsvar>
            </svar>
            <svar id="19">
               <delsvar id="19.1">Smärtlindring och akupunktur</delsvar>
            </svar>
            <svar id="20">
               <delsvar id="20.1">För tidigt att säga något om planerade åtgärder</delsvar>
            </svar>
            <svar id="21">
               <delsvar id="21.1">Smärtlindring</delsvar>
            </svar>
            <svar id="22">
               <delsvar id="22.1">Rehabträning ska ge resultat om 1 år</delsvar>
            </svar>
            <svar id="23">
               <delsvar id="23.1">Laga mat går bra, men endast på fredagar</delsvar>
            </svar>
            <svar id="25">
               <delsvar id="25.1">Mycket material är hemligstämplat</delsvar>
            </svar>
            <svar id="26">
               <delsvar id="26.1">true</delsvar>
               <delsvar id="26.2">Jag har hemligt material på mitt skrivbord som kan vara intressant för er</delsvar>
            </svar>
         </urn2:intyg>
      </urn2:RegisterCertificate>`;
    },
    'Läkarintyg för sjukpenning': function(patient, user, intyg) {
        return `<urn2:RegisterCertificate 
             xmlns="urn:riv:clinicalprocess:healthcond:certificate:3"
             xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3"
             xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:types:3"
             xmlns:urn4="urn:riv:itintegration:registry:1">
            <urn2:intyg>
            <intygs-id>
               <urn3:root>` + user.enhetId + `</urn3:root>
               <urn3:extension>` + intyg.id + `</urn3:extension>
            </intygs-id>
            <typ>
               <urn3:code>LISJP</urn3:code>
               <urn3:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn3:codeSystem>
            </typ>
            <version>1.0</version>
            <!--Optional:-->
            <signeringstidpunkt>2017-01-08T11:25:30</signeringstidpunkt>
            <!--Optional:-->
            <skickatTidpunkt>2017-01-08T11:25:30</skickatTidpunkt>
            <patient>
               <person-id>
                  <urn3:root>1.2.752.129.2.1.3.1</urn3:root>
                  <urn3:extension>` + patient.id + `</urn3:extension>
               </person-id>
               <fornamn>` + patient.forNamn + `</fornamn>
               <efternamn>` + patient.efterNamn + `</efternamn>
               <!--Optional:-->
               <postadress>TestvÃƒÂ¤gen 22</postadress>
               <postnummer>111 22</postnummer>
               <postort>Teststad</postort>
               <!--You may enter ANY elements at this point-->
            </patient>
            <skapadAv>
               <personal-id>
                  <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                  <urn3:extension>` + user.id + `</urn3:extension>
               </personal-id>
               <fullstandigtNamn>` + user.forNamn + ' ' + user.efterNamn + `</fullstandigtNamn>
               <!--Optional:-->
               <forskrivarkod>0000000</forskrivarkod>
               <!--Zero or more repetitions:-->
               <befattning>
                  <urn3:code>601010</urn3:code>
                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>
                  <!--Optional:-->
                  <urn3:displayName>Kock</urn3:displayName>
               </befattning>
               <enhet>
                  <enhets-id>
                     <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                     <urn3:extension>` + user.enhetId + `</urn3:extension>
                  </enhets-id>
                  <arbetsplatskod>
                     <urn3:root>1.2.752.29.4.71</urn3:root>
                     <urn3:extension>1627</urn3:extension>
                  </arbetsplatskod>
                  <enhetsnamn>` + user.enhetName + `</enhetsnamn>
                  <postadress>Bryggareg 11</postadress>
                  <postnummer>653 40</postnummer>
                  <postort>Karlstad</postort>
                  <telefonnummer>07011111111</telefonnummer>
                  <!--Optional:-->
                  <epost>?</epost>
                  <vardgivare>
                     <vardgivare-id>
                        <urn3:root>1.2.752.129.2.1.4.1</urn3:root>
                        <urn3:extension>TSTNMT2321000156-107M</urn3:extension>
                     </vardgivare-id>
                     <vardgivarnamn>nmt_vg3</vardgivarnamn>
                     <!--You may enter ANY elements at this point-->
                  </vardgivare>
                  <!--You may enter ANY elements at this point-->
               </enhet>
            </skapadAv>
				<svar id="27">
					<delsvar id="27.1">false</delsvar>
				</svar>
				<svar id="1">
					<instans>1</instans>
					<delsvar id="1.1">
						<urn3:cv>
							<urn3:code>UNDERSOKNING</urn3:code>
							<urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
							<urn3:displayName>Min undersÃ¶kning av patienten</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="1.2">2018-05-24</delsvar>
				</svar>
				<svar id="1">
					<instans>2</instans>
					<delsvar id="1.1">
						<urn3:cv>
							<urn3:code>TELEFONKONTAKT</urn3:code>
							<urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
							<urn3:displayName>Min telefonkontakt med patienten</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="1.2">2018-05-24</delsvar>
				</svar>
				<svar id="1">
					<instans>3</instans>
					<delsvar id="1.1">
						<urn3:cv>
							<urn3:code>JOURNALUPPGIFTER</urn3:code>
							<urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
							<urn3:displayName>Journaluppgifter frÃ¥n den</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="1.2">2018-05-24</delsvar>
				</svar>
				<svar id="1">
					<instans>4</instans>
					<delsvar id="1.1">
						<urn3:cv>
							<urn3:code>ANNAT</urn3:code>
							<urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>
							<urn3:displayName>Annat</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="1.2">2018-05-24</delsvar>
					<delsvar id="1.3">Journal from down below</delsvar>
				</svar>
				<svar id="28">
					<instans>1</instans>
					<delsvar id="28.1">
						<urn3:cv>
							<urn3:code>NUVARANDE_ARBETE</urn3:code>
							<urn3:codeSystem>KV_FKMU_0002</urn3:codeSystem>
							<urn3:displayName>Nuvarande arbete</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="28">
					<instans>2</instans>
					<delsvar id="28.1">
						<urn3:cv>
							<urn3:code>ARBETSSOKANDE</urn3:code>
							<urn3:codeSystem>KV_FKMU_0002</urn3:codeSystem>
							<urn3:displayName>ArbetssÃ¶kande</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="28">
					<instans>3</instans>
					<delsvar id="28.1">
						<urn3:cv>
							<urn3:code>FORALDRALEDIG</urn3:code>
							<urn3:codeSystem>KV_FKMU_0002</urn3:codeSystem>
							<urn3:displayName>FÃ¶rÃ¤ldraledighet fÃ¶r vÃ¥rd av barn</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="28">
					<instans>4</instans>
					<delsvar id="28.1">
						<urn3:cv>
							<urn3:code>STUDIER</urn3:code>
							<urn3:codeSystem>KV_FKMU_0002</urn3:codeSystem>
							<urn3:displayName>Studier</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="29">
					<delsvar id="29.1">Blomplockare</delsvar>
				</svar>
				<svar id="6">
					<delsvar id="6.2">
						<urn3:cv>
							<urn3:code>S47</urn3:code>
							<urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
							<urn3:displayName>KlÃ¤mskada pÃ¥ skuldra och Ã¶verarm</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="6.1">KlÃ¤mskada pÃ¥ skuldra och Ã¶verarm</delsvar>
					<delsvar id="6.4">
						<urn3:cv>
							<urn3:code>Y1113</urn3:code>
							<urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
							<urn3:displayName>FÃ¶rgiftning med och exponering fÃ¶r antiepileptika, lugnande lÃ¤kemedel och sÃ¶mnmedel, medel mot parkinsonism samt psykotropa lÃ¤kemedel som ej klassificeras annorstÃ¤des, med oklar avsikt-institutionellt boende-annan sysselsÃ¤ttning</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="6.3">FÃ¶rgiftning med och exponering fÃ¶r antiepileptika, lugnande lÃ¤kemedel och sÃ¶mnmedel, medel mot parkinsonism samt psykotropa lÃ¤kemedel som ej klassificeras annorstÃ¤des, med oklar avsikt-institutionellt boende-annan sysselsÃ¤ttning</delsvar>
					<delsvar id="6.6">
						<urn3:cv>
							<urn3:code>M659B</urn3:code>
							<urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>
							<urn3:displayName>Ospecifik synovit/tenosynovit i axelled/Ã¶verarm</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="6.5">Ospecifik synovit/tenosynovit i axelled/Ã¶verarm</delsvar>
				</svar>
				<svar id="35">
					<delsvar id="35.1">Kan inte plocka blommor.</delsvar>
				</svar>
				<svar id="17">
					<delsvar id="17.1">Se fÃ¶regÃ¥ende.</delsvar>
				</svar>
				<svar id="19">
					<delsvar id="19.1">SmÃ¶rjer med diverse krÃ¤mer.</delsvar>
				</svar>
				<svar id="20">
					<delsvar id="20.1">Mer krÃ¤mer.</delsvar>
				</svar>
				<svar id="32">
					<instans>1</instans>
					<delsvar id="32.1">
						<urn3:cv>
							<urn3:code>HELT_NEDSATT</urn3:code>
							<urn3:codeSystem>KV_FKMU_0003</urn3:codeSystem>
							<urn3:displayName>100%</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="32.2">
						<urn3:datePeriod>
							<urn3:start>2018-09-11</urn3:start>
							<urn3:end>2019-09-25</urn3:end>
						</urn3:datePeriod>
					</delsvar>
				</svar>
				<svar id="32">
					<instans>2</instans>
					<delsvar id="32.1">
						<urn3:cv>
							<urn3:code>TRE_FJARDEDEL</urn3:code>
							<urn3:codeSystem>KV_FKMU_0003</urn3:codeSystem>
							<urn3:displayName>75%</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="32.2">
						<urn3:datePeriod>
							<urn3:start>2018-07-06</urn3:start>
							<urn3:end>2018-08-10</urn3:end>
						</urn3:datePeriod>
					</delsvar>
				</svar>
				<svar id="32">
					<instans>3</instans>
					<delsvar id="32.1">
						<urn3:cv>
							<urn3:code>HALFTEN</urn3:code>
							<urn3:codeSystem>KV_FKMU_0003</urn3:codeSystem>
							<urn3:displayName>50%</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="32.2">
						<urn3:datePeriod>
							<urn3:start>2018-05-30</urn3:start>
							<urn3:end>2018-06-04</urn3:end>
						</urn3:datePeriod>
					</delsvar>
				</svar>
				<svar id="32">
					<instans>4</instans>
					<delsvar id="32.1">
						<urn3:cv>
							<urn3:code>EN_FJARDEDEL</urn3:code>
							<urn3:codeSystem>KV_FKMU_0003</urn3:codeSystem>
							<urn3:displayName>25%</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="32.2">
						<urn3:datePeriod>
							<urn3:start>2018-05-24</urn3:start>
							<urn3:end>2018-05-29</urn3:end>
						</urn3:datePeriod>
					</delsvar>
				</svar>
				<svar id="37">
					<delsvar id="37.1">Det krÃ¤vdes mer krÃ¤m.</delsvar>
				</svar>
				<svar id="33">
					<delsvar id="33.1">true</delsvar>
					<delsvar id="33.2">HejHejHej</delsvar>
				</svar>
				<svar id="34">
					<delsvar id="34.1">true</delsvar>
				</svar>
				<svar id="39">
					<delsvar id="39.1">
						<urn3:cv>
							<urn3:code>ATER_X_ANTAL_DGR</urn3:code>
							<urn3:codeSystem>KV_FKMU_0006</urn3:codeSystem>
							<urn3:displayName>Patienten kommer med stor sannolikhet att Ã¥tergÃ¥ helt i nuvarande sysselsÃ¤ttning efter x antal dagar</urn3:displayName>
						</urn3:cv>
					</delsvar>
					<delsvar id="39.3">
						<urn3:cv>
							<urn3:code>NITTIO_DGR</urn3:code>
							<urn3:codeSystem>KV_FKMU_0007</urn3:codeSystem>
							<urn3:displayName>90 dagar</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>1</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>ARBETSTRANING</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>ArbetstrÃ¤ning</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>2</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>ARBETSANPASSNING</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>Arbetsanpassning</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>3</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>SOKA_NYTT_ARBETE</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>SÃ¶ka nytt arbete</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>4</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>BESOK_ARBETSPLATS</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>BesÃ¶k pÃ¥ arbetsplatsen</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>5</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>ERGONOMISK</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>Ergonomisk bedÃ¶mning</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>6</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>HJALPMEDEL</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>HjÃ¤lpmedel</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>7</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>KONFLIKTHANTERING</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>Konflikthantering</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>8</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>KONTAKT_FHV</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>Kontakt med fÃ¶retagshÃ¤lsovÃ¥rd</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>9</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>OMFORDELNING</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>OmfÃ¶rdelning av arbetsuppgifter</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="40">
					<instans>10</instans>
					<delsvar id="40.1">
						<urn3:cv>
							<urn3:code>OVRIGA_ATGARDER</urn3:code>
							<urn3:codeSystem>KV_FKMU_0004</urn3:codeSystem>
							<urn3:displayName>Ã–vrigt</urn3:displayName>
						</urn3:cv>
					</delsvar>
				</svar>
				<svar id="44">
					<delsvar id="44.1">TrÃ¤na pÃ¥ att arbeta.</delsvar>
				</svar>
				<svar id="25">
					<delsvar id="25.1">Detta blir en kort text.</delsvar>
				</svar>
				<svar id="26">
					<delsvar id="26.1">true</delsvar>
					<delsvar id="26.2">Har vÃ¤ntat 12 Ã¥r pÃ¥ att fÃ¥ Ã¶ppna min lÃ¶fbergs lila</delsvar>
				</svar>
         </urn2:intyg>
         <!--You may enter ANY elements at this point-->
      </urn2:RegisterCertificate>`;
    }
};

module.exports.RegisterCertificate = function(patient, user, intyg) {
    return smiIntyg[intyg.typ](patient, user, intyg);
};
