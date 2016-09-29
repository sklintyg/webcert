# language: sv
@smi
Egenskap: Kopiera SMI-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare utan adress till enheten
	När jag går in på en patient

@kopiera 
Scenario: Skapa ett intyg som sedan kopieras och enhetens adress följer med
	När jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag fyller i adress
	Och jag signerar intyget

	Och jag kopierar intyget
	Så ska adressen kopieras till det kopierade intyget
