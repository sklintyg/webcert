# language: sv
@BEHÖRIGHET @lakare
Egenskap: GE-007 - Behörigheter för en läkare

@SMI @INLOGGAD-ENHET @VA-002 @F.BE-009
Scenario: Kan endast nå intyg på inloggad vårdenhet
	Givet att jag är inloggad som läkare utan angiven vårdenhet
	Så ska jag se en rubrik med texten "Välj din vårdenhet"

	När jag väljer vårdenheten "TSTNMT2321000156-107P"

	Och jag går in på en patient
	Och jag går in på ett slumpat SMI-intyg med status "Signerat"

	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"

@UNDERLIGGANDE-ENHET @SMI @VA-002
Scenario: Kan se intyg på underenheter när jag loggar in på överliggande enhet
	Givet att jag är inloggad som läkare på underenhet "TSTNMT2321000156-UND2"
	När jag går in på en patient
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på utkastet 
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-107Q"
	Och går till den sparade länken
	Så ska intygets första status vara "Intyget är signerat"


