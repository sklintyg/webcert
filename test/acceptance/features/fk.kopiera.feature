# language: sv
@kopiera @aa
Egenskap: Kopiera FK-intyg

Bakgrund: Jag är inne på en patient
	Givet att jag är inloggad som läkare utan adress till enheten
	När jag går in på en patient

Scenariomall: Enhetens adress ska följa med när jag kopierar ett <intygKod>-intyg om adress saknas i HSA
	När jag går in på att skapa ett <intyg> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag kopierar intyget
	Så ska adressen kopieras till det kopierade intyget


Exempel:
  |intygKod | 	intyg 								| 
  |LUSE		|  	"Läkarutlåtande för sjukersättning" | 
  |FK7263	| 	"Läkarintyg FK 7263"				| 
