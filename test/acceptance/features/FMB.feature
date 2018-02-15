# language: sv
@visning @fmb
Egenskap: Visning av FMB information

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient


Scenariomall: Visas vid rätt fält för <intygKod>
	Givet att vårdsystemet skapat ett intygsutkast för <intyg>
	Och jag går in på utkastet
	Och jag fyller i diagnoskod
	Så ska rätt info gällande FMB visas

@fk7263 @LegacyFk7263
Exempel:
  |intygKod | 	intyg 								|
  |FK7263	|  	"Läkarintyg FK 7263" 				|

@lisjp @smi
  Exempel:
  |intygKod | 	intyg 								|
  |LISJP		| 	"Läkarintyg för sjukpenning" |

@treställig
Scenariomall: FMB information för treställig diagnoskod ska visas vid rätt fält då koden inte har egen FMB info när <intygKod> skapas
	Givet att vårdsystemet skapat ett intygsutkast för <intyg>
	Och jag går in på utkastet
	Och jag fyller i diagnoskod utan egen FMB info
	Så ska FMB info för överliggande diagnoskod visas

Exempel:
  |intygKod | 	intyg 								|
  |FK7263	|  	"Läkarintyg FK 7263" 				|
  |LISJP		| 	"Läkarintyg för sjukpenning" |

Scenariomall: Ska inte visas för alla diagnoskoder då man skapar <intygKod>
	Givet att vårdsystemet skapat ett intygsutkast för <intyg>
	Och jag går in på utkastet
	Och jag fyller i diagnoskod utan FMB info
	Så ska ingen info gällande FMB visas

Exempel:
  |intygKod | 	intyg 								|
  |FK7263	|  	"Läkarintyg FK 7263" 				|
  |LISJP		| 	"Läkarintyg för sjukpenning" |
