#language: sv
@avliden
Egenskap: Det ska inte vara möjligt att skapa intyg för avliden patient


Scenario: Användaren ska varnas att patienten är avliden
   Givet att jag är inloggad som läkare
   När jag går in på en patient som är avliden
   Så ska jag varnas om att "Patienten har avlidit"

@notReady
Scenario:Användaren ska inte kunna kopiera intyg som är utskrivet till en avliden
   Givet att jag är inloggad som djupintegrerad läkare
   När jag skickar ett SMI-intyg till intygstjänsten på en avliden person
   När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
   Och jag skickar intyget till Försäkringskassan
   När Försäkringskassan skickar ett "KONTKT" meddelande på intyget
   Och jag svarar på frågan
