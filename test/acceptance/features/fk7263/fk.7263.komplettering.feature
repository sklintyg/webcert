# language: sv
@komplettera @fk7263
Egenskap: Komplettering av FK7263-intyg


Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

@nyttIntyg
Scenario: Ska kunna besvara komplettering med nytt FK7263-intyg
   Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag ska se den data jag angett för intyget
   Och jag skickar intyget till Försäkringskassan
   Så ska intygets status vara "Intyget är signerat"

   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar intyget
   Så jag ska se den data jag angett för intyget

@textsvar @waitingForFix
Scenario: Ska kunna besvara komplettering med textmeddelande
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan 
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget
   Så ska jag se kompletteringsfrågan på intygs-sidan
   Och jag ska kunna svara med textmeddelande

@fortsattUtkast
Scenario: Ska kunna fortsätta besvara kompletterande FK7283-intyg 
   Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   #När jag går in på ett "Läkarintyg FK 7263" med status "Signerat" 
   Och jag skickar intyget till Försäkringskassan
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Och jag går tillbaka till intyget som behöver kompletteras
   Så ska det finnas en knapp med texten "Fortsätt på intygsutkast"
   
