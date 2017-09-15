#language: sv
@avliden
Egenskap: Avliden patient

  Scenario: Varning om patient är avliden
     Givet att jag är inloggad som läkare
     När jag går in på en patient som är avliden
     Så ska jag varnas om att "Patienten har avlidit"


 @skicka-till-FK @integration @notReady
 Scenario: Kan skicka och ställa frågor på intyg
     Givet att jag är inloggad som djupintegrerad läkare
     När jag skickar ett SMI-intyg till intygstjänsten på en avliden person
     Och jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
     Och jag skickar intyget till Försäkringskassan
     Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system"

 @svara-på-fråga @smi @integration
 Scenario: Försäkringskassan kan ställa frågor på ett intyg
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan ställer en "OVRIGT" fråga om intyget
    Och jag svarar på frågan
    Så ska det synas vem som svarat

@svara-på-komplettering @djupintegration @smi @integration
Scenario: Kan svara på komplettering med ett nytt intyg
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
    Och jag går in på intyget via djupintegrationslänk
    Och jag väljer att svara med ett nytt intyg
    Så ska jag se kompletteringsfrågan på utkast-sidan
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat, skickat och mottaget av Försäkringskassans system"
