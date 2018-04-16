#language: sv
@avliden
Egenskap: Avliden patient

@PS-001
  Scenario: Varning om patient är avliden
     Givet att jag är inloggad som läkare
     När jag går in på en patient som är avliden
     Så ska jag varnas om att "Patienten är avliden"

# TODO: Skriv om detta och sätt patient till avliden via APIet för avliden
 @skicka-till-FK @integration @notReady
 Scenario: Kan skicka och ställa frågor på intyg
     Givet att jag är inloggad som djupintegrerad läkare
     När jag skickar ett SMI-intyg till intygstjänsten på en avliden person
     Och jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
     Och jag skickar intyget till Försäkringskassan
     Så ska intygets status vara "Intyget är skickat till Försäkringskassan"
	 Och ska intygets status vara "Intyget är tillgängligt för patienten"

	 
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
    Så ska intygets status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets status vara "Intyget är tillgängligt för patienten"
