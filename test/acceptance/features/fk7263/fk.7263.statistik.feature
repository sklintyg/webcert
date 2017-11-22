# language: sv

@statistik @integration @fk7263 @waitingForFix @INTYG-5034
Egenskap: FK7263-integration med Statistiktjänsten

#Bakgrund: Jag befinner mig på Rehabstöds förstasida

@skapa @databas
Scenario: Ett nyskapat FK7263 intyg ska finnas i Statistiktjänstens databas
    När att jag är inloggad som läkare
	Och jag går in på en patient
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
    Så ska jag se intyget i databasen

@skapa @gui
Scenario: Ett nyskapat och makulerat FK7263 intyg ska finnas och senare tas bort från Statistiktjänstens GUI
    När att jag är inloggad som läkare
	Och jag går in på en patient
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "slumpad"
	Och jag signerar intyget
    Så ska jag se intyget i databasen

    När jag går in på Statistiktjänsten
    Och jag är inloggad som läkare i Statistiktjänsten
    Och jag går till statistiksidan för diagnoskod "samma som ovan"
    Och jag kollar totala "samma som ovan" diagnoser som finns
    Och jag anropar statitisk-APIet processIntyg
    Och laddar om sidan
    Så ska totala "samma som ovan" diagnoser som finns vara "1" extra

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
    Och jag går in på intyget som tidigare skapats
    Och jag makulerar intyget
    Och jag anropar statitisk-APIet processIntyg

    När jag går in på Statistiktjänsten
    Och jag är inloggad som läkare i Statistiktjänsten
    Och jag går till statistiksidan för diagnoskod "samma som ovan"
    Så ska totala "samma som ovan" diagnoser som finns vara "1" mindre
