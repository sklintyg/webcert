# language: sv
@BACKOFFICE
Egenskap: Skapa statistik-data

Bakgrund: Ingen bakgrund
	
@TESTDATA-STATISTIK @KARLJOHANSSON
Scenariomall: [Statistik] - Generera intyg på <Patient> med statistikData <statistikData>
	
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
Scenariomall: [Statistik] - Generera intyg på <Patient> med statistikData <statistikData>
	
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
	
@TESTDATA-STATISTIK
Scenariomall: [Statistik] - Generera <intygstyp> på <Patient> för <lakare>
	Givet att jag är inloggad som läkare <lakare>
	När jag går in på testpatienten "<Patient>"
	Och jag går in på att skapa ett <intygstyp> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska jag se den data jag angett för intyget

	Exempel:
	| Patient | lakare | intygstyp |
	| 19900503-2389 | "Karl Johansson" | "Transportstyrelsens läkarintyg högre körkortsbehörighet" |
	| 19900503-2389 | "Karl Johansson" | "Transportstyrelsens läkarintyg diabetes" |
	| 19950516-2397 | "Karl Johansson" | "Läkarutlåtande för sjukersättning" |
	| 19950529-2384 | "Karl Johansson" | "Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
	| 20100712-2383 | "Karl Johansson" | "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" |
	| 20101211-2395 | "Karl Johansson" | "Dödsbevis" |
	| 19600617-2818 | "Karl Johansson" | "Dödsorsaksintyg" |
	
	| 19900503-2389 | "Lennart Johansson Persson" | "Transportstyrelsens läkarintyg högre körkortsbehörighet" |
	| 19900503-2389 | "Lennart Johansson Persson" | "Transportstyrelsens läkarintyg diabetes" |
	| 19950516-2397 | "Lennart Johansson Persson" | "Läkarutlåtande för sjukersättning" |
	| 19950529-2384 | "Lennart Johansson Persson" | "Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
	| 20100712-2383 | "Lennart Johansson Persson" | "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" |
	| 19900509-2391 | "Lennart Johansson Persson" | "Dödsbevis" |
	| 19650201-3755 | "Lennart Johansson Persson" | "Dödsorsaksintyg" |