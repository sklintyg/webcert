angular.module('webcert').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('/views/dashboard/about.certificates.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"about-webcert-intyg\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div wc-about>\r" +
    "\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">\r" +
    "\n" +
    "          Läkarintyg FK 7263 </h3>\r" +
    "\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">\r" +
    "\n" +
    "          Läkarintyget används av Försäkringskassan för att bedöma om patienten har rätt till sjukpenning. Av intyget\r" +
    "\n" +
    "          ska det framgå hur sjukdomen påverkar patientens arbetsförmåga och hur länge patienten behöver vara\r" +
    "\n" +
    "          sjukskriven.</p>\r" +
    "\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">Transportstyrelsens läkarintyg</h3>\r" +
    "\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">\r" +
    "\n" +
    "          Transportstyrelsens läkarintyg ska användas vid förlängd giltighet av högre behörighet från 45 år, ansökan om\r" +
    "\n" +
    "          körkortstillstånd för grupp II och III och vid ansökan om taxiförarlegitimation. Transportstyrelsens\r" +
    "\n" +
    "          läkarintyg kan även användas när Transportstyrelsen i annat fall begärt ett allmänt läkarintyg avseende\r" +
    "\n" +
    "          lämplighet att inneha körkort.</p>\r" +
    "\n" +
    "        <h3 wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">Transportstyrelsens läkarintyg, diabetes</h3>\r" +
    "\n" +
    "        <p wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">\r" +
    "\n" +
    "          Transportstyrelsens läkarintyg, diabetes ska användas vid diabetessjukdom. Föreskrivna krav på läkarens\r" +
    "\n" +
    "          specialistkompetens vid diabetessjukdom framgår av 17 kap. i Transportstyrelsens föreskrifter (TSFS 2010:125)\r" +
    "\n" +
    "          och allmänna råd om medicinska krav för innehav av körkort m.m.</p>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.cookies.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"about-webcert-cookies\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div wc-about>\r" +
    "\n" +
    "        <span message key=\"about.cookies\"></span>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.faq.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"about-webcert-faq\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div wc-about>\r" +
    "\n" +
    "        <h3>Inloggning</h3>\r" +
    "\n" +
    "        <h4>Varför kan jag inte logga in?</h4>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Kontrollera följande: </p>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li>Att SITHS-kortet (även kallat eTjänstekortet eller tjänstekortet) sitter ordentligt\r" +
    "\n" +
    "            i kortläsaren.\r" +
    "\n" +
    "          </li>\r" +
    "\n" +
    "          <li>Att datorn är ansluten till internet.</li>\r" +
    "\n" +
    "          <li>Att du skrivit rätt webbadress.</li>\r" +
    "\n" +
    "          <li>Att NetiD är installerat på din PC. (Du kan se den som en ikon i menyfältet nere i högra hörnet).</li>\r" +
    "\n" +
    "          <li>Testa att högerklicka på NetiD-ikonen nere till höger. Välj att uppdatera e-legitimationsförflyttning\r" +
    "\n" +
    "            eller läs in kortet på nytt. Testa därefter att gå in i Webcert igen.\r" +
    "\n" +
    "          </li>\r" +
    "\n" +
    "          <li>Att du anger rätt kod när du försöker logga in. Observera att det finns två koder kopplade till ditt kort,\r" +
    "\n" +
    "            en legitimeringskod och en underskriftskod. Vid inloggning används legitimeringskoden.\r" +
    "\n" +
    "          </li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Om inget av ovanstående fungerar kontaktar du din SITHS-kortadministratör. </p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "          <h3>Skriva intyg</h3>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Hur fungerar hjälpfunktionen?</h4>\r" +
    "\n" +
    "          <p>Klicka på frågetecknet i anslutning till rubriken i utkastet så visas en kort hjälptext om hur fältet ska\r" +
    "\n" +
    "            fyllas i. Om du behöver ytterligare information klickar du på länken. Hjälpfunktionen avser endast hjälp vid\r" +
    "\n" +
    "            ifyllande av utkastet, inte teknisk hjälp.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Hur kan jag få hjälp med bedömningar avfunktionsnedsättning och aktivitetsbegränsning?</h4>\r" +
    "\n" +
    "          <p>I fält 4 och 5 i läkarintyg FK 7263, där du ska göra en bedömning av patientens funktionsnedsättning och\r" +
    "\n" +
    "            aktivitetsbegränsning, finns en länk till <a\r" +
    "\n" +
    "                href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\r" +
    "\n" +
    "              försäkringsmedicinska beslutsstöd</a>. Via länken når\r" +
    "\n" +
    "            du en hjälpmanual för hur bedömningarna bör ske.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Hur använder jag mig av Socialstyrelsens försäkringsmedicinska beslutsstöd?</h4>\r" +
    "\n" +
    "          <p>Genom att trycka på ikonen för <a\r" +
    "\n" +
    "              href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\r" +
    "\n" +
    "            försäkringsmedicinska beslutsstöd</a>, som finns vid fält 4 och\r" +
    "\n" +
    "            5 i läkarintyg FK 7263, länkas du till Socialstyrelsens webbsida. Om diagnosen du har valt finns med i\r" +
    "\n" +
    "            beslutsstödet kopplas du direkt till denna information. Om diagnosen inte finns med kan du välja att själv\r" +
    "\n" +
    "            söka information om annan diagnos. <a\r" +
    "\n" +
    "                href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\" target=\"_blank\">Socialstyrelsens\r" +
    "\n" +
    "              försäkringsmedicinska beslutsstöd</a> är till hjälp för att\r" +
    "\n" +
    "            bedöma hur lång tid sjukskrivningen ska vara. Om sjukskrivningen omfattar längre tid än den som\r" +
    "\n" +
    "            rekommenderas ska det motiveras i intygets fält 9.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Vad är skillnaden mellan ett intyg och ett utkast?</h4>\r" +
    "\n" +
    "          <p>\r" +
    "\n" +
    "            Ett intyg betraktas som ett utkast ända tills det signerats av en läkare, då övergår det till att bli ett\r" +
    "\n" +
    "            intyg. </p>\r" +
    "\n" +
    "          <p>\r" +
    "\n" +
    "            Du kan skriva i och spara ett utkast så många gånger du vill innan det signeras. Ett utkast går även att\r" +
    "\n" +
    "            radera och tas då bort från Webcert. </p>\r" +
    "\n" +
    "          <p>\r" +
    "\n" +
    "            I samband med att intyget signeras skickas det till Intygstjänsten och kan då nås av patienten via Mina\r" +
    "\n" +
    "            intyg. </p>\r" +
    "\n" +
    "          <p>\r" +
    "\n" +
    "            Ett intyg kan inte sparas om eller raderas, däremot kan ett intyg makuleras och återtas eller ersättas av\r" +
    "\n" +
    "            ett nytt intyg. </p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Tangentbordsnavigering i Webcert</h4>\r" +
    "\n" +
    "          <p>I Webcert stöds navigering med hjälp av tangentbordet. Webcert följer den standard som finns för webbläsare\r" +
    "\n" +
    "            och du kan använda \"Tab\" för att hoppa mellan ifyllnadsrutor i ett intygsutkast. I de fall ett fält utgörs\r" +
    "\n" +
    "            av val i form av radioknappar används tangentbordets piltangenter för att navigera mellan valen. För att\r" +
    "\n" +
    "            markera ett val i en kryssruta eller radioknapp använd \"Mellansteg\"-tangenten.\r" +
    "\n" +
    "          </p>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h3>Spara intyg</h3>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h4>Varför går det inte att spara intyget?</h4>\r" +
    "\n" +
    "        <p>Intyget sparas automatiskt under utfärdandet. Om spara-knappen inte är tillgänglig betyder det att intyget\r" +
    "\n" +
    "          redan är sparat.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "          <h3>Skicka intyg</h3>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Varför syns inte skicka-knappen?</h4>\r" +
    "\n" +
    "          <p>Du har inte signerat intyget. Observera att intyget låses vid signering, vilket innebär att det inte längre\r" +
    "\n" +
    "            kan ändras eller utplånas.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Varför behöver jag patientens samtycke för att skicka intyget?</h4>\r" +
    "\n" +
    "          <p>Intyget innehåller patientens personuppgifter, såsom uppgifter om patientens hälsa. För att få skicka\r" +
    "\n" +
    "            patientens intyg och därmed personuppgifter till en annan myndighet krävs enligt gällande lagstiftning\r" +
    "\n" +
    "            (personuppgiftslagen och patientdatalagen) att du först inhämtar patientens samtycke.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>När ska jag skicka intyget till Försäkringskassan?</h4>\r" +
    "\n" +
    "          <p>Intyg ska endast skickas till Försäkringskassan om samtliga tre punkter är uppfyllda:</p>\r" +
    "\n" +
    "          <ul>\r" +
    "\n" +
    "            <li>sjukskrivningen kommer att vara längre än 14 dagar.</li>\r" +
    "\n" +
    "            <li>patienten kommer ansöka om sjukpenning.</li>\r" +
    "\n" +
    "            <li>patienten kan eller vill inte skicka intyget från Mina intyg.</li>\r" +
    "\n" +
    "          </ul>\r" +
    "\n" +
    "          <p>Observera att patienten måste ge sitt samtycke för att du ska kunna skicka intyget elektroniskt direkt till\r" +
    "\n" +
    "            Försäkringskassan.</p>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div wc-feature-active feature=\"hanteraFragor\">\r" +
    "\n" +
    "          <h3>Fråga och svars-funktionen</h3>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Hur ställer jag en fråga till Försäkringskassan?</h4>\r" +
    "\n" +
    "          <p>Funktionen för Fråga och svar blir tillgänglig först när intyget är signerat och skickat till\r" +
    "\n" +
    "            Försäkringskassan. Gå in i intyget för att se alla frågor och svar för det aktuella läkarintyget. Du kan se\r" +
    "\n" +
    "            vårdenhetens alla frågor och svar under fliken \"Frågor och svar\".</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Hur vet jag om det har kommit en fråga från Försäkringskassan?</h4>\r" +
    "\n" +
    "          <p>Enhetens vårdadministratör får via mejl information om när ett nytt ärende har kommit in från\r" +
    "\n" +
    "            Försäkringskassan. Vårdadministratören ansvarar för att rätt läkare får information om det nya ärendet.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>Jag har skickat en fråga till Försäkringskassan, hur ser jag om det kommit ett svar?</h4>\r" +
    "\n" +
    "          <p>Du kan se det på något av följande sätt:</p>\r" +
    "\n" +
    "          <ul>\r" +
    "\n" +
    "            <li>Gå in på den berörda patientens intyg.</li>\r" +
    "\n" +
    "            <li>Gå in på fliken \"Frågor och svar\".</li>\r" +
    "\n" +
    "            <li>Enhetens vårdadministratör får via mejl information om när ett nytt ärende från Försäkringskassan har\r" +
    "\n" +
    "              inkommit och ansvarar för att rätt läkare får information om det nya ärendet. Fråga din vårdadministratör\r" +
    "\n" +
    "              om svaret har kommit.\r" +
    "\n" +
    "            </li>\r" +
    "\n" +
    "          </ul>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>När försvinner frågor från listan under fliken \"Frågor och svar\"?</h4>\r" +
    "\n" +
    "          <p>En fråga försvinner aldrig från listan över frågor och svar, däremot så visas den inte i listan när den är\r" +
    "\n" +
    "            markerad som hanterad. Det går att söka efter och se en hanterad fråga med hjälp av sökfiltret.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <h4>En fråga har besvarats via brev och ligger därmed kvar som obesvarad i Webcert. Hur tar jag bort\r" +
    "\n" +
    "            frågan?</h4>\r" +
    "\n" +
    "          <p>Öppna intyget som frågan hör till. Klicka på knappen \"markera som hanterad\".</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/dashboard/about.support.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"about-webcert-support\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div wc-about>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          I \"Vanliga frågor\" finns hjälp om du har frågor om Webcert. Om du saknar svar på din fråga kan du\r" +
    "\n" +
    "          använda följande kontaktvägar, beroende på typ av fråga:</p>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li>Frågor som gäller inloggning: kontakta din SITHS-kortadministratör.</li>\r" +
    "\n" +
    "          <li>Tekniska frågor om Webcert: hanteras i första hand av den lokala IT-avdelningen. Om din lokala\r" +
    "\n" +
    "            IT-avdelning inte kan hitta felet ska de kontakta <a href=\"http://www.inera.se/felanmalan\" target=\"_blank\">Ineras\r" +
    "\n" +
    "              Nationell kundservice</a>.\r" +
    "\n" +
    "            Frågor om ifyllnaden av intyg: i intyget finns hjälpfunktioner för stöd hur intyget ska fyllas i. Du kan\r" +
    "\n" +
    "            även gå till <a href=\"http://www.socialstyrelsen.se/riktlinjer/forsakringsmedicinsktbeslutsstod\"\r" +
    "\n" +
    "                            target=\"_blank\">Socialstyrelsens försäkringsmedicinska beslutsstöd</a> för hjälp med bedömningar av\r" +
    "\n" +
    "            funktionsnedsättning och aktivitetsbegränsning.\r" +
    "\n" +
    "          </li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/about.webcert.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"about-webcert-webcert\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div wc-about>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Webcert är en tjänst som gör det möjligt att utfärda elektroniska läkarintyg och samla dem på ett ställe.\r" +
    "\n" +
    "          Intyg som kan utfärdas i Webcert är:</p>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"fk7263\">Läkarintyg FK 7263</li>\r" +
    "\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-bas\">Transportstyrelsens läkarintyg</li>\r" +
    "\n" +
    "          <li wc-feature-active feature=\"hanteraIntygsutkast\" intygstyp=\"ts-diabetes\">Transportstyrelsens läkarintyg, diabetes</li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          I kommande versioner av tjänsten blir det möjligt att utfärda fler typer av läkarintyg. </p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h4>Användarkrav för Webcert</h4>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li>SITHS-kort (kallas även eTjänstekort eller tjänstekort)</li>\r" +
    "\n" +
    "          <li>legitimeringskod kopplad till SITHS-kortet som används vid inloggning</li>\r" +
    "\n" +
    "          <li>underskriftskod kopplad till SITHS-kortet som används för att signera intyg (endast läkare)</li>\r" +
    "\n" +
    "          <li>kortläsare</li>\r" +
    "\n" +
    "          <li>NetiD installerat i datorn</li>\r" +
    "\n" +
    "          <li>användaren ska ha rätt behörighet.</li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h4>Om SITHS-kortet</h4>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          SITHS-kortet är en personlig legitimation som inte får lånas ut. Både kort och koder ska förvaras på ett\r" +
    "\n" +
    "          säkert sätt. </p>\r" +
    "\n" +
    "        <p>Om du har problem med att logga in med ditt kort eller vill veta vilken behörighet du har så kan du testa det\r" +
    "\n" +
    "          här: <a href=\"https://test.siths.se/\" target=\"_blank\">test.siths.se</a></p>\r" +
    "\n" +
    "        <p>Om du förlorat ditt kort eller misstänker att någon obehörig fått kännedom om dina koder ska kortet spärras\r" +
    "\n" +
    "          omedelbart. Om ditt kort har spärrats eller om du glömt någon av koderna ska du vända dig till din\r" +
    "\n" +
    "          SITHS-kortadministratör.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h4>Användare</h4>\r" +
    "\n" +
    "        <p>Webcert har två typer av användare: läkare och vårdadministratörer (exempelvis läkarsekreterare). Behörighet\r" +
    "\n" +
    "          för inloggning:</p>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li>Vårdadministratör ska ha medarbetaruppdraget \"Vård och behandling\" i HSA.</li>\r" +
    "\n" +
    "          <li>Läkare ska vara legitimerad läkare eller AT-läkare samt ha medarbetaruppdraget \"Vård och behandling\" i\r" +
    "\n" +
    "            HSA.\r" +
    "\n" +
    "          </li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "        <p>Alla användare kan skapa, skriva och spara utkast. Endast läkare kan signera och skriva ut intyg samt besvara\r" +
    "\n" +
    "          frågor med ämnet \"komplettering\". Vårdadministratör kan via mejl notifiera läkare om att signera sparade\r" +
    "\n" +
    "          intyg.</p>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <h4>Funktioner och invånarens hantering</h4>\r" +
    "\n" +
    "        <p>Intyg som signeras i Webcert sparas även i webbtjänsten Mina intyg, där invånaren själv kan hantera sitt\r" +
    "\n" +
    "          intyg. Mina intyg nås via 1177 Vårdguidens e-tjänster. I Mina intyg kan invånaren skicka intyget vidare\r" +
    "\n" +
    "          elektroniskt till någon av de anslutna mottagarna, det går även att skriva ut intyget.</p>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Om patienten inte vill använda Mina intyg är det möjligt för läkaren att skicka vissa intygstyper elektroniskt\r" +
    "\n" +
    "          direkt från Webcert till mottagaren. Observera att patientens samtycke då krävs. </p>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Om intyget inte skickas på något av dessa sätt kan läkaren skriva ut en kopia på det signerade intyget från\r" +
    "\n" +
    "          Webcert och ge till patienten. </p>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          För vissa intygstyper finns en elektronisk funktion för frågor och svar. Via funktionen kan läkaren och\r" +
    "\n" +
    "          Försäkringskassan kommunicera om ett utfärdat intyg. </p>\r" +
    "\n" +
    "        <h4>Webbläsare som stöds</h4>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          För att kunna använda Webcerts funktioner behöver du någon av följande webbläsare: </p>\r" +
    "\n" +
    "        <ul>\r" +
    "\n" +
    "          <li>Internet Explorer 8 eller efterföljande versioner.</li>\r" +
    "\n" +
    "        </ul>\r" +
    "\n" +
    "        <p>Du måste även ha JavaScript aktiverat i din webbläsare för att kunna använda Webcert.</p>\r" +
    "\n" +
    "        <h4>Adress till vårdenheten</h4>\r" +
    "\n" +
    "        <p>\r" +
    "\n" +
    "          Intyg som utfärdas i Webcert innehåller adress- och kontaktinformation till den eller de vårdenheter du\r" +
    "\n" +
    "          arbetar för. För landstingsanställda läkare hämtas uppgifterna från HSA. Om du upptäcker att adress- och kontaktinformation är\r" +
    "\n" +
    "          fel, så kan informationen ändras i HSA. </p>\r" +
    "\n" +
    "        <h4>\r" +
    "\n" +
    "          Ansvarig för Webcert: </h4>\r" +
    "\n" +
    "        <p><a href=\"http://www.inera.se\" target=\"_blank\">Inera AB</a><br>\r" +
    "\n" +
    "          Postadress: Box 17703<br>\r" +
    "\n" +
    "          118 93 Stockholm<br>\r" +
    "\n" +
    "          Organisationsnummer: 556559-4230</p>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/create.choose-cert-type.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "  <div class=\"row\">\r" +
    "\n" +
    "    <div id=\"valj-intyg-typ\" class=\"col-xs-12 col-lg-7 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <div class=\"row\">\r" +
    "\n" +
    "        <div class=\"col-xs-12\">\r" +
    "\n" +
    "          <form name=\"certForm\" ng-submit=\"createDraft()\" novalidate autocomplete=\"off\">\r" +
    "\n" +
    "            <!--<a class=\"backlink\" href=\"#/create/choose-patient/index\"><span message key=\"common.goback\"></span></a>-->\r" +
    "\n" +
    "            <h1>Sök/skriv intyg</h1>\r" +
    "\n" +
    "            <div class=\"form-group\">\r" +
    "\n" +
    "              <label class=\"control-label\">Patientuppgifter</label>\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <span id=\"patientNamn\">{{fornamn}} {{mellannamn}} {{efternamn}}</span><br />\r" +
    "\n" +
    "                {{personnummer}}\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <button class=\"btn btn-default\" type=\"button\" ng-click=\"changePatient()\">Byt patient</button>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <!-- New cert -->\r" +
    "\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label for=\"intygType\" class=\"control-label\">Skriv nytt intyg</label>\r" +
    "\n" +
    "                <select id=\"intygType\" class=\"form-control\" name=\"intygType\" data-ng-model=\"intygType\"\r" +
    "\n" +
    "                        data-ng-options=\"type.id as type.label for type in certTypes\" wc-focus-me=\"focusFirstInput\"></select>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <button id=\"intygTypeFortsatt\" class=\"btn btn-success\" type=\"submit\"\r" +
    "\n" +
    "                        ng-disabled=\"intygType == 'default'\">Fortsätt\r" +
    "\n" +
    "                </button>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- error message -->\r" +
    "\n" +
    "              <div id=\"create-error\" ng-show=\"widgetState.createErrorMessageKey\" class=\"alert alert-danger\">\r" +
    "\n" +
    "                <span message key=\"{{widgetState.createErrorMessageKey}}\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <div class=\"alert alert-warning col-lg-7\" ng-show=\"intygType != 'default' && intygType != undefined\">\r" +
    "\n" +
    "                <span message key='{{\"certificatetypes.\"+ intygType +\".helptext\"}}'></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          </form>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "      <!-- Tidigare intyg table -->\r" +
    "\n" +
    "      <div class=\"webcert-col-section\">\r" +
    "\n" +
    "        <h2 class=\"col-head\">Tidigare intyg för {{fornamn}} {{efternamn}}</h2>\r" +
    "\n" +
    "        <div class=\"form-group\">\r" +
    "\n" +
    "          <label class=\"radio-inline\">\r" +
    "\n" +
    "            <input id=\"intygFilterAktuella\" name=\"intygFilter\" type=\"radio\" value=\"current\"\r" +
    "\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Aktuella intyg\r" +
    "\n" +
    "          </label>\r" +
    "\n" +
    "          <label class=\"radio-inline\">\r" +
    "\n" +
    "            <input id=\"intygFilterRattade\" name=\"intygFilter\" type=\"radio\" value=\"revoked\"\r" +
    "\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Makulerade intyg\r" +
    "\n" +
    "          </label>\r" +
    "\n" +
    "          <label class=\"radio-inline\">\r" +
    "\n" +
    "            <input id=\"intygFilterSamtliga\" name=\"intygFilter\" type=\"radio\" value=\"all\"\r" +
    "\n" +
    "                   ng-model=\"filterForm.intygFilter\" checked=\"checked\"> Samtliga intyg\r" +
    "\n" +
    "          </label>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div wc-spinner label=\"info.loadingdata\" show-spinner=\"widgetState.doneLoading\"\r" +
    "\n" +
    "             show-content=\"!widgetState.doneLoading\">\r" +
    "\n" +
    "          <div class=\"webcert-col-section qa-table-section\" id=\"intygLista\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <!-- No certs for person -->\r" +
    "\n" +
    "            <div id=\"current-list-noResults-unit\"\r" +
    "\n" +
    "                 ng-show=\"!widgetState.activeErrorMessageKey && widgetState.currentList.length<1\"\r" +
    "\n" +
    "                 class=\"alert alert-info\">\r" +
    "\n" +
    "              <span message key=\"info.nocertsfound\"></span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <!-- error message -->\r" +
    "\n" +
    "            <div id=\"current-list-noResults-error\" ng-show=\"widgetState.activeErrorMessageKey\"\r" +
    "\n" +
    "                 class=\"alert alert-danger\">\r" +
    "\n" +
    "              <span message key=\"{{widgetState.activeErrorMessageKey}}\"></span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <!-- Previous certs table -->\r" +
    "\n" +
    "            <div class=\"row\" ng-show=\"widgetState.currentList.length>0\" id=\"prevCertTable\">\r" +
    "\n" +
    "              <table class=\"col-md-12 table table-striped table-qa\">\r" +
    "\n" +
    "                <tr>\r" +
    "\n" +
    "                  <th></th>\r" +
    "\n" +
    "                  <th></th>\r" +
    "\n" +
    "                  <th title=\"\">Typ av intyg</th>\r" +
    "\n" +
    "                  <th title=\"\">Status</th>\r" +
    "\n" +
    "                  <th title=\"\">Sparat datum</th>\r" +
    "\n" +
    "                  <th title=\"\">Sparat/signerat av</th>\r" +
    "\n" +
    "                </tr>\r" +
    "\n" +
    "                <tr ng-repeat=\"cert in widgetState.currentList\">\r" +
    "\n" +
    "                  <td>\r" +
    "\n" +
    "                    <button class=\"btn btn-info\" title=\"Visa intyget\" ng-click=\"openIntyg(cert)\"\r" +
    "\n" +
    "                            id=\"showBtn-{{cert.intygId}}\">Visa\r" +
    "\n" +
    "                    </button>\r" +
    "\n" +
    "                  </td>\r" +
    "\n" +
    "                  <td>\r" +
    "\n" +
    "                    <button ng-show=\"cert.status != 'CANCELLED' && cert.status != 'DRAFT_COMPLETE' && cert.status != 'DRAFT_INCOMPLETE'\" class=\"btn btn-info\"\r" +
    "\n" +
    "                            wc-feature-active feature=\"kopieraIntyg\" intygstyp=\"{{cert.intygType}}\"\r" +
    "\n" +
    "                            title=\"Skapar en kopia av befintligt intyg. Det nya intyget (kopian) kan ändras och signeras. Kopiera kan användas exempelvis vid förlängning av en sjukskrivning.\" ng-click=\"copyIntyg(cert)\"\r" +
    "\n" +
    "                            id=\"copyBtn-{{cert.intygId}}\">\r" +
    "\n" +
    "                      Kopiera\r" +
    "\n" +
    "                    </button>\r" +
    "\n" +
    "                  </td>\r" +
    "\n" +
    "                  <td><span message key=\"certificatetypes.{{cert.intygType}}.typename\"></span></td>\r" +
    "\n" +
    "                  <td class=\"unbreakable\"><span message key=\"cert.status.{{cert.status}}\"></span></td>\r" +
    "\n" +
    "                  <td class=\"unbreakable\">{{cert.lastUpdatedSigned | date:'shortDate'}}</td>\r" +
    "\n" +
    "                  <td class=\"unbreakable table-qa-last\">{{cert.updatedSignedBy}}</td>\r" +
    "\n" +
    "                </tr>\r" +
    "\n" +
    "              </table>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "        <!-- spinner end -->\r" +
    "\n" +
    "\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/create.choose-patient.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "    <div class=\"row\">\r" +
    "\n" +
    "      <div id=\"skapa-valj-patient\" class=\"col-xs-7 webcert-col webcert-col-single\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <form name=\"pnrForm\" ng-submit=\"lookupPatient()\" novalidate autocomplete=\"off\">\r" +
    "\n" +
    "          <h1>Sök/skriv intyg</h1>\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            <label for=\"pnr\" class=\"control-label\">Patientens personnummer</label>\r" +
    "\n" +
    "            <input id=\"pnr\" class=\"form-control\" name=\"pnr\" type=\"text\" placeholder=\"Exempel: ååååmmdd-nnnn\" ng-model=\"personnummer\" required wc-person-number wc-visited maxlength=\"13\" wc-focus-me=\"focusPnr\" />\r" +
    "\n" +
    "            <div ng-show=\"pnrForm.pnr.$invalid && pnrForm.pnr.$viewValue && (pnrForm.pnr.$visited || pnrForm.submitted)\">\r" +
    "\n" +
    "              <span class=\"error\">Du måste ange ett giltigt personnummer</span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            <div class=\"form-group\">\r" +
    "\n" +
    "              <button id=\"skapapersonnummerfortsatt\" class=\"btn btn-success\" type=\"submit\" ng-disabled=\"pnrForm.$invalid || widgetState.waiting\">\r" +
    "\n" +
    "                <img ng-show=\"widgetState.waiting\" ng-src=\"/img/ajax-loader-small-green.gif\"/> Fortsätt\r" +
    "\n" +
    "              </button>            </div>\r" +
    "\n" +
    "            <div class=\"alert alert-danger\" id=\"puerror\" ng-if=\"widgetState.errorid\">\r" +
    "\n" +
    "              <span message key=\"{{widgetState.errorid}}\"></span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "        </form>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/dashboard/create.edit-patient-name.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "  <div class=\"row\">\r" +
    "\n" +
    "    <div id=\"sok-skriv-fyll-i-namn\" class=\"col-xs-12 col-lg-7 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <form name=\"nameForm\" ng-submit=\"chooseCertType()\" novalidate autocomplete=\"off\">\r" +
    "\n" +
    "        <!--a class=\"backlink\" href=\"#/create/choose-patient/index\"><span message key=\"common.goback\"></span></a-->\r" +
    "\n" +
    "        <h1>Sök/skriv intyg</h1>\r" +
    "\n" +
    "        <div class=\"form-group\">\r" +
    "\n" +
    "          <label class=\"control-label\">Patientuppgifter</label>\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            {{personnummer}}\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <button class=\"btn btn-default\" type=\"button\" ng-click=\"changePatient()\">Byt patient</button>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div class=\"webcert-col-section webcert-top-padding-section\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"alert alert-warning\" ng-show=\"!personNotFound && !errorOccured\">\r" +
    "\n" +
    "            Observera att nedanstående uppgifter är inhämtade från personuppgiftstjänsten. Vill du ändra dem kan du göra det här, men dessa ändringar kommer endast gälla för detta intyg. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"alert alert-danger\" ng-show=\"personNotFound\">\r" +
    "\n" +
    "            <strong>Inget resultat!</strong> Slagningen mot personuppgiftstjänsten returnerade inget resultat för {{personnummer}}. Manuell inmatning av namn är möjlig. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"alert alert-danger\" ng-show=\"errorOccured\">\r" +
    "\n" +
    "            <strong>Tekniskt fel!</strong> Ett teknisk fel inträffade vid slagning mot personuppgiftstjänsten. Manuell inmatning av namn är möjlig. Tänk på att basera inmatade uppgifter på patientens legitimationsuppgifter.\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            <label for=\"fornamn\" class=\"control-label\">Förnamn</label>\r" +
    "\n" +
    "            <input id=\"fornamn\" class=\"form-control\" name=\"fornamn\" type=\"text\" ng-model=\"fornamn\" required\r" +
    "\n" +
    "                   wc-visited />\r" +
    "\n" +
    "            <div ng-show=\"nameForm.fornamn.$invalid && (nameForm.fornamn.$visited || nameForm.submitted)\">\r" +
    "\n" +
    "              <span class=\"error\">Du måste ange ett förnamn</span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            <label for=\"efternamn\" class=\"control-label\">Efternamn</label>\r" +
    "\n" +
    "            <input id=\"efternamn\" class=\"form-control\" name=\"efternamn\" type=\"text\" ng-model=\"efternamn\" required\r" +
    "\n" +
    "                   wc-visited />\r" +
    "\n" +
    "            <div ng-show=\"nameForm.efternamn.$invalid && (nameForm.efternamn.$visited || nameForm.submitted)\">\r" +
    "\n" +
    "              <span class=\"error\">Du måste ange ett efternamn</span>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <div class=\"form-group\">\r" +
    "\n" +
    "            <button id=\"namnFortsatt\" class=\"btn btn-success\" type=\"submit\" ng-disabled=\"nameForm.$invalid\">Fortsätt</button>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "      </form>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/unhandled-qa.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraFragor\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"unhandled-qa\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <h1>\r" +
    "\n" +
    "        <span message key=\"dashboard.unanswered.title\"></span>\r" +
    "\n" +
    "      </h1>\r" +
    "\n" +
    "\r" +
    "\n" +
    "      <div wc-spinner label=\"info.loadingdata\" show-spinner=\"!widgetState.doneLoading\"\r" +
    "\n" +
    "           show-content=\"widgetState.doneLoading\">\r" +
    "\n" +
    "        <div ng-show=\"units.length > 2\">\r" +
    "\n" +
    "          <h2 class=\"less-top-padding\">\r" +
    "\n" +
    "            Välj vårdenhet eller mottagning</h2>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"row\">\r" +
    "\n" +
    "            <div id=\"wc-care-unit-clinic-selector\" wc-care-unit-clinic-selector></div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div ng-if=\"isActiveUnitChosen()\">\r" +
    "\n" +
    "          <h2 class=\"col-head\" ng-class=\"{'less-top-padding' : units.length < 2}\">{{selectedUnit.namn}}</h2>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"webcert-bottom-padding-section\">\r" +
    "\n" +
    "            <span\r" +
    "\n" +
    "                ng-if=\"!widgetState.filteredYet\">Nedan visas alla ej hanterade frågor och svar som kräver en åtgärd.<br></span>\r" +
    "\n" +
    "            <button id=\"show-advanced-filter-btn\" class=\"btn btn-link btn-link-minimal\"\r" +
    "\n" +
    "                    ng-click=\"widgetState.filterFormCollapsed = !widgetState.filterFormCollapsed\"\r" +
    "\n" +
    "                    ng-switch=\"widgetState.filterFormCollapsed\">\r" +
    "\n" +
    "              <span ng-switch-when=\"true\">Visa sökfilter</span><span ng-switch-when=\"false\">Dölj sökfilter</span>\r" +
    "\n" +
    "            </button>\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <!-- Filter form -->\r" +
    "\n" +
    "            <div id=\"advanced-filter-form\" ng-show=\"!widgetState.filterFormCollapsed\"\r" +
    "\n" +
    "                 class=\"qa-filter-panel form-horizontal\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- frågor och svar som är -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Frågor och svar som är</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"vidarebefordradAlla\" name=\"vidarebefordrad\" type=\"radio\" value=\"default\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\" checked=\"checked\"> Alla frågor och svar\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"vidarebefordradJa\" name=\"vidarebefordrad\" type=\"radio\" value=\"true\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\"> Vidarebefordrade\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"vidarebefordradNej\" name=\"vidarebefordrad\" type=\"radio\" value=\"false\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.vidarebefordrad\"> Ej vidarebefordrade\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- åtgärd -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Åtgärd</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <select id=\"qp-showStatus\" class=\"form-control\" ng-model=\"filterForm.vantarPaSelector\"\r" +
    "\n" +
    "                          ng-options=\"s.label for s in statusList\"></select>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- Avsändare -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Avsändare</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"frageStallareAlla\" name=\"frageStallare\" type=\"radio\" value=\"default\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.questionFrom\" checked=\"checked\"> Alla avsändare\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"frageStallareFK\" name=\"frageStallare\" type=\"radio\" value=\"FK\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.questionFrom\"> Försäkringskassan\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"frageStallareWC\" name=\"frageStallare\" type=\"radio\" value=\"WC\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.questionFrom\"> Vårdenheten</label>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- signerat av -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Signerat av</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <select id=\"qp-lakareSelector\" class=\"form-control\" ng-model=\"filterForm.lakareSelector\"\r" +
    "\n" +
    "                          ng-options=\"s.name for s in lakareList\" ng-disabled=\"widgetState.loadingLakares\"></select>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- Skickat/mottaget -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Skickat/mottaget</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10 form-inline\">\r" +
    "\n" +
    "                  Från\r" +
    "\n" +
    "                  <span dom-id=\"filter-changedate-from\" target-model=\"filterQuery.changedFrom\" wc-date-picker-field></span>\r" +
    "\n" +
    "                  till\r" +
    "\n" +
    "                  <span dom-id=\"filter-changedate-to\" target-model=\"filterQuery.changedTo\" wc-date-picker-field></span>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- buttons -->\r" +
    "\n" +
    "              <div class=\"form-group\" style=\"margin-top:10px;\">\r" +
    "\n" +
    "                <div class=\"col-sm-offset-2 col-sm-10\">\r" +
    "\n" +
    "                  <button id=\"filter-qa-btn\" class=\"btn btn-default\" ng-click=\"filterList()\">Sök</button>\r" +
    "\n" +
    "                  <button class=\"btn btn-default\" ng-click=\"resetFilterForm()\" id=\"reset-search-form\">\r" +
    "\n" +
    "                    Återställ sökformuläret\r" +
    "\n" +
    "                  </button>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div wc-spinner label=\"info.running.query\" show-spinner=\"widgetState.runningQuery\"\r" +
    "\n" +
    "               show-content=\"!widgetState.runningQuery\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- No results message for unhandled -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-unit\"\r" +
    "\n" +
    "                   ng-show=\"widgetState.currentList.length < 1 && !widgetState.activeErrorMessageKey\"\r" +
    "\n" +
    "                   class=\"alert alert-info\">\r" +
    "\n" +
    "                <span message key=\"info.query.noresults\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- No results message for query -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-query\"\r" +
    "\n" +
    "                   ng-show=\"widgetState.totalCount < 1 && !widgetState.activeErrorMessageKey\" class=\"alert alert-info\">\r" +
    "\n" +
    "                <span message key=\"info.nounanswered.qa.for.unit\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- error message -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-error\" ng-show=\"widgetState.activeErrorMessageKey\"\r" +
    "\n" +
    "                   class=\"alert alert-danger\">\r" +
    "\n" +
    "                <span message key=\"{{widgetState.activeErrorMessageKey}}\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <div id=\"query-count\" ng-show=\"widgetState.totalCount > 0\">\r" +
    "\n" +
    "                Sökresultat - {{widgetState.totalCount}} träffar\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- qa table -->\r" +
    "\n" +
    "              <div class=\"row\" ng-show=\"widgetState.currentList.length > 0\" id=\"qaTable\">\r" +
    "\n" +
    "                <table class=\"col-md-12 table table-striped table-qa\">\r" +
    "\n" +
    "                  <tr>\r" +
    "\n" +
    "                    <th></th>\r" +
    "\n" +
    "                    <th></th>\r" +
    "\n" +
    "                    <th class=\"center\" title=\"Markera om fråga-svar är vidarebefordrat till den som ska hantera det.\">\r" +
    "\n" +
    "                      Vidarebefordrad\r" +
    "\n" +
    "                    </th>\r" +
    "\n" +
    "                    <th title=\"Åtgärd som krävs för att fråga-svar ska anses som hanterad och avslutad.\">Åtgärd</th>\r" +
    "\n" +
    "                    <th title=\"Vem som initierade frågan.\">Avsändare</th>\r" +
    "\n" +
    "                    <th title=\"Berörd patients personnummer.\">Patient</th>\r" +
    "\n" +
    "                    <th title=\"Läkare som har signerat intyget.\">Signerat av</th>\r" +
    "\n" +
    "                    <th title=\"Datum för senaste händelse. Exempelvis när fråga skickades eller när ett svar inkom.\">\r" +
    "\n" +
    "                      Skickat/mottaget\r" +
    "\n" +
    "                    </th>\r" +
    "\n" +
    "                  </tr>\r" +
    "\n" +
    "                  <tr ng-repeat=\"qa in widgetState.currentList\">\r" +
    "\n" +
    "                    <td>\r" +
    "\n" +
    "                      <button class=\"btn btn-info\" title=\"Visar intyget och fråga-svar.\"\r" +
    "\n" +
    "                              ng-click=\"openIntyg(qa.intygsReferens)\" id=\"showqaBtn-{{qa.internReferens}}\">Visa\r" +
    "\n" +
    "                      </button>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td>\r" +
    "\n" +
    "                      <button class=\"btn btn-default\"\r" +
    "\n" +
    "                              ng-class=\"{'btn-info': !qa.vidarebefordrad, 'btn-default btn-secondary' : qa.vidarebefordrad}\"\r" +
    "\n" +
    "                              title=\"Skicka mejl med en länk till intyget för att informera den som ska hantera frågan-svaret.\"\r" +
    "\n" +
    "                              ng-click=\"openMailDialog(qa)\">\r" +
    "\n" +
    "                        <img ng-if=\"!qa.vidarebefordrad\" src=\"/img/mail.png\">\r" +
    "\n" +
    "                        <img ng-if=\"qa.vidarebefordrad\" src=\"/img/mail_dark.png\">\r" +
    "\n" +
    "                      </button>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td class=\"center\">\r" +
    "\n" +
    "                      <input id=\"selected\" type=\"checkbox\" ng-disabled=\"qa.updateInProgress\"\r" +
    "\n" +
    "                             ng-model=\"qa.vidarebefordrad\" ng-change=\"onVidareBefordradChange(qa)\" />\r" +
    "\n" +
    "                                            <span ng-if=\"qa.updateInProgress\"> <img\r" +
    "\n" +
    "                                                src=\"/img/ajax-loader-kit-16x16.gif\"></span>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td><span message key=\"qa.measure.{{qa.measureResKey}}\"></span></td>\r" +
    "\n" +
    "                    <td><span message key=\"qa.fragestallare.{{qa.frageStallare}}\"></span></td>\r" +
    "\n" +
    "                    <td class=\"unbreakable\">{{qa.intygsReferens.patientId.patientIdExtension}}</td>\r" +
    "\n" +
    "                    <td>{{qa.vardperson.namn}}</td>\r" +
    "\n" +
    "                    <td class=\"unbreakable table-qa-last\">{{qa.senasteHandelseDatum |\r" +
    "\n" +
    "                      date:'shortDate'}}\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <!--td class=\"unbreakable table-qa-last\">{{qa.sistaDatumForSvar}}</td-->\r" +
    "\n" +
    "                </table>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <div id=\"showing-nr-hits\" ng-show=\"widgetState.totalCount>0\">Visar\r" +
    "\n" +
    "                träff 1 - {{widgetState.currentList.length}} av\r" +
    "\n" +
    "                {{widgetState.totalCount}}\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <div ng-show=\"widgetState.currentList.length < widgetState.totalCount\">\r" +
    "\n" +
    "                <button id=\"hamtaFler\" class=\"btn btn-default\" title=\"Hämta fler träffar\" ng-click=\"fetchMore()\"\r" +
    "\n" +
    "                        ng-disabled=\"widgetState.fetchingMoreInProgress\">\r" +
    "\n" +
    "                  <img src=\"/img/loader-small.gif\" ng-show=\"widgetState.fetchingMoreInProgress\"> Hämta\r" +
    "\n" +
    "                  fler träffar\r" +
    "\n" +
    "                </button>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <!-- spinner end -->\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/unsigned.html',
    "<div id=\"wcHeader\" wc-header></div>\r" +
    "\n" +
    "<div class=\"container-fluid\" wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "  <div class=\"row\" id=\"unsigned\">\r" +
    "\n" +
    "    <div class=\"col-md-12 webcert-col webcert-col-single\">\r" +
    "\n" +
    "      <h1>\r" +
    "\n" +
    "        <span message key=\"dashboard.unsigned.title\"></span>\r" +
    "\n" +
    "      </h1>\r" +
    "\n" +
    "\r" +
    "\n" +
    "      <div wc-spinner label=\"info.loadingdata\" show-spinner=\"!widgetState.doneLoading\"\r" +
    "\n" +
    "           show-content=\"widgetState.doneLoading\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "        <div>\r" +
    "\n" +
    "          <h2 class=\"col-head less-top-padding\">{{widgetState.valdVardenhet.namn}}</h2>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div class=\"webcert-bottom-padding-section\">\r" +
    "\n" +
    "                      <span ng-if=\"!widgetState.filteredYet\">Nedan visas alla ej signerade utkast.<br></span>\r" +
    "\n" +
    "            <button id=\"show-advanced-filter-btn\" class=\"btn btn-link btn-link-minimal\"\r" +
    "\n" +
    "                    ng-click=\"widgetState.queryFormCollapsed = !widgetState.queryFormCollapsed\"\r" +
    "\n" +
    "                    ng-switch=\"widgetState.queryFormCollapsed\">\r" +
    "\n" +
    "              <span ng-switch-when=\"true\">Visa sökfilter</span><span ng-switch-when=\"false\">Dölj sökfilter</span>\r" +
    "\n" +
    "            </button>\r" +
    "\n" +
    "            <div id=\"advanced-filter-form\" ng-show=\"!widgetState.queryFormCollapsed\"\r" +
    "\n" +
    "                 class=\"qa-filter-panel form-horizontal\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- utkast som är -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Utkast som är</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"forwardedAll\" name=\"forwarded\" type=\"radio\" ng-model=\"filterForm.forwarded\"\r" +
    "\n" +
    "                           value=\"default\"> Alla utkast\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"forwarded\" name=\"forwarded\" type=\"radio\" value=\"true\" ng-model=\"filterForm.forwarded\">\r" +
    "\n" +
    "                    Vidarebefordrade\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"forwardedNot\" name=\"forwarded\" type=\"radio\" value=\"false\"\r" +
    "\n" +
    "                           ng-model=\"filterForm.forwarded\"> Ej vidarebefordrade\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- Visa endast -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Visa endast</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"completeAll\" name=\"complete\" type=\"radio\" ng-model=\"filterForm.complete\" value=\"default\">\r" +
    "\n" +
    "                    Båda\r" +
    "\n" +
    "                  </label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"completeNo\" name=\"complete\" type=\"radio\" value=\"false\" ng-model=\"filterForm.complete\">\r" +
    "\n" +
    "                    Utkast, uppgifter saknas</label>\r" +
    "\n" +
    "                  <label class=\"radio-inline\">\r" +
    "\n" +
    "                    <input id=\"completeYes\" name=\"complete\" type=\"radio\" value=\"true\" ng-model=\"filterForm.complete\">\r" +
    "\n" +
    "                    Utkast, kan signeras</label>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- sparat datum -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Sparat datum</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10 form-inline\">\r" +
    "\n" +
    "                  Från\r" +
    "\n" +
    "                  <span dom-id=\"filter-changedate-from\" target-model=\"filterForm.lastFilterQuery.filter.savedFrom\"\r" +
    "\n" +
    "                        wc-date-picker-field></span>\r" +
    "\n" +
    "                  till\r" +
    "\n" +
    "                  <span dom-id=\"filter-changedate-to\" target-model=\"filterForm.lastFilterQuery.filter.savedTo\"\r" +
    "\n" +
    "                        wc-date-picker-field></span>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- Sparat av -->\r" +
    "\n" +
    "              <div class=\"form-group\">\r" +
    "\n" +
    "                <label class=\"col-sm-2 control-label\">Sparat av</label>\r" +
    "\n" +
    "                <div class=\"col-sm-10\">\r" +
    "\n" +
    "                  <select id=\"uc-savedBy\" class=\"form-control\" ng-model=\"filterForm.lastFilterQuery.filter.savedBy\"\r" +
    "\n" +
    "                          ng-options=\"s.hsaId as s.name for s in widgetState.savedByList\"\r" +
    "\n" +
    "                          ng-disabled=\"widgetState.loadingSavedByList\"></select>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- buttons -->\r" +
    "\n" +
    "              <div class=\"form-group\" style=\"margin-top:10px;\">\r" +
    "\n" +
    "                <div class=\"col-sm-offset-2 col-sm-10\">\r" +
    "\n" +
    "                  <button id=\"uc-filter-btn\" class=\"btn btn-default\" ng-click=\"filterDrafts()\">Sök</button>\r" +
    "\n" +
    "                  <button class=\"btn btn-default\" ng-click=\"resetFilter()\">Återställ sökformuläret</button>\r" +
    "\n" +
    "                </div>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "          <div wc-spinner label=\"info.running.query\" show-spinner=\"widgetState.runningQuery\"\r" +
    "\n" +
    "               show-content=\"!widgetState.runningQuery\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "            <div class=\"webcert-col-section qa-table-section\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- No results message for unhandled -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-unit\"\r" +
    "\n" +
    "                   ng-show=\"widgetState.doneLoading && !widgetState.activeErrorMessageKey && widgetState.currentList.length<1\"\r" +
    "\n" +
    "                   class=\"alert alert-info\">\r" +
    "\n" +
    "                <span message key=\"info.nounsigned.certs.for.unit\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- No results message for query -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-query\" ng-show=\"widgetState.totalCount < 1\" class=\"alert alert-info\">\r" +
    "\n" +
    "                <span message key=\"info.query.noresults\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- error message -->\r" +
    "\n" +
    "              <div id=\"current-list-noResults-error\"\r" +
    "\n" +
    "                   ng-show=\"widgetState.doneLoading && widgetState.activeErrorMessageKey\" class=\"alert alert-danger\">\r" +
    "\n" +
    "                <span message key=\"{{widgetState.activeErrorMessageKey}}\"></span>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <div id=\"query-count\" ng-show=\"widgetState.totalCount>0\">\r" +
    "\n" +
    "                Sökresultat - {{widgetState.totalCount}} träffar\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "              <!-- unsigned cert table -->\r" +
    "\n" +
    "              <div class=\"row\" ng-show=\"widgetState.currentList.length>0\" id=\"unsignedCertTable\">\r" +
    "\n" +
    "                <table class=\"col-md-12 table table-striped table-qa\">\r" +
    "\n" +
    "                  <tr>\r" +
    "\n" +
    "                    <th></th>\r" +
    "\n" +
    "                    <th></th>\r" +
    "\n" +
    "                    <th class=\"center\"\r" +
    "\n" +
    "                        title=\"Här markerar du om utkastet är vidarebefordrat till den som ska signera det.\">\r" +
    "\n" +
    "                      Vidarebefordrad\r" +
    "\n" +
    "                    </th>\r" +
    "\n" +
    "                    <th title=\"Typ av intyg.\">Typ av intyg</th>\r" +
    "\n" +
    "                    <th title=\"Visar utkastets status.\r" +
    "\n" +
    "-\tUtkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas\r" +
    "\n" +
    "-\tUtkast kan signeras = utkastet är sparat och kan signeras\r" +
    "\n" +
    "-\tSignerat = intyget är signerat\r" +
    "\n" +
    "-\tSkickat = intyget är signerat och skickat till mottagaren\r" +
    "\n" +
    "-\tMottaget = intyget är signerat och mottaget av mottagarens system\r" +
    "\n" +
    "-\tMakulerat = intyget är makulerat\">Status\r" +
    "\n" +
    "                    </th>\r" +
    "\n" +
    "                    <th title=\"Datum då utkastet senast sparades.\">Sparat datum</th>\r" +
    "\n" +
    "                    <th title=\"Personnummer för patient som utkastet gäller.\">Patient</th>\r" +
    "\n" +
    "                    <th title=\"Person som senast sparade utkastet.\">Sparat av</th>\r" +
    "\n" +
    "                  </tr>\r" +
    "\n" +
    "                  <tr ng-repeat=\"cert in widgetState.currentList\">\r" +
    "\n" +
    "                    <td>\r" +
    "\n" +
    "                      <button class=\"btn btn-info\" title=\"Visar intyget\" ng-click=\"openIntyg(cert)\"\r" +
    "\n" +
    "                              id=\"showBtn-{{cert.intygId}}\"> Visa\r" +
    "\n" +
    "                      </button>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td>\r" +
    "\n" +
    "                      <button class=\"btn btn-default\"\r" +
    "\n" +
    "                              ng-class=\"{'btn-info': !cert.vidarebefordrad, 'btn-default btn-secondary' : cert.vidarebefordrad}\"\r" +
    "\n" +
    "                              title=\"Skicka mejl med en länk till utkastet för att informera den läkare som ska signera det.\"\r" +
    "\n" +
    "                              ng-click=\"openMailDialog(cert)\">\r" +
    "\n" +
    "                        <img ng-if=\"!cert.vidarebefordrad\" src=\"/img/mail.png\">\r" +
    "\n" +
    "                        <img ng-if=\"cert.vidarebefordrad\" src=\"/img/mail_dark.png\">\r" +
    "\n" +
    "                      </button>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td class=\"center\">\r" +
    "\n" +
    "                      <input id=\"selected\" type=\"checkbox\" ng-disabled=\"cert.updateState.updateInProgress\" ng-model=\"cert.vidarebefordrad\"\r" +
    "\n" +
    "                             ng-change=\"onForwardedChange(cert)\" />\r" +
    "\n" +
    "                      <span ng-if=\"cert.updateState.updateInProgress\"> <img src=\"/img/ajax-loader-kit-16x16.gif\"></span>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td class=\"unbreakable\"><span message\r" +
    "\n" +
    "                                                  key=\"certificatetypes.{{cert.intygType}}.typename\"></span>\r" +
    "\n" +
    "                    </td>\r" +
    "\n" +
    "                    <td><span message key=\"cert.status.{{cert.status}}\"></span></td>\r" +
    "\n" +
    "                    <td class=\"unbreakable\">{{cert.lastUpdatedSigned | date:'shortDate'}}</td>\r" +
    "\n" +
    "                    <td class=\"unbreakable\">{{cert.patientId}}</td>\r" +
    "\n" +
    "                    <td class=\"table-qa-last\">{{cert.updatedSignedBy}}</td>\r" +
    "\n" +
    "                </table>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <div id=\"showing-nr-hits\" ng-show=\"widgetState.totalCount>0\">Visar\r" +
    "\n" +
    "                träff {{widgetState.queryStartFrom+1}} - {{widgetState.currentList.length}} av\r" +
    "\n" +
    "                {{widgetState.totalCount}}\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "              <div\r" +
    "\n" +
    "                  ng-show=\"((filterForm.lastFilterQuery.startFrom + filterForm.lastFilterQuery.pageSize) < widgetState.totalCount)\">\r" +
    "\n" +
    "                <button class=\"btn btn-default\" title=\"Hämta fler träffar\" ng-click=\"fetchMore()\"\r" +
    "\n" +
    "                        ng-disabled=\"widgetState.fetchingMoreInProgress\">\r" +
    "\n" +
    "                  <img src=\"/img/loader-small.gif\" ng-show=\"widgetState.fetchingMoreInProgress\"> Hämta\r" +
    "\n" +
    "                  fler träffar\r" +
    "\n" +
    "                </button>\r" +
    "\n" +
    "              </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "            </div>\r" +
    "\n" +
    "          </div>\r" +
    "\n" +
    "          <!-- spinner end -->\r" +
    "\n" +
    "        </div>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/dashboard/view.certificate.html',
    "<div id=\"wcHeader\" class=\"print-hide\" wc-header default-active=\"index\"></div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "<div class=\"row webcert-multicol\" id=\"viewCertAndQA\" wc-feature-active feature=\"hanteraIntygsutkast\">\r" +
    "\n" +
    "  <div class=\"webcert-multicol-background\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "    <!-- Left column -->\r" +
    "\n" +
    "    <div class=\"col-sm-6 webcert-col webcert-col-primary\">\r" +
    "\n" +
    "      <div class=\"print-hide\">\r" +
    "\n" +
    "        <a id=\"tillbakaButton\" wc-feature-not-active feature=\"franJournalsystem\" class=\"backlink-icon\" href=\"/web/dashboard#/create/choose-cert-type/index\"\r" +
    "\n" +
    "           title=\"Tillbaka till Sök/skriv intyg\"></a>\r" +
    "\n" +
    "        <h1 class=\"backlink-heading\">\r" +
    "\n" +
    "          <span message key=\"{{widgetState.certificateType}}.label.certtitle\"></span>\r" +
    "\n" +
    "        </h1>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "      <div wc-insert-certificate certificate-type=\"{{widgetState.certificateType}}\"></div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "    <!-- Certificate Left side end -->\r" +
    "\n" +
    "\r" +
    "\n" +
    "    <!-- Right side -->\r" +
    "\n" +
    "    <div class=\"col-sm-6 webcert-col-secondary print-hide\"\r" +
    "\n" +
    "         wc-feature-active feature=\"hanteraFragor\" intygstyp=\"{{widgetState.certificateType}}\">\r" +
    "\n" +
    "      <div class=\"webcert-col-shadow\"></div>\r" +
    "\n" +
    "      <h2 class=\"col-head\">Fråga &amp; Svar</h2>\r" +
    "\n" +
    "      <div wc-insert-qa certificate-type=\"{{widgetState.certificateType}}\"></div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div><!-- Right side end -->\r" +
    "\n"
  );


  $templateCache.put('/views/dashboard/view.qa.html',
    "<div id=\"wcHeader\" class=\"print-hide\" wc-header default-active=\"unhandled-qa\"></div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "<div class=\"row webcert-multicol\" id=\"viewQAAndCert\">\r" +
    "\n" +
    "  <div class=\"webcert-multicol-background\">\r" +
    "\n" +
    "\r" +
    "\n" +
    "    <!-- Left column -->\r" +
    "\n" +
    "    <div class=\"col-sm-6 webcert-col webcert-col-primary print-hide\">\r" +
    "\n" +
    "      <div wc-feature-active feature=\"hanteraFragor\" intygstyp=\"{{widgetState.certificateType}}\">\r" +
    "\n" +
    "        <a id=\"tillbakaButton\" wc-feature-not-active feature=\"franJournalsystem\" class=\"backlink-icon\" href=\"/web/dashboard#/unhandled-qa\" title=\"Tillbaka till Fråga och Svar\"></a>\r" +
    "\n" +
    "        <h1 class=\"backlink-heading\">Fråga och svar</h1>\r" +
    "\n" +
    "        <div wc-insert-qa certificate-type=\"{{widgetState.certificateType}}\"></div>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "    <!-- Certificate Left side end -->\r" +
    "\n" +
    "\r" +
    "\n" +
    "    <!-- Right side -->\r" +
    "\n" +
    "    <div class=\"col-sm-6 webcert-col-secondary\">\r" +
    "\n" +
    "      <div class=\"print-hide\">\r" +
    "\n" +
    "        <div class=\"webcert-col-shadow\"></div>\r" +
    "\n" +
    "        <h2 class=\"col-head\">\r" +
    "\n" +
    "          <span message key=\"{{widgetState.certificateType}}.label.certtitle\"></span>\r" +
    "\n" +
    "        </h2>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "      <div wc-insert-certificate certificate-type=\"{{widgetState.certificateType}}\"></div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div><!-- Right side end -->\r" +
    "\n"
  );


  $templateCache.put('/views/partials/check-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "\t<h3 ng-focus=\"dialog.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "\t</h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "\t<div ng-bind-html=\"bodyText\" style=\"padding-bottom: 10px\"><span ng-if=\"bodyTextId != undefined\" message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\r" +
    "\n" +
    "    <label class=\"checkbox\"><input id=\"dontShowAgain\" type=\"checkbox\" ng-model=\"model.dontShowCopyInfo\"> Visa inte denna information igen</label>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!dialog.acceptprogressdone\" ng-click=\"button1click()\">\r" +
    "\n" +
    "      <img ng-hide=\"dialog.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\r" +
    "\n" +
    "      <span message key=\"{{button3text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"dialog.showerror\">\r" +
    "\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\r" +
    "\n" +
    "        <span message key=\"{{dialog.errormessageid}}\"></span>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/common-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "\t<h3 ng-focus=\"model.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "\t</h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "\t<div ng-bind-html=\"bodyText\"><span ng-if=\"bodyTextId != undefined\" message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "\t<button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\" ng-click=\"button1click()\">\r" +
    "\n" +
    "\t\t<img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\r" +
    "\n" +
    "\t\t<span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "\t</button>\r" +
    "\n" +
    "\t<button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "\t\t<span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "\t</button>\r" +
    "\n" +
    "\t<button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\r" +
    "\n" +
    "\t\t<span message key=\"{{button3text}}\"></span>\r" +
    "\n" +
    "\t</button>\r" +
    "\n" +
    "\t<div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\r" +
    "\n" +
    "\t\t<div class=\"alert alert-danger alert-dialog-error\">\r" +
    "\n" +
    "\t\t\t<span message key=\"{{model.errormessageid}}\"></span></div>\r" +
    "\n" +
    "\t</div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/copy-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "\t<h3 ng-focus=\"dialog.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "\t</h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  <p>\r" +
    "\n" +
    "    Kopiera intyg innebär att en kopia skapas av det befintliga intyget och med samma information.\r" +
    "\n" +
    "    <span ng-show=\"model.otherCareUnit != undefined\" id=\"annanVardenhet\">\r" +
    "\n" +
    "      Om intyget är utfärdat på en annan vårdenhet kommer adressuppgifterna att ersättas med de för den vårdenhet du är inloggad på.\r" +
    "\n" +
    "    </span>\r" +
    "\n" +
    "    <span ng-show=\"model.patientId != undefined && model.patientId.length > 0\" id=\"msgNyttPersonId\">\r" +
    "\n" +
    "      <br>För denna patient finns nytt person-id och informationen kommer därför uppdateras i det nya intyget.\r" +
    "\n" +
    "    </span>\r" +
    "\n" +
    "    <span ng-show=\"!model.deepIntegration\" id=\"msgInteFranJournalSystem\">\r" +
    "\n" +
    "      I de fall patienten har ändrat namn eller adress så uppdateras den informationen.\r" +
    "\n" +
    "    </span>\r" +
    "\n" +
    "  </p>\r" +
    "\n" +
    "  <p>Uppgifterna i intygsutkastet går att ändra innan det signeras.</p>\r" +
    "\n" +
    "  Kopiera intyg kan användas exempelvis vid förlängning av en sjukskrivning.\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\r" +
    "\n" +
    "    <label class=\"checkbox\"><input id=\"dontShowAgain\" type=\"checkbox\" ng-model=\"model.dontShowCopyInfo\"> Visa inte denna information igen</label>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "  <div class=\"webcert-top-padding-section form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\" ng-click=\"button1click()\">\r" +
    "\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\r" +
    "\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\r" +
    "\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );


  $templateCache.put('/views/partials/error-dialog.html',
    "<div class=\"modal-header\">\r" +
    "\n" +
    "    <h3>Tekniskt fel</h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  {{bodyText}}\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "    <button class=\"btn btn-success\" ng-click=\"$close()\">OK</button>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/general-dialog.html',
    "<div class=\"modal-header\">\r" +
    "\n" +
    "    <button class=\"close\" ng-click=\"$dismiss()\">×</button>\r" +
    "\n" +
    "    <h3>\r" +
    "\n" +
    "        <span message key=\"modal.title.{{title}}\"></span>\r" +
    "\n" +
    "    </h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "    {{bodyText}}\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "    <button id=\"buttonYes\" class=\"btn btn-success\" ng-click=\"yes()\">\r" +
    "\n" +
    "        <span message key=\"common.yes\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"buttonNo\" class=\"btn btn-info\" ng-click=\"no()\">\r" +
    "\n" +
    "        <span message key=\"common.no\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"buttonNoDontAsk\" class=\"btn btn-info\" ng-click=\"noDontAsk()\">\r" +
    "\n" +
    "        <span message key=\"common.nodontask\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/makulera-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "    <span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "  </h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.acceptprogressdone\"\r" +
    "\n" +
    "            ng-click=\"button1click()\">\r" +
    "\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\" />\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\r" +
    "\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\r" +
    "\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/preference-dialog.html',
    "<div class=\"modal-header\">\r" +
    "\n" +
    "  <button class=\"close\" ng-click=\"$dismiss()\">×</button>\r" +
    "\n" +
    "  <h3>\r" +
    "\n" +
    "    <span message key=\"modal.title.{{title}}\"></span>\r" +
    "\n" +
    "  </h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  {{bodyText}}\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <button id=\"buttonYes\" class=\"btn btn-success\" ng-click=\"yes()\">\r" +
    "\n" +
    "    <span message key=\"common.yes\"></span>\r" +
    "\n" +
    "  </button>\r" +
    "\n" +
    "  <button id=\"buttonNo\" class=\"btn btn-info\" ng-click=\"no()\">\r" +
    "\n" +
    "    <span message key=\"common.no\"></span>\r" +
    "\n" +
    "  </button>\r" +
    "\n" +
    "  <button id=\"buttonNoDontAsk\" class=\"btn btn-info\" ng-click=\"noDontAsk()\">\r" +
    "\n" +
    "    <span message key=\"common.nodontask\"></span>\r" +
    "\n" +
    "  </button>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/qa-check-hanterad-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "    <span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "  </h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "  <div class=\"form-group form-inline\">\r" +
    "\n" +
    "    <label class=\"checkbox\" ng-click=\"model.widgetState.setSkipShowUnhandledDialog(model.widgetState)\">\r" +
    "\n" +
    "      <input type=\"checkbox\" id=\"preferenceSkipShowUnhandledDialog\" name=\"preferenceSkipShowUnhandledDialog\" ng-model=\"model.widgetState.skipShowUnhandledDialog\" ng-change=\"model.widgetState.setSkipShowUnhandledDialog(model.widgetState)\">\r" +
    "\n" +
    "      <span message key=\"label.qacheckhanterad.checkbox\"></span></label>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-click=\"button1click()\">\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button3id}}\" class=\"btn btn-info\" ng-click=\"button3click()\">\r" +
    "\n" +
    "      <span message key=\"{{button3text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/qa-only-warning-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "  <button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "  <h3 ng-focus=\"model.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "    <span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "  </h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "  <div style=\"padding-bottom: 10px\"><span message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-click=\"button1click()\">\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>\r" +
    "\n"
  );


  $templateCache.put('/views/partials/send-dialog.html',
    "<div id=\"{{dialogId}}\" class=\"modal-header\">\r" +
    "\n" +
    "\t<button class=\"close\" ng-click=\"button2click()\">×</button>\r" +
    "\n" +
    "\t<h3 ng-focus=\"model.focus\" tabindex=\"-1\">\r" +
    "\n" +
    "\t\t<span message key=\"{{titleId}}\"></span>\r" +
    "\n" +
    "\t</h3>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-body\">\r" +
    "\n" +
    "\t<div ng-bind-html=\"bodyText\" style=\"padding-bottom: 10px\"><span ng-if=\"bodyTextId != undefined\" message key=\"{{bodyTextId}}\"></span></div>\r" +
    "\n" +
    "</div>\r" +
    "\n" +
    "<div class=\"modal-footer\">\r" +
    "\n" +
    "  <div class=\"form-group\">\r" +
    "\n" +
    "    <label class=\"checkbox\"><input id=\"patientSamtycke\" type=\"checkbox\" ng-model=\"model.patientConsent\"> Patienten samtycker till att intyget skickas.</label>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "  <div class=\"form-inline\">\r" +
    "\n" +
    "    <button id=\"{{button1id}}\" class=\"btn btn-success\" ng-disabled=\"!model.patientConsent || !model.acceptprogressdone\" ng-click=\"button1click()\">\r" +
    "\n" +
    "      <img ng-hide=\"model.acceptprogressdone\" ng-src=\"/img/ajax-loader-small-green.gif\"/>\r" +
    "\n" +
    "      <span message key=\"{{button1text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button2id}}\" class=\"btn btn-info\" ng-click=\"button2click()\">\r" +
    "\n" +
    "      <span message key=\"{{button2text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <button id=\"{{button3id}}\" ng-show=\"button3visible\" class=\"btn btn-info\" ng-click=\"button3click()\">\r" +
    "\n" +
    "      <span message key=\"{{button3text}}\"></span>\r" +
    "\n" +
    "    </button>\r" +
    "\n" +
    "    <div class=\"webcert-top-padding-section\" data-ng-show=\"model.showerror\">\r" +
    "\n" +
    "      <div class=\"alert alert-danger alert-dialog-error\">\r" +
    "\n" +
    "        <span message key=\"{{model.errormessageid}}\"></span>\r" +
    "\n" +
    "      </div>\r" +
    "\n" +
    "    </div>\r" +
    "\n" +
    "  </div>\r" +
    "\n" +
    "</div>"
  );

}]);
