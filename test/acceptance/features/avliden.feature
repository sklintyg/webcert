#language: sv
@avliden
Egenskap: Det ska inte vara möjligt att skapa intyg för avliden patient


Scenario: Användaren ska varnas att patienten är avliden
   Givet att jag är inloggad som läkare
   När jag går in på en patient som är avliden
   Så ska jag varnas om att "Patienten har avlidit"


@notReady 
Scenario:Användaren ska kunna skicka ett intyg på en avliden till Försäkringskassan

   Givet att jag är inloggad som djupintegrerad läkare
   När jag skickar ett SMI-intyg till intygstjänsten på en avliden person
   När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
   Och jag skickar intyget till Försäkringskassan
   Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system"

@notReady
Scenario: Försäkringskassan ska kunna ställa en fråga på ett intyg som tillhör en avliden
Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
   Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan ställer en "OVRIGT" fråga om intyget
    Och jag svarar på frågan
 
