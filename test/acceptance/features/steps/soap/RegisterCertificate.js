/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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



module.exports.RegisterCertificate = function(personId, personName1, personName2, doctorHsa, doctorName, unitHsa, unitName, intygsId) { //, intygsTyp) {

    //this.intyg = generateIntygByType(intyg.typ);
    // if (intygsTyp === 'Läkarutlåtande för sjukersättning') {
    return '<urn2:RegisterCertificate \n' +
        '               xmlns="urn:riv:clinicalprocess:healthcond:certificate:3" \n' +
        '                xmlns:urn2="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3" \n' +
        '                xmlns:urn3="urn:riv:clinicalprocess:healthcond:certificate:types:3" \n' +
        '                xmlns:urn4="urn:riv:itintegration:registry:1">\n' +
        '         <urn2:intyg>\n' +
        '            <intygs-id>\n' +
        '               <urn3:root>' + unitHsa + '</urn3:root>\n' +
        '               <urn3:extension>' + intygsId + '</urn3:extension>\n' +
        '            </intygs-id>\n' +
        '            <typ>\n' +
        '               <urn3:code>LUSE</urn3:code>\n' +
        '               <urn3:codeSystem>b64ea353-e8f6-4832-b563-fc7d46f29548</urn3:codeSystem>\n' +
        '               <urn3:displayName>Läkarutlåtande för sjukersättning</urn3:displayName>\n' +
        '            </typ>\n' +
        '            <version>1.0</version>\n' +
        '            <signeringstidpunkt>2016-12-09T12:53:59</signeringstidpunkt>\n' +
        '            <skickatTidpunkt>2016-12-09T12:53:59</skickatTidpunkt>\n' +
        '            <patient>\n' +
        '               <person-id>\n' +
        '                  <urn3:root>1.2.752.129.2.1.3.1</urn3:root>\n' +
        '                  <urn3:extension>' + personId + '</urn3:extension>\n' +
        '               </person-id>\n' +
        '               <fornamn>' + personName1 + ' </fornamn>\n' +
        '               <efternamn>' + personName2 + '</efternamn>\n' +
        '               <postadress>Hallloj 12</postadress>\n' +
        '               <postnummer>55664</postnummer>\n' +
        '               <postort>Tjo</postort>\n' +
        '            </patient>\n' +
        '            <skapadAv>\n' +
        '               <personal-id>\n' +
        '                  <urn3:root>1.2.752.129.2.1.4.1</urn3:root>\n' +
        '                  <urn3:extension>' + doctorHsa + '</urn3:extension>\n' +
        '               </personal-id>\n' +
        '               <fullstandigtNamn>' + doctorName + '</fullstandigtNamn>\n' +
        '               <forskrivarkod>0000000</forskrivarkod>\n' +
        '                 <befattning>\n' +
        '                  <urn3:code>205010</urn3:code>\n' +
        '                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>\n' +
        '               </befattning>\n' +
        '                   <befattning>\n' +
        '                  <urn3:code>205010</urn3:code>\n' +
        '                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>\n' +
        '               </befattning>\n' +
        '               <befattning>\n' +
        '                  <urn3:code>205010</urn3:code>\n' +
        '                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>\n' +
        '               </befattning>\n' +
        '                   <befattning>\n' +
        '                  <urn3:code>205010</urn3:code>\n' +
        '                  <urn3:codeSystem>1.2.752.129.2.2.1.4</urn3:codeSystem>\n' +
        '                  <!--Optional:-->\n' +
        '                  <urn3:displayName>Barnmorska, vårdavdelning</urn3:displayName>\n' +
        '               </befattning>\n' +
        '               <enhet>\n' +
        '                  <enhets-id>\n' +
        '                     <urn3:root>1.2.752.129.2.1.4.1</urn3:root>\n' +
        '                     <urn3:extension>' + unitHsa + '</urn3:extension>\n' +
        '                  </enhets-id>\n' +
        '                  <arbetsplatskod>\n' +
        '                     <urn3:root>1.2.752.29.4.71</urn3:root>\n' +
        '                     <urn3:extension>0000000</urn3:extension>\n' +
        '                  </arbetsplatskod>\n' +
        '                  <enhetsnamn>' + unitName + '</enhetsnamn>\n' +
        '                  <postadress>Bryggaregatan 11</postadress>\n' +
        '                  <postnummer>65340</postnummer>\n' +
        '                  <postort>Karlstad</postort>\n' +
        '                  <telefonnummer>054203040</telefonnummer>\n' +
        '                  <epost>intygnmt@nordicmedtest.se</epost>\n' +
        '                  <vardgivare>\n' +
        '                     <vardgivare-id>\n' +
        '                        <urn3:root>1.2.752.129.2.1.4.1</urn3:root>\n' +
        '                        <urn3:extension>TSTNMT2321000156-107M</urn3:extension>\n' +
        '                     </vardgivare-id>\n' +
        '                     <vardgivarnamn>nmt_vg1</vardgivarnamn>\n' +
        '                  </vardgivare>\n' +
        '               </enhet>\n' +
        '               <specialistkompetens>\n' +
        '                  <urn3:code>N/A</urn3:code>\n' +
        '                  <urn3:displayName>Medicinsk gastroenterologi och hepatologi</urn3:displayName>\n' +
        '               </specialistkompetens>\n' +
        '            </skapadAv>\n' +
        '            <svar id="1">\n' +
        '               <instans>1</instans>\n' +
        '               <delsvar id="1.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>UNDERSOKNING</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Min undersökning av patienten</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="1.2">2016-12-09</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="1">\n' +
        '               <instans>2</instans>\n' +
        '               <delsvar id="1.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>JOURNALUPPGIFTER</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Journaluppgifter från den</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="1.2">2016-12-09</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="1">\n' +
        '               <instans>3</instans>\n' +
        '               <delsvar id="1.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>ANHORIG</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Anhörigs beskrivning av patienten</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="1.2">2016-12-09</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="1">\n' +
        '               <instans>4</instans>\n' +
        '               <delsvar id="1.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>ANNAT</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0001</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Annat</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="1.2">2016-12-09</delsvar>\n' +
        '               <delsvar id="1.3">Journal from down below</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="2">\n' +
        '               <delsvar id="2.1">2016-12-09</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="3">\n' +
        '               <delsvar id="3.1">true</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="4">\n' +
        '               <instans>1</instans>\n' +
        '               <delsvar id="4.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>HABILITERING</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Underlag från habiliteringen</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="4.2">2016-12-09</delsvar>\n' +
        '               <delsvar id="4.3">Vårdcentralen Smultronet</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="4">\n' +
        '               <instans>2</instans>\n' +
        '               <delsvar id="4.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>ARBETSTERAPEUT</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Underlag från arbetsterapeut</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="4.2">2016-12-09</delsvar>\n' +
        '               <delsvar id="4.3">CSK</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="4">\n' +
        '               <instans>3</instans>\n' +
        '               <delsvar id="4.1">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>FYSIOTERAPEUT</urn3:code>\n' +
        '                     <urn3:codeSystem>KV_FKMU_0005</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Underlag från fysioterapeut</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="4.2">2016-12-09</delsvar>\n' +
        '               <delsvar id="4.3">CentralSjukhuset!!</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="5">\n' +
        '               <delsvar id="5.1">Patienten har haft besvären  Patienten känner sig trött. och har sedan dess pågått.</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="6">\n' +
        '               <delsvar id="6.2">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>S47</urn3:code>\n' +
        '                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Klämskada på skuldra och överarm</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="6.1">Klämskada på skuldra och överarm</delsvar>\n' +
        '               <delsvar id="6.4">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>F205</urn3:code>\n' +
        '                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Schizofrent resttillstånd</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="6.3">Schizofrent resttillstånd</delsvar>\n' +
        '               <delsvar id="6.6">\n' +
        '                  <urn3:cv>\n' +
        '                     <urn3:code>M659B</urn3:code>\n' +
        '                     <urn3:codeSystem>1.2.752.116.1.1.1.1.3</urn3:codeSystem>\n' +
        '                     <urn3:displayName>Ospecifik synovit/tenosynovit i axelled/överarm</urn3:displayName>\n' +
        '                  </urn3:cv>\n' +
        '               </delsvar>\n' +
        '               <delsvar id="6.5">Ospecifik synovit/tenosynovit i axelled/överarm</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="7">\n' +
        '               <delsvar id="7.1">I tidernas begynnelse ställdes diagnosen för patienten på Sahlgrenska</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="45">\n' +
        '               <delsvar id="45.1">true</delsvar>\n' +
        '               <delsvar id="45.2">alla och ingen.</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="8">\n' +
        '               <delsvar id="8.1">Pga smärtan påverkas intellektet</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="9">\n' +
        '               <delsvar id="9.1">Får svårt att kommunicera då humöret inte är på topp</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="10">\n' +
        '               <delsvar id="10.1">Har svårt att koncentrera sig längre än 2 minuter</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="11">\n' +
        '               <delsvar id="11.1">Påverkar inget annat enligt patienten</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="12">\n' +
        '               <delsvar id="12.1">Har fått sämre syn med åldern</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="13">\n' +
        '               <delsvar id="13.1">Det gör ont - Lena PH</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="14">\n' +
        '               <delsvar id="14.1">Ställer till det en hel del</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="17">\n' +
        '               <delsvar id="17.1">Patienten kan inte räcka upp armen. Stelhet i axelpartiet</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="18">\n' +
        '               <delsvar id="18.1">Rheabträning</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="19">\n' +
        '               <delsvar id="19.1">Smärtlindring och akupunktur</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="20">\n' +
        '               <delsvar id="20.1">För tidigt att säga något om planerade åtgärder</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="21">\n' +
        '               <delsvar id="21.1">Smärtlindring</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="22">\n' +
        '               <delsvar id="22.1">Rehabträning ska ge resultat om 1 år</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="23">\n' +
        '               <delsvar id="23.1">Laga mat går bra, men endast på fredagar</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="25">\n' +
        '               <delsvar id="25.1">Mycket material är hemligstämplat</delsvar>\n' +
        '            </svar>\n' +
        '            <svar id="26">\n' +
        '               <delsvar id="26.1">true</delsvar>\n' +
        '               <delsvar id="26.2">Jag har hemligt material på mitt skrivbord som kan vara intressant för er</delsvar>\n' +
        '            </svar>\n' +
        '         </urn2:intyg>\n' +
        '      </urn2:RegisterCertificate>\n';
    // }

};
