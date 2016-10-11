# language: sv
@komplettering @luse @INTYG-2642
 # kompletteringsfråga saknas på utkast-sida
Egenskap: Komplettering av LUSE-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient
   
@nyttIntyg
Scenario: Ska kunna besvara komplettering med nytt intyg
   När jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag ska se den data jag angett för intyget
   Och jag skickar intyget till Försäkringskassan
   Så ska intygets status vara "Intyget är signerat"

   När Försäkringskassan skickar ett "Komplettering" meddelande på intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar intyget
   Så jag ska se den data jag angett för intyget

# Scenario: Ska kunna besvara komplettering med textmeddelande
#    När jag går in på ett "Läkarintyg för sjukpenning utökat" med status "Mottaget"
#    När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
#    Och jag går in på intyget
#    Så ska jag se kompletteringsfrågan på intygs-sidan
#    Och jag ska kunna svara med textmeddelande
