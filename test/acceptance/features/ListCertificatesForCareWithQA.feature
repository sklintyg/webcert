# language: sv

@LIST-CERTIFICATES-FOR-CARE-WITH-QA @DI-027
Egenskap: DI-027 - ListCertificatesForCareWithQA - Journalsystem integration

Bakgrund: Jag är inloggad som djupintegrerad läkare
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"


@SMOKE @SMI
Scenario: DI-002 - Hämta en lista med intyg och utkast för en specifik vårdenhet och patient
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret endast innehålla intyg för utvald patient
    Och ska svaret endast innehålla intyg för vårdenheten

@SAMORDNINGSNUMMER @TS @TS-DIABETES
Scenariomall: DI-002 - Hämta en lista med intyg och utkast för en patient med samordningsnummer
    Givet att vårdsystemet skapat ett intygsutkast för <intygsTyp> med samordningsnummer
    Och jag går in på intygsutkastet via djupintegrationslänk
    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret endast innehålla intyg för utvald patient
	
Exempel: 
	|	intygsTyp									|
	|	"Transportstyrelsens läkarintyg högre körkortsbehörighet"			|
	|	"Transportstyrelsens läkarintyg diabetes"	|

@HÄNDELSER
@SKAPAT @ANDRAT @SIGNAT @SKICKA @MAKULE 

@MAKULERA @SIGNERA @FK @SMI
Scenario: DI-027 - Hämta händelser för ett intyg
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på

    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan
    Och jag makulerar intyget

    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret visa intyghändelse "SKAPAT"
    Och ska svaret visa intyghändelse "ANDRAT"
    Och ska svaret visa intyghändelse "SIGNAT"
    Och ska svaret visa intyghändelse "SKICKA"
    Och ska svaret visa intyghändelse "MAKULE"
   
@FRÅGA-FRÅN-VÅRDEN @SMI
Scenario: DI-027 - Hämta fråga/svar händelser för frågor från vården
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk

    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa skickade frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0

    När Försäkringskassan skickar ett svar
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0

    När jag markerar svaret från Försäkringskassan som hanterad
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1

    När jag markerar svaret från Försäkringskassan som INTE hanterad
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0

@HÄNDELSER
@KONTKT

@FRÅGA-FRÅN-FK @SMI
Scenario: DI-027 - Hämta fråga/svar händelser för frågor från FK
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När Försäkringskassan skickar ett "KONTKT" meddelande på intyget

    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0

    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag svarar på frågan

    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1
    Och ska responsen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0

@RADERA @SMI
Scenario: DI-027 - Hämta radera händelse för ett intygutkast
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag raderar utkastet

    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret inte innehålla intyget jag var inne på