# language: sv

@pdl @srs @GE-005
Egenskap: GE-005 - PDL-loggning för SRS

Bakgrund:
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"
	Och jag går in på en patient

# Log.activity.activityType = "Läsa"
# Log.activity.activityArg = "Prediktion från SRS av risk för lång sjukskrivning"
@SRS-US-W07 @prediktion
Scenario: GE-005 - Loggning vid visning av prediktion
    Givet en patient som "har givit samtycke" till SRS
    Och att vårdsystemet skapat ett intygsutkast för samma patient för "Läkarintyg FK 7263" 
  	Och jag går in på utkastet
    Och jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
