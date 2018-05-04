# language: sv
@WIP
Egenskap: Skapa statistik-data

Bakgrund: Inloggad som läkare
	Givet att jag är inloggad som läkare
	
@WIP
Scenariomall: [Statistik] - Generera intyg för <Patient> med statistikData <statistikData>
	
	Givet att jag är inloggad som läkare "Karl Johansson"
	När jag går in på testpatienten "<Patient>"
	Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget med statistikdata "<statistikData>"
	Så är signerknappen tillgänglig
	När jag signerar intyget
	Så ska jag se den data jag angett för intyget

Exempel:
	|Patient|statistikData|
	|190001029818|0|
	|190102099801|1|
	|190211129812|2|
	|200201022381|3|
	|200201092392|4|
	
	
@WIP
Scenariomall: [Statistik] - Generera intyg för <Patient> med statistikData <statistikData>
	
	Givet att jag är inloggad som läkare "Lennart Johansson Persson"
	När jag går in på testpatienten "<Patient>"
	Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget med statistikdata "<statistikData>"
	Så är signerknappen tillgänglig
	När jag signerar intyget
	Så ska jag se den data jag angett för intyget

Exempel:
	|Patient|statistikData|
	|197001239297|5|
	|201301062384|6|
	|201301182380|7|
	|201301202386|8|
	|198005149284|9|