# language: sv
@fk7263
Egenskap: Kopiera FK7263-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare utan adress till enheten
	När jag går in på en patient

@kopiera @kristina
Scenario: Skapa ett intyg som sedan kopieras och enhetens adress följer med
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag fyller i adress
	Och jag signerar intyget

	Och jag kopierar intyget
	Så ska adressen kopieras till det kopierade intyget
	
