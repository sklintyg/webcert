# language: sv

@Regressionstest @RegressionsTestStatistik @Statistik @fk7263
Egenskap: Regressionstest på Statistiktjänsten med ökad test teckning.

Bakgrund: Jag befinner mig på webcerts förstasida

@nätverk @smoke @statitisk-APIet @processIntyg
Scenario: Testa att anropa statitisk-APIet
    När jag anropar statitisk-APIet processIntyg

@makulera @verksamhetsStatistik
Scenario: Ett nyskapat och makulerat FK7263 intyg ska finnas och senare tas bort från Statistiktjänstens GUI
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
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

@verksamhetsStatistik
Scenario: Två nyskapade FK7263 intyg på samma person ska räknas som en i Statistiktjänstens GUI
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "slumpad"
	Och jag signerar intyget
    Så ska jag se intyget i databasen
	
	När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på patienten
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "samma som ovan"
	Och jag signerar intyget
    Så ska jag se intyget i databasen

    När jag går in på Statistiktjänsten
    Och jag är inloggad som läkare i Statistiktjänsten
    Och jag går till statistiksidan för diagnoskod "samma som ovan"
    Och jag kollar totala "samma som ovan" diagnoser som finns
    Och jag anropar statitisk-APIet processIntyg
    Och laddar om sidan
    Så ska totala "samma som ovan" diagnoser som finns vara "1" extra
	
@verksamhetsStatistik
Scenario: Två nyskapade FK7263 intyg på olika personer ska räknas som två i Statistiktjänstens GUI
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "slumpad"
	Och jag signerar intyget
    Så ska jag se intyget i databasen
	
	När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en annan patient
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "samma som ovan"
	Och jag signerar intyget
    Så ska jag se intyget i databasen

    När jag går in på Statistiktjänsten
    Och jag är inloggad som läkare i Statistiktjänsten
    Och jag går till statistiksidan för diagnoskod "samma som ovan"
    Och jag kollar totala "samma som ovan" diagnoser som finns
    Och jag anropar statitisk-APIet processIntyg
    Och laddar om sidan
    Så ska totala "samma som ovan" diagnoser som finns vara "2" extra
