# language: sv
@BACKOFFICE
Egenskap: Skapa statistik-data

Bakgrund: Inloggad som läkare
	Givet att jag är inloggad som läkare
	
@TESTDATA-STATISTIK @KARLJOHANSSON
Scenariomall: [Statistik] - Generera intyg för <Patient> med statistikData <statistikData>
	
	Givet att jag är inloggad som läkare "Karl Johansson"
	När jag går in på testpatienten "<Patient>"
	Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget med statistikdata "<statistikData>"
	Så är signeraknappen tillgänglig
	När jag signerar intyget
	Så ska jag se den data jag angett för intyget

Exempel:
	| Patient | statistikData |
	| 19000102-9818 | 0 |
	| 19010209-9801 | 1 |
	| 19021112-9812 | 2 |
	| 20020102-2381 | 3 |
	| 20020109-2392 | 4 |
	
	
@TESTDATA-STATISTIK @LENNARTJOHANSSON
Scenariomall: [Statistik] - Generera intyg för <Patient> med statistikData <statistikData>
	
	Givet att jag är inloggad som läkare "Lennart Johansson Persson"
	När jag går in på testpatienten "<Patient>"
	Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget med statistikdata "<statistikData>"
	Så är signeraknappen tillgänglig
	När jag signerar intyget
	Så ska jag se den data jag angett för intyget

Exempel:
	| Patient | statistikData |
	| 19700123-9297 | 5 |
	| 20130106-2384 | 6 |
	| 20130118-2380 | 7 |
	| 20130120-2386 | 8 |
	| 19800514-9284 | 9 |