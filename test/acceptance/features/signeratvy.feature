# language: sv
@SIGNERATVY
Egenskap: Datamängder i signerat-vy

Bakgrund: Jag är inne på en patient
	Givet att jag är inloggad som läkare
	När jag går in på en patient

Scenariomall: Signera <intyg> och kontrollera fält i signerat vyn
		  Och jag går in på att skapa ett "<intyg>" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Så ska jag se den data jag angett för intyget

Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg högre körkortsbehörighet           |
  | Transportstyrelsens läkarintyg diabetes                           |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |