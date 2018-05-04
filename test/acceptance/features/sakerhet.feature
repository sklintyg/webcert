# language: sv
@SAKERHET
Egenskap: Tester för säkerhet

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

Scenario: Vidarebefodrad till intygs-Vy
   Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
   Och jag går in på utkastet 
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget

   Och jag går in på intyget med edit länken
   Så ska jag komma till intygssidan