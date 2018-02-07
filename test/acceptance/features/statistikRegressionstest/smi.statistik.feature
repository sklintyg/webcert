# language: sv

@Regressionstest @RegressionsTestStatistik @Statistik @smi @notReady
Egenskap: Regressionstest på Statistiktjänsten med ökad test teckning.

Bakgrund: Jag befinner mig på webcerts förstasida

@makulera @verksamhetsStatistik @lisjp
Scenario: Ett nyskapat och makulerat FK7263 intyg ska finnas och senare tas bort från Statistiktjänsten
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
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

@verksamhetsStatistik @lisjp
Scenario: Två nyskapade LISJP intyg på samma person ska räknas som en i Statistiktjänstens GUI
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag ändrar diagnoskoden till "slumpad"
	Och jag signerar intyget
    Så ska jag se intyget i databasen
	
	När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på patienten
	Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
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
Scenario: Två nyskapade LISJP intyg på olika personer ska räknas som två i Statistiktjänstens GUI
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
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

@statistikApiet @fragasvar @lisjp @workingOnThis
Scenariomall: <beskrivning> statistik ska finnas i statistiktjänsten
    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107Q"
	Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
    Så ska jag se intyget i databasen
	
	När jag skickar intyget till Försäkringskassan
	Och Försäkringskassan ställer en "<ämne>" fråga om intyget
	Och jag svarar på frågan
	Så ska det synas vem som svarat
	Och jag går in på en patient
	
	När jag går in på Statistiktjänsten
	Och jag är inloggad som läkare i Statistiktjänsten
	Och jag hämtar "<beskrivning>" från Statistik APIet - <APIrequest>
	
	Och jag anropar statitisk-APIet processIntyg
	Och jag hämtar "<beskrivning>" från Statistik APIet - <APIrequest>
    Så ska "<ämne>" i "<beskrivning>" vara en extra
	
@MeddelandenPerAmne
Exempel:
  | beskrivning | APIrequest | ämne |
  | Meddelande per ämne | getMeddelandenPerAmne | AVSTMN |
  | Meddelande per ämne | getMeddelandenPerAmne | OVRIGT |

@MeddelandenPerAmneLandsting
Exempel:
  | beskrivning | APIrequest | ämne |
  | Meddelanden per ämne och landsting | getMeddelandenPerAmneLandsting | AVSTMN |
  | Meddelanden per ämne och landsting | getMeddelandenPerAmneLandsting | OVRIGT |

@MeddelandenPerAmneOchEnhetLandsting
Exempel:
  | beskrivning | APIrequest | ämne |
  | Meddelanden per ämne, enhet och landsting | getMeddelandenPerAmneOchEnhetLandsting | AVSTMN |
  | Meddelanden per ämne, enhet och landsting | getMeddelandenPerAmneOchEnhetLandsting | OVRIGT |

@MeddelandenPerAmneOchEnhetTvarsnittVerksamhet
Exempel:
  | beskrivning | APIrequest | ämne |
  | Verksamhet meddelanden per ämne och Enhet Tvärsnitt | getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet | AVSTMN |
  | Verksamhet meddelanden per ämne och Enhet Tvärsnitt | getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet | OVRIGT |
  
@MeddelandenPerAmneOchEnhetVerksamhet  
Exempel:
  | beskrivning | APIrequest | ämne |
  | Verksamhet meddelanden per ämne och Enhet | getMeddelandenPerAmneOchEnhetVerksamhet | AVSTMN |
  | Verksamhet meddelanden per ämne och Enhet | getMeddelandenPerAmneOchEnhetVerksamhet | OVRIGT |  
  
  #Todo's
  #| Verksamhet meddelanden per ämne Tvärsnitt | getMeddelandenPerAmneTvarsnittVerksamhet | @MeddelandenPerAmneTvarsnittVerksamhet |
  #| Verksamhet meddelanden per ämne | getMeddelandenPerAmneVerksamhet | @MeddelandenPerAmneVerksamhet |
  #| Antal meddelanden per månad | getNumberOfMeddelandenPerMonth | @NumberOfMeddelandenPerMonth |
  #| Verksamhet Antal meddelanden per månad Tvärsnitt | getNumberOfMeddelandenPerMonthTvarsnittVerksamhet | @NumberOfMeddelandenPerMonthTvarsnittVerksamhet |
  #| Verksamhet Antal meddelanden per månad | getNumberOfMeddelandenPerMonthVerksamhet | @NumberOfMeddelandenPerMonthVerksamhet |