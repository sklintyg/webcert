angular.module('webcert').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('/views/dashboard/about.certificates.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\">\n" +
    "  <div class=\"row\" id=\"about-webcert-intyg\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <div wc-about>\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">\n" +
    "          Läkarintyg FK 7263 </h3>\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">\n" +
    "          Läkarintyget används av Försäkringskassan för att bedöma om patienten har rätt till sjukpenning. Av intyget\n" +
    "          ska det framgå hur sjukdomen påverkar patientens arbetsförmåga och hur länge patienten behöver vara\n" +
    "          sjukskriven.</p>\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">Transportstyrelsens läkarintyg</h3>\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">\n" +
    "          Transportstyrelsens läkarintyg ska användas vid förlängd giltighet av högre behörighet från 45 år, ansökan om\n" +
    "          körkortstillstånd för grupp II och III och vid ansökan om taxiförarlegitimation. Transportstyrelsens\n" +
    "          läkarintyg kan även användas när Transportstyrelsen i annat fall begärt ett allmänt läkarintyg avseende\n" +
    "          lämplighet att inneha körkort.</p>\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">Transportstyrelsens läkarintyg, diabetes</h3>\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">\n" +
    "          Transportstyrelsens läkarintyg, diabetes ska användas vid diabetessjukdom. Föreskrivna krav på läkarens\n" +
    "          specialistkompetens vid diabetessjukdom framgår av 17 kap. i Transportstyrelsens föreskrifter (TSFS 2010:125)\n" +
    "          och allmänna råd om medicinska krav för innehav av körkort m.m.</p>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.cookies.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\">\n" +
    "  <div class=\"row\" id=\"about-webcert-cookies\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <div wc-about>\n" +
    "        <span message key=\"about.cookies\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.faq.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\">\n" +
    "  <div class=\"row\" id=\"about-webcert-faq\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <div wc-about>\n" +
    "        <h3>Inloggning</h3>\n" +
    "        <h4>Varför kan jag inte logga in?</h4>\n" +
    "        <p>\n" +
    "          Kontrollera följande: </p>\n" +
    "        <ul>\n" +
    "          <li>Att SITHS-kortet (även kallat eTjänstekortet eller tjänstekortet) sitter ordentligt\n" +
    "            i kortläsaren.\n" +
    "          </li>\n" +
    "          <li>Att datorn är ansluten till internet.</li>\n" +
    "          <li>Att du skrivit rätt webbadress.</li>\n" +
    "          <li>Att NetiD är installerat på din PC. (Du kan se den som en ikon i menyfältet nere i högra hörnet).</li>\n" +
    "          <li>Testa att högerklicka på NetiD-ikonen nere till höger. Välj att uppdatera e-legitimationsförflyttning\n" +
    "            eller läs in kortet på nytt. Testa därefter att gå in i Webcert igen.\n" +
    "          </li>\n" +
    "          <li>Att du anger rätt kod när du försöker logga in. Observera att det finns två koder kopplade till ditt kort,\n" +
    "            en legitimeringskod och en underskriftskod. Vid inloggning används legitimeringskoden.\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "        <p>\n" +
    "          Om inget av ovanstående fungerar kontaktar du din SITHS-kortadministratör. </p>\n" +
    "\n" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "          <h3>Skriva intyg</h3>\n" +
    "\n" +
    "          <h4>Hur fungerar hjälpfunktionen?</h4>\n" +
    "          <p>Klicka på frågetecknet i anslutning till rubriken i utkastet så visas en kort hjälptext om hur fältet ska\n" +
    "            fyllas i. Om du behöver ytterligare information klickar du på länken. Hjälpfunktionen avser endast hjälp vid\n" +
    "            ifyllande av utkastet, inte teknisk hjälp.</p>\n" +
    "\n" +
    "          <h4>Hur kan jag få hjälp med bedömningar avfunktionsnedsättning och aktivitetsbegränsning?</h4>\n" +
    "          <p>I fält 4 och 5 i läkarintyg FK 7263, där du ska göra en bedömning av patientens funktionsnedsättning och\n" +
    "            aktivitetsbegränsning, finns en länk till <a\n" +
    "                href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\n" +
    "              försäkringsmedicinska beslutsstöd</a>. Via länken når\n" +
    "            du en hjälpmanual för hur bedömningarna bör ske.</p>\n" +
    "\n" +
    "          <h4>Hur använder jag mig av Socialstyrelsens försäkringsmedicinska beslutsstöd?</h4>\n" +
    "          <p>Genom att trycka på ikonen för <a\n" +
    "              href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\n" +
    "            försäkringsmedicinska beslutsstöd</a>, som finns vid fält 4 och\n" +
    "            5 i läkarintyg FK 7263, länkas du till Socialstyrelsens webbsida. Om diagnosen du har valt finns med i\n" +
    "            beslutsstödet kopplas du direkt till denna information. Om diagnosen inte finns med kan du välja att själv\n" +
    "            söka information om annan diagnos. <a\n" +
    "                href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\n" +
    "              försäkringsmedicinska beslutsstöd</a> är till hjälp för att\n" +
    "            bedöma hur lång tid sjukskrivningen ska vara. Om sjukskrivningen omfattar längre tid än den som\n" +
    "            rekommenderas ska det motiveras i intygets fält 9.</p>\n" +
    "\n" +
    "          <h4>Vad är skillnaden mellan ett intyg och ett utkast?</h4>\n" +
    "          <p>\n" +
    "            Ett intyg betraktas som ett utkast ända tills det signerats av en läkare, då övergår det till att bli ett\n" +
    "            intyg. </p>\n" +
    "          <p>\n" +
    "            Du kan skriva i och spara ett utkast så många gånger du vill innan det signeras. Ett utkast går även att\n" +
    "            radera och tas då bort från Webcert. </p>\n" +
    "          <p>\n" +
    "            I samband med att intyget signeras skickas det till Intygstjänsten och kan då nås av patienten via Mina\n" +
    "            intyg. </p>\n" +
    "          <p>\n" +
    "            Ett intyg kan inte sparas om eller raderas, däremot kan ett intyg makuleras och återtas eller ersättas av\n" +
    "            ett nytt intyg. </p>\n" +
    "\n" +
    "          <h4>Tangentbordsnavigering i Webcert</h4>\n" +
    "          <p>I Webcert stöds navigering med hjälp av tangentbordet. Webcert följer den standard som finns för webbläsare\n" +
    "            och du kan använda \"Tab\" för att hoppa mellan ifyllnadsrutor i ett intygsutkast. I de fall ett fält utgörs\n" +
    "            av val i form av radioknappar används tangentbordets piltangenter för att navigera mellan valen. För att\n" +
    "            markera ett val i en kryssruta eller radioknapp använd \"Mellansteg\"-tangenten.\n" +
    "          </p>\n" +
    "        </div>\n" +
    "\n" +
    "        <h3>Spara intyg</h3>\n" +
    "\n" +
    "        <h4>Varför går det inte att spara intyget?</h4>\n" +
    "        <p>Intyget sparas automatiskt under utfärdandet. Om spara-knappen inte är tillgänglig betyder det att intyget\n" +
    "          redan är sparat.</p>\n" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "          <h3>Skicka intyg</h3>\n" +
    "\n" +
    "          <h4>Varför syns inte skicka-knappen?</h4>\n" +
    "          <p>Du har inte signerat intyget. Observera att intyget låses vid signering, vilket innebär att det inte längre\n" +
    "            kan ändras eller utplånas.</p>\n" +
    "\n" +
    "          <h4>Varför behöver jag patientens samtycke för att skicka intyget?</h4>\n" +
    "          <p>Intyget innehåller patientens personuppgifter, såsom uppgifter om patientens hälsa. För att få skicka\n" +
    "            patientens intyg och därmed personuppgifter till en annan myndighet krävs enligt gällande lagstiftning\n" +
    "            (personuppgiftslagen och patientdatalagen) att du först inhämtar patientens samtycke.</p>\n" +
    "\n" +
    "          <h4>När ska jag skicka intyget till Försäkringskassan?</h4>\n" +
    "          <p>Intyg ska endast skickas till Försäkringskassan om samtliga tre punkter är uppfyllda:</p>\n" +
    "          <ul>\n" +
    "            <li>sjukskrivningen kommer att vara längre än 14 dagar.</li>\n" +
    "            <li>patienten kommer ansöka om sjukpenning.</li>\n" +
    "            <li>patienten kan eller vill inte skicka intyget från Mina intyg.</li>\n" +
    "          </ul>\n" +
    "          <p>Observera att patienten måste ge sitt samtycke för att du ska kunna skicka intyget elektroniskt direkt till\n" +
    "            Försäkringskassan.</p>\n" +
    "        </div>\n" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraFragor\">\n" +
    "          <h3>Fråga och svars-funktionen</h3>\n" +
    "\n" +
    "          <h4>Hur ställer jag en fråga till Försäkringskassan?</h4>\n" +
    "          <p>Funktionen för Fråga och svar blir tillgänglig först när intyget är signerat och skickat till\n" +
    "            Försäkringskassan. Gå in i intyget för att se alla frågor och svar för det aktuella läkarintyget. Du kan se\n" +
    "            vårdenhetens alla frågor och svar under fliken \"Frågor och svar\".</p>\n" +
    "\n" +
    "          <h4>Hur vet jag om det har kommit en fråga från Försäkringskassan?</h4>\n" +
    "          <p>Enhetens vårdadministratör får via mejl information om när ett nytt ärende har kommit in från\n" +
    "            Försäkringskassan. Vårdadministratören ansvarar för att rätt läkare får information om det nya ärendet.</p>\n" +
    "\n" +
    "          <h4>Jag har skickat en fråga till Försäkringskassan, hur ser jag om det kommit ett svar?</h4>\n" +
    "          <p>Du kan se det på något av följande sätt:</p>\n" +
    "          <ul>\n" +
    "            <li>Gå in på den berörda patientens intyg.</li>\n" +
    "            <li>Gå in på fliken \"Frågor och svar\".</li>\n" +
    "            <li>Enhetens vårdadministratör får via mejl information om när ett nytt ärende från Försäkringskassan har\n" +
    "              inkommit och ansvarar för att rätt läkare får information om det nya ärendet. Fråga din vårdadministratör\n" +
    "              om svaret har kommit.\n" +
    "            </li>\n" +
    "          </ul>\n" +
    "\n" +
    "          <h4>När försvinner frågor från listan under fliken \"Frågor och svar\"?</h4>\n" +
    "          <p>En fråga försvinner aldrig från listan över frågor och svar, däremot så visas den inte i listan när den är\n" +
    "            markerad som hanterad. Det går att söka efter och se en hanterad fråga med hjälp av sökfiltret.</p>\n" +
    "\n" +
    "          <h4>En fråga har besvarats via brev och ligger därmed kvar som obesvarad i Webcert. Hur tar jag bort\n" +
    "            frågan?</h4>\n" +
    "          <p>Öppna intyget som frågan hör till. Klicka på knappen \"markera som hanterad\".</p>\n" +
    "\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/dashboard/about.support.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\">\n" +
    "  <div class=\"row\" id=\"about-webcert-support\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <div wc-about>\n" +
    "        <p>\n" +
    "          I \"Vanliga frågor\" finns hjälp om du har frågor om Webcert. Om du saknar svar på din fråga kan du\n" +
    "          använda följande kontaktvägar, beroende på typ av fråga:</p>\n" +
    "        <ul>\n" +
    "          <li>Frågor som gäller inloggning: kontakta din SITHS-kortadministratör.</li>\n" +
    "          <li>Tekniska frågor om Webcert: hanteras i första hand av den lokala IT-avdelningen. Om din lokala\n" +
    "            IT-avdelning inte kan hitta felet ska de kontakta <a href=\"http://www.inera.se/felanmalan\" target=\"_blank\">Ineras\n" +
    "              Nationell kundservice</a>.\n" +
    "            Frågor om ifyllnaden av intyg: i intyget finns hjälpfunktioner för stöd hur intyget ska fyllas i. Du kan\n" +
    "            även gå till <a href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\"\n" +
    "                            target=\"_blank\">Socialstyrelsens försäkringsmedicinska beslutsstöd</a> för hjälp med bedömningar av\n" +
    "            funktionsnedsättning och aktivitetsbegränsning.\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.webcert.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\">\n" +
    "  <div class=\"row\" id=\"about-webcert-webcert\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <div wc-about>\n" +
    "        <p>\n" +
    "          Webcert är en tjänst som gör det möjligt att utfärda elektroniska läkarintyg och samla dem på ett ställe.\n" +
    "          Intyg som kan utfärdas i Webcert är:</p>\n" +
    "        <ul>\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">Läkarintyg FK 7263</li>\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">Transportstyrelsens läkarintyg</li>\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">Transportstyrelsens läkarintyg, diabetes</li>\n" +
    "        </ul>\n" +
    "        <p>\n" +
    "          I kommande versioner av tjänsten blir det möjligt att utfärda fler typer av läkarintyg. </p>\n" +
    "\n" +
    "        <h4>Användarkrav för Webcert</h4>\n" +
    "        <ul>\n" +
    "          <li>SITHS-kort (kallas även eTjänstekort eller tjänstekort)</li>\n" +
    "          <li>legitimeringskod kopplad till SITHS-kortet som används vid inloggning</li>\n" +
    "          <li>underskriftskod kopplad till SITHS-kortet som används för att signera intyg (endast läkare)</li>\n" +
    "          <li>kortläsare</li>\n" +
    "          <li>NetiD installerat i datorn</li>\n" +
    "          <li>användaren ska ha rätt behörighet.</li>\n" +
    "        </ul>\n" +
    "\n" +
    "        <h4>Om SITHS-kortet</h4>\n" +
    "        <p>\n" +
    "          SITHS-kortet är en personlig legitimation som inte får lånas ut. Både kort och koder ska förvaras på ett\n" +
    "          säkert sätt. </p>\n" +
    "        <p>Om du har problem med att logga in med ditt kort eller vill veta vilken behörighet du har så kan du testa det\n" +
    "          här: <a href=\"https://test.siths.se/\" target=\"_blank\">test.siths.se</a></p>\n" +
    "        <p>Om du förlorat ditt kort eller misstänker att någon obehörig fått kännedom om dina koder ska kortet spärras\n" +
    "          omedelbart. Om ditt kort har spärrats eller om du glömt någon av koderna ska du vända dig till din\n" +
    "          SITHS-kortadministratör.</p>\n" +
    "\n" +
    "        <h4>Användare</h4>\n" +
    "        <p>Webcert har två typer av användare: läkare och vårdadministratörer (exempelvis läkarsekreterare). Behörighet\n" +
    "          för inloggning:</p>\n" +
    "        <ul>\n" +
    "          <li>Vårdadministratör ska ha medarbetaruppdraget \"Vård och behandling\" i HSA.</li>\n" +
    "          <li>Läkare ska vara legitimerad läkare eller AT-läkare samt ha medarbetaruppdraget \"Vård och behandling\" i\n" +
    "            HSA.\n" +
    "          </li>\n" +
    "        </ul>\n" +
    "        <p>Alla användare kan skapa, skriva och spara utkast. Endast läkare kan signera och skriva ut intyg samt besvara\n" +
    "          frågor med ämnet \"komplettering\". Vårdadministratör kan via mejl notifiera läkare om att signera sparade\n" +
    "          intyg.</p>\n" +
    "\n" +
    "        <h4>Funktioner och invånarens hantering</h4>\n" +
    "        <p>Intyg som signeras i Webcert sparas även i webbtjänsten Mina intyg, där invånaren själv kan hantera sitt\n" +
    "          intyg. Mina intyg nås via 1177 Vårdguidens e-tjänster. I Mina intyg kan invånaren skicka intyget vidare\n" +
    "          elektroniskt till någon av de anslutna mottagarna, det går även att skriva ut intyget.</p>\n" +
    "        <p>\n" +
    "          Om patienten inte vill använda Mina intyg är det möjligt för läkaren att skicka vissa intygstyper elektroniskt\n" +
    "          direkt från Webcert till mottagaren. Observera att patientens samtycke då krävs. </p>\n" +
    "        <p>\n" +
    "          Om intyget inte skickas på något av dessa sätt kan läkaren skriva ut en kopia på det signerade intyget från\n" +
    "          Webcert och ge till patienten. </p>\n" +
    "        <p>\n" +
    "          För vissa intygstyper finns en elektronisk funktion för frågor och svar. Via funktionen kan läkaren och\n" +
    "          Försäkringskassan kommunicera om ett utfärdat intyg. </p>\n" +
    "        <h4>Webbläsare som stöds</h4>\n" +
    "        <p>\n" +
    "          För att kunna använda Webcerts funktioner behöver du någon av följande webbläsare: </p>\n" +
    "        <ul>\n" +
    "          <li>Internet Explorer 8 eller efterföljande versioner.</li>\n" +
    "        </ul>\n" +
    "        <p>Du måste även ha JavaScript aktiverat i din webbläsare för att kunna använda Webcert.</p>\n" +
    "        <h4>Adress till vårdenheten</h4>\n" +
    "        <p>\n" +
    "          Intyg som utfärdas i Webcert innehåller adress- och kontaktinformation till den eller de vårdenheter du\n" +
    "          arbetar för. För landstingsanställda läkare hämtas uppgifterna från HSA. Om du upptäcker att adress- och kontaktinformation är\n" +
    "          fel, så kan informationen ändras i HSA. </p>\n" +
    "        <h4>\n" +
    "          Ansvarig för Webcert: </h4>\n" +
    "        <p><a href=\"http://www.inera.se\" target=\"_blank\">Inera AB</a><br>\n" +
    "          Postadress: Box 17703<br>\n" +
    "          118 93 Stockholm<br>\n" +
    "          Organisationsnummer: 556559-4230</p>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/create.choose-cert-type.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "  <div class=\"row\">\n" +
    "    <div id=\"valj-intyg-typ\" class=\"col-xs-12 col-lg-7 webcert-col webcert-col-single\">\n" +
    "      <div class=\"row\">\n" +
    "        <div class=\"col-xs-12\">\n" +
    "          <form name=\"certForm\" ng-submit=\"createDraft()\" novalidate autocomplete=\"off\">\n" +
    "            <!--<a class=\"backlink\" href=\"#/create/choose-patient/index\"><span message key=\"common.goback\"></span></a>-->\n" +
    "            <h1>Sök/skriv intyg</h1>\n" +
    "            <div class=\"form-group\">\n" +
    "              <label class=\"control-label\">Patientuppgifter</label>\n" +
    "              <div class=\"form-group\">\n" +
    "                <span id=\"patientNamn\">{{fornamn}} {{mellannamn}} {{efternamn}}</span><br />\n" +
    "                {{personnummer}}\n" +
    "              </div>\n" +
    "              <button class=\"btn btn-default\" type=\"button\" ng-click=\"changePatient()\">Byt patient</button>\n" +
    "            </div>\n" +
    "\n" +
    "            <!-- New cert -->\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\n" +
    "              <div class=\"form-group\">\n" +
    "                <label for=\"intygType\" class=\"control-label\">Skriv nytt intyg</label>\n" +
    "                <select id=\"intygType\" class=\"form-control\" name=\"intygType\" data-ng-model=\"intygType\"\n" +
    "                        data-ng-options=\"type.id as type.label for type in certTypes\" wc-focus-me=\"focusFirstInput\"></select>\n" +
    "              </div>\n" +
    "              <div class=\"form-group\">\n" +
    "                <button id=\"intygTypeFortsatt\" class=\"btn btn-success\" type=\"submit\"\n" +
    "                        ng-disabled=\"intygType == 'default'\">Fortsätt\n" +
    "                </button>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- error message -->\n" +
    "              <div id=\"create-error\" ng-show=\"viewState.createErrorMessageKey\" class=\"alert alert-danger\">\n" +
    "                <span message key=\"{{viewState.createErrorMessageKey}}\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <div class=\"alert alert-warning col-lg-7\" ng-show=\"intygType != 'default' && intygType != undefined\">\n" +
    "                <span message key='{{\"certificatetypes.\"+ intygType +\".helptext\"}}'></span>\n" +
    "              </div>\n" +
    "            </div>\n" +
    "\n" +
    "          </form>\n" +
    "        </div>\n" +
    "      </div>\n" +
    "\n" +
    "      <!-- Tidigare intyg table -->\n" +
    "      <div class=\"webcert-col-section\">\n" +
    "        <h2 class=\"col-head\">Tidigare intyg för {{fornamn}} {{efternamn}}</h2>\n" +
    "        <div class=\"form-group\">\n" +
    "          <label class=\"radio-inline\">\n" +
    "            <input id=\"intygFilterAktuella\" name=\"intygFilter\" type=\"radio\" value=\"current\"\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Aktuella intyg\n" +
    "          </label>\n" +
    "          <label class=\"radio-inline\">\n" +
    "            <input id=\"intygFilterRattade\" name=\"intygFilter\" type=\"radio\" value=\"revoked\"\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Makulerade intyg\n" +
    "          </label>\n" +
    "          <label class=\"radio-inline\">\n" +
    "            <input id=\"intygFilterSamtliga\" name=\"intygFilter\" type=\"radio\" value=\"all\"\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Samtliga intyg\n" +
    "          </label>\n" +
    "        </div>\n" +
    "\n" +
    "        <div wc-spinner label=\"info.loadingdata\" show-spinner=\"viewState.doneLoading\"\n" +
    "             show-content=\"!viewState.doneLoading\">\n" +
    "          <div class=\"webcert-col-section qa-table-section\" id=\"intygLista\">\n" +
    "\n" +
    "            <!-- No certs for person -->\n" +
    "            <div id=\"current-list-noResults-unit\"\n" +
    "                 ng-show=\"!viewState.activeErrorMessageKey && viewState.currentList.length<1\"\n" +
    "                 class=\"alert alert-info\">\n" +
    "              <span message key=\"info.nocertsfound\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <!-- error message -->\n" +
    "            <div id=\"current-list-noResults-error\" ng-show=\"viewState.activeErrorMessageKey\"\n" +
    "                 class=\"alert alert-danger\">\n" +
    "              <span message key=\"{{viewState.activeErrorMessageKey}}\"></span>\n" +
    "            </div>\n" +
    "            <div ng-show=\"viewState.inlineErrorMessageKey\" class=\"alert alert-danger\">\n" +
    "              <span message key=\"{{viewState.inlineErrorMessageKey}}\"></span>\n" +
    "            </div>\n" +
    "\n" +
    "            <!-- Previous certs table -->\n" +
    "            <div class=\"row\" ng-show=\"viewState.currentList.length>0\" id=\"prevCertTable\">\n" +
    "              <table class=\"col-md-12 table table-striped table-qa\">\n" +
    "                <tr>\n" +
    "                  <th></th>\n" +
    "                  <th></th>\n" +
    "                  <th title=\"\">Typ av intyg</th>\n" +
    "                  <th title=\"\">Status</th>\n" +
    "                  <th title=\"\">Sparat datum</th>\n" +
    "                  <th title=\"\">Sparat/signerat av</th>\n" +
    "                </tr>\n" +
    "                <tr ng-repeat=\"cert in viewState.currentList\">\n" +
    "                  <td>\n" +
    "                    <button class=\"btn btn-info\" title=\"Visa intyget\" ng-click=\"openIntyg(cert)\"\n" +
    "                            id=\"showBtn-{{cert.intygId}}\">Visa\n" +
    "                    </button>\n" +
    "                  </td>\n" +
    "                  <td>\n" +
    "                    <button ng-show=\"cert.status != 'CANCELLED' && cert.status != 'DRAFT_COMPLETE' && cert.status != 'DRAFT_INCOMPLETE'\" class=\"btn btn-info\"\n" +
    "                            wc-feature-active feature=\"kopieraIntyg\" intygstyp=\"{{cert.intygType}}\"\n" +
    "                            title=\"Skapar en kopia av befintligt intyg. Det nya intyget (kopian) kan ändras och signeras.\" ng-click=\"copyIntyg(cert)\"\n" +
    "                            id=\"copyBtn-{{cert.intygId}}\">\n" +
    "                      Kopiera\n" +
    "                    </button>\n" +
    "                  </td>\n" +
    "                  <td><span message key=\"certificatetypes.{{cert.intygType}}.typename\"></span></td>\n" +
    "                  <td class=\"unbreakable\"><span message key=\"cert.status.{{cert.status}}\"></span></td>\n" +
    "                  <td class=\"unbreakable\">{{cert.lastUpdatedSigned | date:'shortDate'}}</td>\n" +
    "                  <td class=\"unbreakable table-qa-last\">{{cert.updatedSignedBy}}</td>\n" +
    "                </tr>\n" +
    "              </table>\n" +
    "\n" +
    "            </div>\n" +
    "            <span class=\"{{viewState.unsigned}}\"></span>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "        <!-- spinner end -->\n" +
    "\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/create.choose-patient.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "    <div class=\"row\">\n" +
    "      <div id=\"skapa-valj-patient\" class=\"col-xs-7 webcert-col webcert-col-single\">\n" +
    "\n" +
    "        <form name=\"pnrForm\" ng-submit=\"lookupPatient()\" novalidate autocomplete=\"off\">\n" +
    "          <h1>Sök/skriv intyg</h1>\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"pnr\" class=\"control-label\">Patientens personnummer</label>\n" +
    "            <input id=\"pnr\" class=\"form-control\" name=\"pnr\" type=\"text\" placeholder=\"Exempel: ååååmmdd-nnnn\" ng-model=\"personnummer\" required wc-person-number wc-visited maxlength=\"13\" wc-focus-me=\"focusPnr\" />\n" +
    "            <div ng-show=\"pnrForm.pnr.$invalid && pnrForm.pnr.$viewValue && (pnrForm.pnr.$visited || pnrForm.submitted)\">\n" +
    "              <span class=\"error\">Du måste ange ett giltigt personnummer</span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <div class=\"form-group\">\n" +
    "            <div class=\"form-group\">\n" +
    "              <button id=\"skapapersonnummerfortsatt\" class=\"btn btn-success\" type=\"submit\" ng-disabled=\"pnrForm.$invalid || widgetState.waiting\">\n" +
    "                <img ng-show=\"widgetState.waiting\" ng-src=\"/img/ajax-loader-small-green.gif\"/> Fortsätt\n" +
    "              </button>            </div>\n" +
    "            <div class=\"alert alert-danger\" id=\"puerror\" ng-if=\"widgetState.errorid\">\n" +
    "              <span message key=\"{{widgetState.errorid}}\"></span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "        </form>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/dashboard/create.edit-patient-name.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "  <div class=\"row\">\n" +
    "    <div id=\"sok-skriv-fyll-i-namn\" class=\"col-xs-12 col-lg-7 webcert-col webcert-col-single\">\n" +
    "      <form name=\"nameForm\" ng-submit=\"chooseCertType()\" novalidate autocomplete=\"off\">\n" +
    "        <!--a class=\"backlink\" href=\"#/create/choose-patient/index\"><span message key=\"common.goback\"></span></a-->\n" +
    "        <h1>Sök/skriv intyg</h1>\n" +
    "        <div class=\"form-group\">\n" +
    "          <label class=\"control-label\">Patientuppgifter</label>\n" +
    "          <div class=\"form-group\">\n" +
    "            {{personnummer}}\n" +
    "          </div>\n" +
    "          <button class=\"btn btn-default\" type=\"button\" ng-click=\"changePatient()\">Byt patient</button>\n" +
    "        </div>\n" +
    "\n" +
    "        <div class=\"webcert-col-section webcert-top-padding-section\">\n" +
    "\n" +
    "          <div class=\"alert alert-warning\" ng-show=\"!personNotFound && !errorOccured\">\n" +
    "            Observera att nedanstående uppgifter är inhämtade från personuppgiftstjänsten. Vill du ändra dem kan du göra det här, men dessa ändringar kommer endast gälla för detta intyg. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"alert alert-danger\" ng-show=\"personNotFound\">\n" +
    "            <strong>Inget resultat!</strong> Slagningen mot personuppgiftstjänsten returnerade inget resultat för {{personnummer}}. Manuell inmatning av namn är möjlig. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"alert alert-danger\" ng-show=\"errorOccured\">\n" +
    "            <strong>Tekniskt fel!</strong> Ett teknisk fel inträffade vid slagning mot personuppgiftstjänsten. Manuell inmatning av namn är möjlig. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\n" +
    "          </div>\n" +
    "\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"fornamn\" class=\"control-label\">Förnamn</label>\n" +
    "            <input id=\"fornamn\" class=\"form-control\" name=\"fornamn\" type=\"text\" ng-model=\"fornamn\" required\n" +
    "                   wc-visited />\n" +
    "            <div ng-show=\"nameForm.fornamn.$invalid && (nameForm.fornamn.$visited || nameForm.submitted)\">\n" +
    "              <span class=\"error\">Du måste ange ett förnamn</span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <div class=\"form-group\">\n" +
    "            <label for=\"efternamn\" class=\"control-label\">Efternamn</label>\n" +
    "            <input id=\"efternamn\" class=\"form-control\" name=\"efternamn\" type=\"text\" ng-model=\"efternamn\" required\n" +
    "                   wc-visited />\n" +
    "            <div ng-show=\"nameForm.efternamn.$invalid && (nameForm.efternamn.$visited || nameForm.submitted)\">\n" +
    "              <span class=\"error\">Du måste ange ett efternamn</span>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <div class=\"form-group\">\n" +
    "            <button id=\"namnFortsatt\" class=\"btn btn-success\" type=\"submit\" ng-disabled=\"nameForm.$invalid\">Fortsätt</button>\n" +
    "          </div>\n" +
    "        </div>\n" +
    "      </form>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/unhandled-qa.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraFragor\">\n" +
    "  <div class=\"row\" id=\"unhandled-qa\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\n" +
    "      <h1>\n" +
    "        <span message key=\"dashboard.unanswered.title\"></span>\n" +
    "      </h1>\n" +
    "\n" +
    "      <!-- load animation for entire page -->\n" +
    "      <div wc-spinner label=\"info.loadingdata\" show-spinner=\"!widgetState.doneLoading\"\n" +
    "           show-content=\"widgetState.doneLoading\">\n" +
    "        <div ng-show=\"units.length > 2\">\n" +
    "          <h2 class=\"less-top-padding\">\n" +
    "            Välj vårdenhet eller mottagning</h2>\n" +
    "\n" +
    "\n" +
    "          <div class=\"row\">\n" +
    "            <div id=\"wc-care-unit-clinic-selector\" wc-care-unit-clinic-selector></div>\n" +
    "          </div>\n" +
    "\n" +
    "        </div>\n" +
    "\n" +
    "        <div ng-if=\"isActiveUnitChosen()\">\n" +
    "          <h2 class=\"col-head\" ng-class=\"{'less-top-padding' : units.length < 2}\">{{selectedUnit.namn}}</h2>\n" +
    "\n" +
    "          <!-- search filter -->\n" +
    "          <div class=\"webcert-bottom-padding-section\">\n" +
    "            <span\n" +
    "                ng-if=\"!widgetState.filteredYet\">Nedan visas alla ej hanterade frågor och svar som kräver en åtgärd.<br></span>\n" +
    "            <button id=\"show-advanced-filter-btn\" class=\"btn btn-link btn-link-minimal\"\n" +
    "                    ng-click=\"widgetState.filterFormCollapsed = !widgetState.filterFormCollapsed\"\n" +
    "                    ng-switch=\"widgetState.filterFormCollapsed\">\n" +
    "              <span ng-switch-when=\"true\">Visa sökfilter</span><span ng-switch-when=\"false\">Dölj sökfilter</span>\n" +
    "            </button>\n" +
    "\n" +
    "            <!-- Filter form -->\n" +
    "            <div id=\"advanced-filter-form\" ng-show=\"!widgetState.filterFormCollapsed\"\n" +
    "                 class=\"qa-filter-panel form-horizontal\">\n" +
    "\n" +
    "              <!-- frågor och svar som är -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Frågor och svar som är</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"vidarebefordradAlla\" name=\"vidarebefordrad\" type=\"radio\" value=\"default\"\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\" checked=\"checked\"> Alla frågor och svar\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"vidarebefordradJa\" name=\"vidarebefordrad\" type=\"radio\" value=\"true\"\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\"> Vidarebefordrade\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"vidarebefordradNej\" name=\"vidarebefordrad\" type=\"radio\" value=\"false\"\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\"> Ej vidarebefordrade\n" +
    "                  </label>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- åtgärd -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Åtgärd</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <select id=\"qp-showStatus\" class=\"form-control\" ng-model=\"filterForm.vantarPaSelector\"\n" +
    "                          ng-options=\"s.label for s in statusList\"></select>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- Avsändare -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Avsändare</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"frageStallareAlla\" name=\"frageStallare\" type=\"radio\" value=\"default\"\n" +
    "                           ng-model=\"filterForm.questionFrom\" checked=\"checked\"> Alla avsändare\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"frageStallareFK\" name=\"frageStallare\" type=\"radio\" value=\"FK\"\n" +
    "                           ng-model=\"filterForm.questionFrom\"> Försäkringskassan\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"frageStallareWC\" name=\"frageStallare\" type=\"radio\" value=\"WC\"\n" +
    "                           ng-model=\"filterForm.questionFrom\"> Vårdenheten</label>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- signerat av -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Signerat av</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <select id=\"qp-lakareSelector\" class=\"form-control\" ng-model=\"filterForm.lakareSelector\"\n" +
    "                          ng-options=\"s.name for s in lakareList\" ng-disabled=\"widgetState.loadingLakares\"></select>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- Skickat/mottaget -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Skickat/mottaget</label>\n" +
    "                <div class=\"col-sm-10 form-inline\">\n" +
    "                  Från\n" +
    "                  <span dom-id=\"filter-changedate-from\" target-model=\"filterQuery.changedFrom\" wc-date-picker-field></span>\n" +
    "                  till\n" +
    "                  <span dom-id=\"filter-changedate-to\" target-model=\"filterQuery.changedTo\" wc-date-picker-field></span>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- buttons -->\n" +
    "              <div class=\"form-group\" style=\"margin-top:10px;\">\n" +
    "                <div class=\"col-sm-offset-2 col-sm-10\">\n" +
    "                  <button id=\"filter-qa-btn\" class=\"btn btn-default\" ng-click=\"filterList()\">Sök</button>\n" +
    "                  <button class=\"btn btn-default\" ng-click=\"resetFilterForm()\" id=\"reset-search-form\">\n" +
    "                    Återställ sökformuläret\n" +
    "                  </button>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <!-- loading animation table result -->\n" +
    "          <div wc-spinner label=\"info.running.query\" show-spinner=\"widgetState.runningQuery\"\n" +
    "               show-content=\"!widgetState.runningQuery\">\n" +
    "\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\n" +
    "\n" +
    "              <!-- No results message for unhandled -->\n" +
    "              <div id=\"current-list-noResults-unit\"\n" +
    "                   ng-show=\"widgetState.doneLoading && widgetState.currentList.length < 1 && !widgetState.activeErrorMessageKey && !widgetState.filteredYet\"\n" +
    "                   class=\"alert alert-info\">\n" +
    "                <span message key=\"info.nounanswered.qa.for.unit\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- No results message for query -->\n" +
    "              <div id=\"current-list-noResults-query\"\n" +
    "                   ng-show=\"widgetState.doneLoading && widgetState.totalCount < 1 && !widgetState.activeErrorMessageKey && widgetState.filteredYet\"\n" +
    "                   class=\"alert alert-info\">\n" +
    "                <span message key=\"info.query.noresults\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- error message -->\n" +
    "              <div id=\"current-list-noResults-error\" ng-show=\"widgetState.activeErrorMessageKey\"\n" +
    "                   class=\"alert alert-danger\">\n" +
    "                <span message key=\"{{widgetState.activeErrorMessageKey}}\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- Search hits -->\n" +
    "              <div id=\"query-count\" ng-show=\"widgetState.totalCount > 0\">\n" +
    "                Sökresultat - {{widgetState.totalCount}} träffar\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- qa table - Frågor och svar-tabell -->\n" +
    "              <div class=\"row\" ng-show=\"widgetState.currentList.length > 0\" id=\"qaTable\">\n" +
    "                <table class=\"col-md-12 table table-striped table-qa\">\n" +
    "                  <tr>\n" +
    "                    <th></th>\n" +
    "                    <th></th>\n" +
    "                    <th class=\"center\" title=\"Markera om fråga-svar är vidarebefordrat till den som ska hantera det.\">\n" +
    "                      Vidarebefordrad\n" +
    "                    </th>\n" +
    "                    <th title=\"Åtgärd som krävs för att fråga-svar ska anses som hanterad och avslutad.\">Åtgärd</th>\n" +
    "                    <th title=\"Vem som initierade frågan.\">Avsändare</th>\n" +
    "                    <th title=\"Berörd patients personnummer.\">Patient</th>\n" +
    "                    <th title=\"Läkare som har signerat intyget.\">Signerat av</th>\n" +
    "                    <th title=\"Datum för senaste händelse. Exempelvis när fråga skickades eller när ett svar inkom.\">\n" +
    "                      Skickat/mottaget\n" +
    "                    </th>\n" +
    "                  </tr>\n" +
    "                  <tr ng-repeat=\"qa in widgetState.currentList\">\n" +
    "                    <td>\n" +
    "                      <button class=\"btn btn-info\" title=\"Visar intyget och fråga-svar.\"\n" +
    "                              ng-click=\"openIntyg(qa.intygsReferens)\" id=\"showqaBtn-{{qa.internReferens}}\">Visa\n" +
    "                      </button>\n" +
    "                    </td>\n" +
    "                    <td>\n" +
    "                      <button class=\"btn btn-default\"\n" +
    "                              ng-class=\"{'btn-info': !qa.vidarebefordrad, 'btn-default btn-secondary' : qa.vidarebefordrad}\"\n" +
    "                              title=\"Skicka mejl med en länk till intyget för att informera den som ska hantera frågan-svaret.\"\n" +
    "                              ng-click=\"openMailDialog(qa)\">\n" +
    "                        <img ng-if=\"!qa.vidarebefordrad\" src=\"/img/mail.png\">\n" +
    "                        <img ng-if=\"qa.vidarebefordrad\" src=\"/img/mail_dark.png\">\n" +
    "                      </button>\n" +
    "                    </td>\n" +
    "                    <td class=\"center\">\n" +
    "                      <input id=\"selected\" type=\"checkbox\" ng-disabled=\"qa.updateInProgress\"\n" +
    "                             ng-model=\"qa.vidarebefordrad\" ng-change=\"onVidareBefordradChange(qa)\" />\n" +
    "                                            <span ng-if=\"qa.updateInProgress\"> <img\n" +
    "                                                src=\"/img/ajax-loader-kit-16x16.gif\"></span>\n" +
    "                    </td>\n" +
    "                    <td><span message key=\"qa.measure.{{qa.measureResKey}}\"></span></td>\n" +
    "                    <td><span message key=\"qa.fragestallare.{{qa.frageStallare}}\"></span></td>\n" +
    "                    <td id=\"patientId-{{qa.internReferens}}\" class=\"unbreakable\">{{qa.intygsReferens.patientId}}</td>\n" +
    "                    <td>{{qa.vardperson.namn}}</td>\n" +
    "                    <td class=\"unbreakable table-qa-last\">{{qa.senasteHandelseDatum |\n" +
    "                      date:'shortDate'}}\n" +
    "                    </td>\n" +
    "                    <!--td class=\"unbreakable table-qa-last\">{{qa.sistaDatumForSvar}}</td-->\n" +
    "                </table>\n" +
    "              </div>\n" +
    "              <div id=\"showing-nr-hits\" ng-show=\"widgetState.totalCount>0\">Visar\n" +
    "                träff 1 - {{widgetState.currentList.length}} av\n" +
    "                {{widgetState.totalCount}}\n" +
    "              </div>\n" +
    "              <div ng-show=\"widgetState.currentList.length < widgetState.totalCount\">\n" +
    "                <button id=\"hamtaFler\" class=\"btn btn-default\" title=\"Hämta fler träffar\" ng-click=\"fetchMore()\"\n" +
    "                        ng-disabled=\"widgetState.fetchingMoreInProgress\">\n" +
    "                  <img src=\"/img/loader-small.gif\" ng-show=\"widgetState.fetchingMoreInProgress\"> Hämta\n" +
    "                  fler träffar\n" +
    "                </button>\n" +
    "              </div>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <!-- spinner end -->\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/unsigned.html',
    "<div id=\"wcHeader\" wc-header></div>\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "  <div class=\"row\" id=\"unsigned\">\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\" ng-form=\"filterFormElement\">\n" +
    "      <h1>\n" +
    "        <span message key=\"dashboard.unsigned.title\"></span>\n" +
    "      </h1>\n" +
    "\n" +
    "      <div wc-spinner label=\"info.loadingdata\" show-spinner=\"!widgetState.doneLoading\"\n" +
    "           show-content=\"widgetState.doneLoading\">\n" +
    "\n" +
    "        <div>\n" +
    "          <h2 class=\"col-head less-top-padding\">{{widgetState.valdVardenhet.namn}}</h2>\n" +
    "\n" +
    "          <div class=\"webcert-bottom-padding-section\">\n" +
    "                      <span ng-if=\"!widgetState.filteredYet\">Nedan visas alla ej signerade utkast.<br></span>\n" +
    "            <button id=\"show-advanced-filter-btn\" class=\"btn btn-link btn-link-minimal\"\n" +
    "                    ng-click=\"widgetState.queryFormCollapsed = !widgetState.queryFormCollapsed\"\n" +
    "                    ng-switch=\"widgetState.queryFormCollapsed\">\n" +
    "              <span ng-switch-when=\"true\">Visa sökfilter</span><span ng-switch-when=\"false\">Dölj sökfilter</span>\n" +
    "            </button>\n" +
    "            <div id=\"advanced-filter-form\" ng-show=\"!widgetState.queryFormCollapsed\"\n" +
    "                 class=\"qa-filter-panel form-horizontal\">\n" +
    "\n" +
    "              <!-- utkast som är -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Utkast som är</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"forwardedAll\" name=\"forwarded\" type=\"radio\" ng-model=\"filterForm.forwarded\"\n" +
    "                           value=\"default\"> Alla utkast\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"forwarded\" name=\"forwarded\" type=\"radio\" value=\"true\" ng-model=\"filterForm.forwarded\">\n" +
    "                    Vidarebefordrade\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"forwardedNot\" name=\"forwarded\" type=\"radio\" value=\"false\"\n" +
    "                           ng-model=\"filterForm.forwarded\"> Ej vidarebefordrade\n" +
    "                  </label>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- Visa endast -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Visa endast</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"completeAll\" name=\"complete\" type=\"radio\" ng-model=\"filterForm.complete\" value=\"default\">\n" +
    "                    Båda\n" +
    "                  </label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"completeNo\" name=\"complete\" type=\"radio\" value=\"false\" ng-model=\"filterForm.complete\">\n" +
    "                    Utkast, uppgifter saknas</label>\n" +
    "                  <label class=\"radio-inline\">\n" +
    "                    <input id=\"completeYes\" name=\"complete\" type=\"radio\" value=\"true\" ng-model=\"filterForm.complete\">\n" +
    "                    Utkast, kan signeras</label>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- sparat datum -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Sparat datum</label>\n" +
    "                <div class=\"col-sm-10 form-inline\">\n" +
    "                  Från\n" +
    "                  <span dom-id=\"filter-changedate-from\" target-model=\"filterForm.lastFilterQuery.filter.savedFrom\"\n" +
    "                        wc-date-picker-field></span>\n" +
    "                  till\n" +
    "                  <span dom-id=\"filter-changedate-to\" target-model=\"filterForm.lastFilterQuery.filter.savedTo\"\n" +
    "                        wc-date-picker-field></span>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <div class=\"form-group\" ng-show=\"widgetState.invalidFromDate || widgetState.invalidToDate || widgetState.invalidToBeforeFromDate\">\n" +
    "                <div class=\"col-sm-2\"></div>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <div class=\" alert alert-danger\">\n" +
    "                    <ul>\n" +
    "                      <li ng-show=\"widgetState.invalidFromDate\"><span message key=\"validation.invalidfromdate\"></span></li>\n" +
    "                      <li ng-show=\"widgetState.invalidToDate\"><span message key=\"validation.invalidtodate\"></span></li>\n" +
    "                      <li ng-show=\"widgetState.invalidToBeforeFromDate\"><span message key=\"validation.invalidtobeforefromdate\"></span></li>\n" +
    "                    </ul>\n" +
    "                  </div>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- Sparat av -->\n" +
    "              <div class=\"form-group\">\n" +
    "                <label class=\"col-sm-2 control-label\">Sparat av</label>\n" +
    "                <div class=\"col-sm-10\">\n" +
    "                  <select id=\"uc-savedBy\" class=\"form-control\" ng-model=\"filterForm.lastFilterQuery.filter.savedBy\"\n" +
    "                          ng-options=\"s.hsaId as s.name for s in widgetState.savedByList\"\n" +
    "                          ng-disabled=\"widgetState.loadingSavedByList\"></select>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- buttons -->\n" +
    "              <div class=\"form-group\" style=\"margin-top:10px;\">\n" +
    "                <div class=\"col-sm-offset-2 col-sm-10\">\n" +
    "                  <button id=\"uc-filter-btn\" class=\"btn btn-default\" ng-click=\"filterDrafts()\">Sök</button>\n" +
    "                  <button class=\"btn btn-default\" ng-click=\"resetFilter()\">Återställ sökformuläret</button>\n" +
    "                </div>\n" +
    "              </div>\n" +
    "            </div>\n" +
    "          </div>\n" +
    "\n" +
    "          <div wc-spinner label=\"info.running.query\" show-spinner=\"widgetState.runningQuery\"\n" +
    "               show-content=\"!widgetState.runningQuery\">\n" +
    "\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\n" +
    "\n" +
    "              <!-- No results message for unhandled -->\n" +
    "              <div id=\"current-list-noResults-unit\"\n" +
    "                   ng-show=\"widgetState.doneLoading && !widgetState.activeErrorMessageKey && widgetState.currentList.length<1 && !widgetState.filteredYet\"\n" +
    "                   class=\"alert alert-info\">\n" +
    "                <span message key=\"info.nounsigned.certs.for.unit\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- No results message for query -->\n" +
    "              <div id=\"current-list-noResults-query\" ng-show=\"widgetState.doneLoading && widgetState.totalCount < 1  && widgetState.filteredYet\" class=\"alert alert-info\">\n" +
    "                <span message key=\"info.query.noresults\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- error message -->\n" +
    "              <div id=\"current-list-noResults-error\"\n" +
    "                   ng-show=\"widgetState.doneLoading && widgetState.activeErrorMessageKey\" class=\"alert alert-danger\">\n" +
    "                <span message key=\"{{widgetState.activeErrorMessageKey}}\"></span>\n" +
    "              </div>\n" +
    "\n" +
    "              <div id=\"query-count\" ng-show=\"widgetState.totalCount>0\">\n" +
    "                Sökresultat - {{widgetState.totalCount}} träffar\n" +
    "              </div>\n" +
    "\n" +
    "              <!-- unsigned cert table -->\n" +
    "              <div class=\"row\" ng-show=\"widgetState.currentList.length>0\" id=\"unsignedCertTable\">\n" +
    "                <table class=\"col-md-12 table table-striped table-qa\">\n" +
    "                  <tr>\n" +
    "                    <th></th>\n" +
    "                    <th></th>\n" +
    "                    <th class=\"center\"\n" +
    "                        title=\"Här markerar du om utkastet är vidarebefordrat till den som ska signera det.\">\n" +
    "                      Vidarebefordrad\n" +
    "                    </th>\n" +
    "                    <th title=\"Typ av intyg.\">Typ av intyg</th>\n" +
    "                    <th title=\"Visar utkastets status.\n" +
    "-\tUtkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas\n" +
    "-\tUtkast kan signeras = utkastet är sparat och kan signeras\n" +
    "-\tSignerat = intyget är signerat\n" +
    "-\tSkickat = intyget är signerat och skickat till mottagaren\n" +
    "-\tMottaget = intyget är signerat och mottaget av mottagarens system\n" +
    "-\tMakulerat = intyget är makulerat\">Status\n" +
    "                    </th>\n" +
    "                    <th title=\"Datum då utkastet senast sparades.\">Sparat datum</th>\n" +
    "                    <th title=\"Personnummer för patient som utkastet gäller.\">Patient</th>\n" +
    "                    <th title=\"Person som senast sparade utkastet.\">Sparat av</th>\n" +
    "                  </tr>\n" +
    "                  <tr ng-repeat=\"cert in widgetState.currentList\">\n" +
    "                    <td>\n" +
    "                      <button class=\"btn btn-info\" title=\"Visar intyget\" ng-click=\"openIntyg(cert)\"\n" +
    "                              id=\"showBtn-{{cert.intygId}}\"> Visa\n" +
    "                      </button>\n" +
    "                    </td>\n" +
    "                    <td>\n" +
    "                      <button class=\"btn btn-default\"\n" +
    "                              ng-class=\"{'btn-info': !cert.vidarebefordrad, 'btn-default btn-secondary' : cert.vidarebefordrad}\"\n" +
    "                              title=\"Skicka mejl med en länk till utkastet för att informera den läkare som ska signera det.\"\n" +
    "                              ng-click=\"openMailDialog(cert)\">\n" +
    "                        <img ng-if=\"!cert.vidarebefordrad\" src=\"/img/mail.png\">\n" +
    "                        <img ng-if=\"cert.vidarebefordrad\" src=\"/img/mail_dark.png\">\n" +
    "                      </button>\n" +
    "                    </td>\n" +
    "                    <td class=\"center\">\n" +
    "                      <input id=\"selected\" type=\"checkbox\" ng-disabled=\"cert.updateState.updateInProgress\" ng-model=\"cert.vidarebefordrad\"\n" +
    "                             ng-change=\"onForwardedChange(cert)\" />\n" +
    "                      <span ng-if=\"cert.updateState.updateInProgress\"> <img src=\"/img/ajax-loader-kit-16x16.gif\"></span>\n" +
    "                    </td>\n" +
    "                    <td class=\"unbreakable\"><span message\n" +
    "                                                  key=\"certificatetypes.{{cert.intygType}}.typename\"></span>\n" +
    "                    </td>\n" +
    "                    <td><span message key=\"cert.status.{{cert.status}}\"></span></td>\n" +
    "                    <td class=\"unbreakable\">{{cert.lastUpdatedSigned | date:'shortDate'}}</td>\n" +
    "                    <td class=\"unbreakable\">{{cert.patientId}}</td>\n" +
    "                    <td class=\"table-qa-last\">{{cert.updatedSignedBy}}</td>\n" +
    "                </table>\n" +
    "              </div>\n" +
    "              <div id=\"showing-nr-hits\" ng-show=\"widgetState.totalCount>0\">Visar\n" +
    "                träff {{widgetState.queryStartFrom+1}} - {{widgetState.currentList.length}} av\n" +
    "                {{widgetState.totalCount}}\n" +
    "              </div>\n" +
    "              <div\n" +
    "                  ng-show=\"((filterForm.lastFilterQuery.startFrom + filterForm.lastFilterQuery.pageSize) < widgetState.totalCount)\">\n" +
    "                <button class=\"btn btn-default\" title=\"Hämta fler träffar\" ng-click=\"fetchMore()\"\n" +
    "                        ng-disabled=\"widgetState.fetchingMoreInProgress\">\n" +
    "                  <img src=\"/img/loader-small.gif\" ng-show=\"widgetState.fetchingMoreInProgress\"> Hämta\n" +
    "                  fler träffar\n" +
    "                </button>\n" +
    "              </div>\n" +
    "\n" +
    "            </div>\n" +
    "          </div>\n" +
    "          <!-- spinner end -->\n" +
    "        </div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/view.certificate.html',
    "<div id=\"wcHeader\" class=\"print-hide\" wc-header default-active=\"index\"></div>\n" +
    "\n" +
    "<div class=\"row webcert-multicol\" id=\"viewCertAndQA\" wc-feature-active feature=\"hanteraIntygsutkast\">\n" +
    "  <div class=\"webcert-multicol-background\">\n" +
    "\n" +
    "    <!-- Left column -->\n" +
    "    <div class=\"col-sm-6 webcert-col webcert-col-primary\">\n" +
    "      <div class=\"print-hide\">\n" +
    "        <a id=\"tillbakaButton\" wc-feature-not-active feature=\"franJournalsystem\" class=\"backlink-icon\" href=\"/web/dashboard#/create/choose-cert-type/index\"\n" +
    "           title=\"Tillbaka till Sök/skriv intyg\"></a>\n" +
    "        <h1 class=\"backlink-heading\">\n" +
    "          <span message key=\"{{widgetState.certificateType}}.label.certtitle\"></span>\n" +
    "        </h1>\n" +
    "      </div>\n" +
    "      <div wc-insert-certificate certificate-type=\"{{widgetState.certificateType}}\"></div>\n" +
    "    </div>\n" +
    "    <!-- Certificate Left side end -->\n" +
    "\n" +
    "    <!-- Right side -->\n" +
    "    <div class=\"col-sm-6 webcert-col-secondary print-hide\"\n" +
    "         wc-feature-active feature=\"hanteraFragor\" intygstyp=\"{{widgetState.certificateType}}\">\n" +
    "      <div class=\"webcert-col-shadow\"></div>\n" +
    "      <h2 class=\"col-head\">Fråga &amp; Svar</h2>\n" +
    "      <div wc-insert-qa certificate-type=\"{{widgetState.certificateType}}\"></div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div><!-- Right side end -->\n"
  );


  $templateCache.put('/views/dashboard/view.qa.html',
    "<div id=\"wcHeader\" class=\"print-hide\" wc-header default-active=\"unhandled-qa\"></div>\n" +
    "\n" +
    "<div class=\"row webcert-multicol\" id=\"viewQAAndCert\">\n" +
    "  <div class=\"webcert-multicol-background\">\n" +
    "\n" +
    "    <!-- Left column -->\n" +
    "    <div class=\"col-sm-6 webcert-col webcert-col-primary print-hide\">\n" +
    "      <div wc-feature-active feature=\"hanteraFragor\" intygstyp=\"{{widgetState.certificateType}}\">\n" +
    "        <a id=\"tillbakaButton\" wc-feature-not-active feature=\"franJournalsystem\" class=\"backlink-icon\" href=\"/web/dashboard#/unhandled-qa\" title=\"Tillbaka till Fråga och Svar\"></a>\n" +
    "        <h1 class=\"backlink-heading\">Fråga och svar</h1>\n" +
    "        <div wc-insert-qa certificate-type=\"{{widgetState.certificateType}}\"></div>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "    <!-- Certificate Left side end -->\n" +
    "\n" +
    "    <!-- Right side -->\n" +
    "    <div class=\"col-sm-6 webcert-col-secondary\">\n" +
    "      <div class=\"print-hide\">\n" +
    "        <div class=\"webcert-col-shadow\"></div>\n" +
    "        <h2 class=\"col-head\">\n" +
    "          <span message key=\"{{widgetState.certificateType}}.label.certtitle\"></span>\n" +
    "        </h2>\n" +
    "      </div>\n" +
    "      <div wc-insert-certificate certificate-type=\"{{widgetState.certificateType}}\"></div>\n" +
    "    </div>\n" +
    "\n" +
    "  </div>\n" +
    "</div><!-- Right side end -->\n"
  );


  $templateCache.put('/views/partials/check-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "\t<h3 ng-focus=\"dialog.focus\" tabindex=\"-1\">\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\n" +
    "\t</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "\t<div ng-bind-html=\"bodyText\" style=\"padding-bottom: 10px\"><span ng-if=\"bodyTextId != undefined\" message key=\"{{bodyTextId}}\"></span></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\n" +
    "    <label class=\"checkbox\"><input id=\"dontShowAgain\" type=\"checkbox\" ng-model=\"model.dontShowCopyInfo\"> Visa inte denna information igen</label>\n" +
    "  </div>\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!dialog.acceptprogressdone\" ng-click=\"button1click()\">\n" +
    "      <img ng-hide=\"dialog.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\n" +
    "      <span message key=\"{{button3text}}\"></span>\n" +
    "    </button>\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"dialog.showerror\">\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\n" +
    "        <span message key=\"{{dialog.errormessageid}}\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/common-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "\t<h3 ng-focus=\"model.focus\" tabindex=\"-1\">\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\n" +
    "\t</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "\t<div ng-bind-html=\"bodyText\"><span ng-if=\"bodyTextId != undefined\" message key=\"{{bodyTextId}}\"></span></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "\t<button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\" ng-click=\"button1click()\">\n" +
    "\t\t<img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\n" +
    "\t\t<span message key=\"{{button1text}}\"></span>\n" +
    "\t</button>\n" +
    "\t<button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "\t\t<span message key=\"{{button2text}}\"></span>\n" +
    "\t</button>\n" +
    "\t<button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\n" +
    "\t\t<span message key=\"{{button3text}}\"></span>\n" +
    "\t</button>\n" +
    "\t<div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\n" +
    "    <div class=\"alert alert-danger alert-dialog-error\" ng-if=\"model.errormessage\">\n" +
    "      <span ng-bind-html=\"model.errormessage\"></span>\n" +
    "    </div>\n" +
    "\t\t<div class=\"alert alert-danger alert-dialog-error\" ng-if=\"model.errormessageid\">\n" +
    "\t\t\t<span message key=\"{{model.errormessageid}}\"></span>\n" +
    "    </div>\n" +
    "\t</div>\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/copy-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "\t<h3 ng-focus=\"dialog.focus\" tabindex=\"-1\">\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\n" +
    "\t</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <p>\n" +
    "    Kopiera intyg innebär att en kopia skapas av det befintliga intyget och med samma information.\n" +
    "    <span ng-show=\"model.otherCareUnit != undefined\" id=\"annanVardenhet\">\n" +
    "      Om intyget är utfärdat på en annan vårdenhet kommer adressuppgifterna att ersättas med de för den vårdenhet du är inloggad på.\n" +
    "    </span>\n" +
    "    <span ng-show=\"model.patientId != undefined && model.patientId.length > 0\" id=\"msgNyttPersonId\">\n" +
    "      <br>För denna patient finns nytt person-id och informationen kommer därför uppdateras i det nya intyget.\n" +
    "    </span>\n" +
    "    <span ng-show=\"!model.deepIntegration\" id=\"msgInteFranJournalSystem\">\n" +
    "      I de fall patienten har ändrat namn eller adress så uppdateras den informationen.\n" +
    "    </span>\n" +
    "  </p>\n" +
    "  <p>Uppgifterna i intygsutkastet går att ändra innan det signeras.</p>\n" +
    "  <span ng-show=\"model.intygTyp == 'fk7263'\" id=\"msgForlangningSjukskrivning\">Kopiera intyg kan användas exempelvis vid förlängning av en sjukskrivning.</span>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\n" +
    "    <label class=\"checkbox\"><input id=\"dontShowAgain\" type=\"checkbox\" ng-model=\"model.dontShowCopyInfo\"> Visa inte denna information igen</label>\n" +
    "  </div>\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\" ng-click=\"button1click()\">\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/error-dialog.html',
    "<div class=\"modal-header\">\n" +
    "    <h3>Tekniskt fel</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  {{bodyText}}\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "    <button class=\"btn btn-success\" ng-click=\"$close()\">OK</button>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/general-dialog.html',
    "<div class=\"modal-header\">\n" +
    "    <button class=\"close\" ng-click=\"$dismiss()\">×</button>\n" +
    "    <h3>\n" +
    "        <span message key=\"modal.title.{{title}}\"></span>\n" +
    "    </h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "    {{bodyText}}\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "    <button id=\"buttonYes\" class=\"btn btn-success\" ng-click=\"yes()\">\n" +
    "        <span message key=\"common.yes\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"buttonNo\" class=\"btn btn-info\" ng-click=\"no()\">\n" +
    "        <span message key=\"common.no\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"buttonNoDontAsk\" class=\"btn btn-info\" ng-click=\"noDontAsk()\">\n" +
    "        <span message key=\"common.nodontask\"></span>\n" +
    "    </button>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/makulera-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\n" +
    "    <span message key=\"{{titleId}}\"></span>\n" +
    "  </h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\"\n" +
    "            ng-click=\"button1click()\">\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\" />\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/preference-dialog.html',
    "<div class=\"modal-header\">\n" +
    "  <button class=\"close\" ng-click=\"$dismiss()\">×</button>\n" +
    "  <h3>\n" +
    "    <span message key=\"modal.title.{{title}}\"></span>\n" +
    "  </h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  {{bodyText}}\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <button id=\"buttonYes\" class=\"btn btn-success\" ng-click=\"yes()\">\n" +
    "    <span message key=\"common.yes\"></span>\n" +
    "  </button>\n" +
    "  <button id=\"buttonNo\" class=\"btn btn-info\" ng-click=\"no()\">\n" +
    "    <span message key=\"common.no\"></span>\n" +
    "  </button>\n" +
    "  <button id=\"buttonNoDontAsk\" class=\"btn btn-info\" ng-click=\"noDontAsk()\">\n" +
    "    <span message key=\"common.nodontask\"></span>\n" +
    "  </button>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/qa-check-hanterad-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\n" +
    "    <span message key=\"{{titleId}}\"></span>\n" +
    "  </h3>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"modal-body\">\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\n" +
    "  <div class=\"form-group form-inline\">\n" +
    "    <label class=\"checkbox\" ng-click=\"model.widgetState.setSkipShowUnhandledDialog(model.widgetState)\">\n" +
    "      <input type=\"checkbox\" id=\"preferenceSkipShowUnhandledDialog\" name=\"preferenceSkipShowUnhandledDialog\" ng-model=\"model.widgetState.skipShowUnhandledDialog\" ng-change=\"model.widgetState.setSkipShowUnhandledDialog(model.widgetState)\">\n" +
    "      <span message key=\"label.qacheckhanterad.checkbox\"></span></label>\n" +
    "  </div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-click=\"button1click()\">\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button3id}}\" class=\"btn btn-info\" ng-click=\"button3click()\">\n" +
    "      <span message key=\"{{button3text}}\"></span>\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/qa-only-warning-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\n" +
    "    <span message key=\"{{titleId}}\"></span>\n" +
    "  </h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-click=\"button1click()\">\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "  </div>\n" +
    "</div>\n"
  );


  $templateCache.put('/views/partials/send-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\n" +
    "\t<h3 ng-focus=\"model.focus\" tabindex=\"-1\">\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\n" +
    "\t</h3>\n" +
    "</div>\n" +
    "<div class=\"modal-body\">\n" +
    "\t<div style=\"padding-bottom: 10px\"><span id=\"{{bodyId}}\" message key=\"{{bodyTextId}}\"></span></div>\n" +
    "</div>\n" +
    "<div class=\"modal-footer\">\n" +
    "  <div class=\"form-group\">\n" +
    "    <label class=\"checkbox\"><input id=\"patientSamtycke\" type=\"checkbox\" ng-model=\"model.patientConsent\"> Patienten samtycker till att intyget skickas.</label>\n" +
    "  </div>\n" +
    "  <div class=\"form-inline\">\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.patientConsent || !model.acceptprogressdone\" ng-click=\"button1click()\">\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\n" +
    "      <span message key=\"{{button1text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\n" +
    "      <span message key=\"{{button2text}}\"></span>\n" +
    "    </button>\n" +
    "    <button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\n" +
    "      <span message key=\"{{button3text}}\"></span>\n" +
    "    </button>\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>"
  );

}]);
