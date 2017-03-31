#language: sv

@ersatt
Egenskap: Funktionallitet som rör ersätt

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
    När jag går in på en patient

@ersattBtn
Scenario: Finns ersätta-knappen
    När jag går in på att skapa ett slumpat intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska jag knapp med texten "Ersätt"
