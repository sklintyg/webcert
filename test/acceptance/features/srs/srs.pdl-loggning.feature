# language: sv

@pdl @srs
Egenskap: PDL-loggning för SRS

Bakgrund:
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"

# Log.activity.activityType = "Läsa"
# Log.activity.activityArg = "Prediktion från SRS av risk för lång sjukskrivning"
@SRS-US-W07 @prediktion
Scenario: Loggning vid visning av prediktion
    Givet en patient som "har givit samtycke" till SRS
    Och att vårdsystemet skapat ett intygsutkast för samma patient för "Läkarintyg FK 7263" 
  	Och jag går in på utkastet
    Och jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Och spara användare till globaluser
    Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
