# language: sv

@kopiera-knapp 
Egenskap: Makulerat intyg ska kunna kopieras

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

@FK7263-kopiera-knapp 
Scenario: Det ska gå att kopiera ett makulerat FK7263 intyg 
 	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat" 
   	Och jag skickar intyget till Försäkringskassan
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"
	Så ska det finnas en knapp med texten "Kopiera"

@SMI-kopiera-knapp
Scenario: Det ska gå att kopiera slumpat och makulerat SMI-intyg
   När jag går in på ett slumpat intyg med status "Signerat"
   Och jag skickar intyget till Försäkringskassan
   Och jag makulerar intyget
   Så ska intyget visa varningen "Intyget är makulerat"
   Så ska det finnas en knapp med texten "Kopiera"
