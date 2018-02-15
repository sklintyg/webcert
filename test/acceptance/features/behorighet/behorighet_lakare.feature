# language: sv
@behorighet @lakare @hurr
Egenskap: Behörigheter för en läkare

@smi @inloggad-enhet
Scenario: Kan endast nå intyg på inloggad vårdenhet
	Givet att jag är inloggad som läkare utan angiven vårdenhet
	Så ska jag se en rubrik med texten "Välj din vårdenhet"

	När jag väljer vårdenheten "TSTNMT2321000156-107P"
	Så ska jag se en rubrik med texten "Sök/skriv intyg"

	När jag går in på en patient
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på utkastet 
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

	Och jag sparar länken till aktuell sida

	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och går till den sparade länken
	Så ska ett fel-meddelande visa "Kunde inte hämta intyget eftersom du saknar behörighet"


@underliggande-enhet @smi
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
	Så ska intygets status vara "Intyget är signerat"


@fornya-utkast @lisjp
Scenario: Det går att förnya signerade och mottagna intyg från intygslistan men inte utkast
	Givet att jag är inloggad som läkare
	Och jag går in på en patient
	Så ska Förnya-knappen visas för aktuella signerade eller mottagna "Läkarintyg för sjukpenning"-intyg

   	Givet att det finns intygsutkast
   	Så ska Förnya-knappen inte visas för något utkast
