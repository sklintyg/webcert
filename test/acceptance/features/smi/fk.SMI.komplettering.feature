# language: sv
@komplettering @smi @INTYG-2642 @INTYG-3778
 # kompletteringsfråga saknas på utkast-sida
Egenskap: Komplettering av SMI-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient
   
@nyttIntyg
Scenario: Ska kunna besvara komplettering med nytt SMI-intyg
   När jag går in på att skapa ett slumpat SMI-intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

   När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar intyget
   Så jag ska se den data jag angett för intyget

@komplettering @utkast @fortsattUtkast
Scenario: Ska kunna fortsätta besvara kompletterande SMI-intyg 
   När jag går in på ett slumpat SMI-intyg med status "Signerat" 
   Och jag skickar intyget till Försäkringskassan
   När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Och jag sparar länken till aktuell sida
   Och jag går tillbaka till intyget som behöver kompletteras
   Så ska det finnas en knapp med texten "Fortsätt på intygsutkast"
   Och jag trycker på knappen med texten "Fortsätt på intygsutkast"
   Så jag verifierar att URL:en är samma som den sparade länken