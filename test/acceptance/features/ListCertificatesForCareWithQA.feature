# language: sv

@ListCertificatesForCareWithQA
Egenskap: ListCertificatesForCareWithQA från journalsystem

Bakgrund: Jag är inloggad som djupintegrerad läkare
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg


@smoke
Scenario: Hämta en lista med intyg och utkast för en specifik vårdenhet och patient
    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret endast innehålla intyg för utvald patient
    Och ska svaret endast innehålla intyg för vårdenheten

@samordningsnummer @notReady
Scenario: Hämta en lista med intyg och utkast för en patient med samordningsnummer
  När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
  Så ska svaret innehålla intyget jag var inne på
  Och ska svaret endast innehålla intyg för utvald patient

@händelser
Scenario: Hämta händelser för ett intyg
   När jag går in på intygsutkastet via djupintegrationslänk
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


   # skapat, skickat, ändrat,makulerat
@fråga-från-vården
Scenario: Hämta fråga/svar händelser för frågor från vården
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

    När jag markerar svaret från Försäkringskassan som hanterat
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1

    När jag markerar svaret från Försäkringskassan som INTE hanterat
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0


@fråga-från-fk
Scenario: Hämta fråga/svar händelser för frågor från FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När Försäkringskassan skickar ett "KONTKT" meddelande på intyget

    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0
    Och ska responsen visa skickade frågor totalt o,ej besvarade 0,besvarade 1, hanterade 0

    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag svarar på frågan

    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska responsen visa mottagna frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1
    Och ska responsen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0

@radera
Scenario: Hämta radera händelse för ett intygutkast
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag raderar utkastet

    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret inte innehålla intyget jag var inne på