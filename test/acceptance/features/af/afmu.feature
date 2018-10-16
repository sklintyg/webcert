# language: sv

@AF @AFMU
Egenskap: Hantera Arbetsförmedlingens medicinska utlåtande intyg

Bakgrund: Jag är inloggad
    Givet att jag är inloggad som läkare
    När jag går in på en patient

Scenario: Arbetsförmedlingens medicinska utlåtande intyg ska ha rätt status efter signering
	När jag går in på att skapa ett "Arbetsförmedlingens medicinska utlåtande" intyg
	Så ska intygets första status vara "Obligatoriska uppgifter saknas"
	När jag fyller i alla nödvändiga fält för intyget
	Så ska intygets första status vara "Klart att signera"
	Och ska intygets andra status vara "Utkastet är sparat"
	Och jag signerar intyget
	Så ska intygets första status vara "Intyget är skickat till Arbetsförmedlingen"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
	Så ska intygets första status vara "Intyget är skickat till Arbetsförmedlingen"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"