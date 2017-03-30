# language: sv
@fk7263
Egenskap: Förnya FK7263-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@fornya
Scenario: Skapa ett intyg som sedan förnyas och signeras
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget

	Och jag förnyar intyget
	Så ska fält för Baserat på vara tomma
	Och ska fält för Bedömning av arbetsförmåga vara tomma
	Och ska fält för Kontakt med FK vara tom

	När jag anger datum för Baserat på
	Och jag anger datum för arbetsförmåga
	Och jag anger kontakt med FK
	Och jag signerar intyget
	Så jag ska se den data jag angett för intyget
	Och ska intygets status vara "Intyget är signerat"	
