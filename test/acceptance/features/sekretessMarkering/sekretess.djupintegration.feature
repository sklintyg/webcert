# language: sv
@SEKRETESSMARKERING @SAKERHET @DJUPINTEGRATION
Egenskap: djupintegration - Sekretessmarkerad patient

@SMI @SIGNERA @F.BE-009
Scenario: Vid djupintegration ska SJF flaggan inte ge några extra rättigheter om patienten är sekrettessmarkerad.
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och jag går in på en patient med sekretessmarkering
	Och att vårdsystemet skapat ett intygsutkast för samma patient för slumpat SMI-intyg
   
	När jag går in på intygsutkastet via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
   
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
    När jag går in på intyget via djupintegrationslänk med parameter "sjf=true"
	Så ska jag varnas om att "Behörighet saknas"