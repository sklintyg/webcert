# language: sv
@sakerhet
Egenskap: Tester för säkerhet

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

Scenario: Vidarebefodrad till intygs-Vy
   När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Och jag går in på intyget med edit länken
   Så ska jag komma till intygssidan