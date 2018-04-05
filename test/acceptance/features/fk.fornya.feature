# language: sv
@fornya
Egenskap: Förnya FK-intyg

Bakgrund: Jag är inne på en patient
	Givet att jag är inloggad som läkare utan adress till enheten
	När jag går in på en patient

@enhetsaddress
Scenariomall: Enhetens adress ska följa med när jag förnyar ett <intygKod>-intyg om adress saknas i HSA
	När jag går in på att skapa ett <intyg> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag uppdaterar enhetsaddress
	Och jag signerar intyget
	Och jag förnyar intyget
	Så ska adressen vara ifylld på det förnyade intyget


Exempel:
  |intygKod | 	intyg 								| 
  |LUSE		|  	"Läkarutlåtande för sjukersättning" | 
