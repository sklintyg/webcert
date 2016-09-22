# language: sv
@visning @fmb 
Egenskap: Visning av FMB info

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@fk7263 
Scenario: FMB information ska visas vid rätt fält för FK7263
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i diagnoskod 
	Så ska rätt info gällande FMB visas

 @fk7263 
Scenario: FMB information ska visas vid rätt fält för Läkarintyg för sjukpenning utökat
	När jag går in på att skapa ett "Läkarintyg för sjukpenning utökat" intyg
	Och jag fyller i diagnoskod
	Så ska rätt info gällande FMB visas

 @LISU
Scenario: FMB information för treställig diagnoskod ska visas vid rätt fält då koden inte har egen FMB info när Läkarintyg för sjukpenning utökat skapas
	När jag går in på att skapa ett "Läkarintyg för sjukpenning utökat" intyg
	Och jag fyller i diagnoskod utan egen FMB info
	Så ska FMB info för överliggande diagnoskod visas

@fk7263
Scenario: FMB information för treställig diagnoskod ska visas vid rätt fält då koden inte har egen FMB info när FK7263 skapas
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i diagnoskod utan egen FMB info
	Så ska FMB info för överliggande diagnoskod visas

 @fk7263 
Scenario: FMB information ska inte visas för alla diagnoskoder då man skapar Läkarintyg för sjukpenning utökat
	När jag går in på att skapa ett "Läkarintyg för sjukpenning utökat" intyg
	Och jag fyller i diagnoskod utan FMB info
	Så ska ingen info gällande FMB visas

@fk7263 
Scenario: FMB information ska inte visas för alla diagnoskoder då man skapar Läkarintyg FK 7263
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i diagnoskod utan FMB info
	Så ska ingen info gällande FMB visas
