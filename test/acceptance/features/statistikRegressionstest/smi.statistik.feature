# language: sv

@REGRESSIONSTEST @REGRESSIONSTESTSTATISTIK @STATISTIK @SMI @NOTREADY
Egenskap: Regressionstest på Statistiktjänsten med ökad test teckning.

Bakgrund: Jag befinner mig på webcerts förstasida

@MAKULERA @VERKSAMHETSSTATISTIK @LISJP
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

@VERKSAMHETSSTATISTIK @LISJP
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

@VERKSAMHETSSTATISTIK
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
    Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
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

@STATISTIKAPIET @FRAGASVAR @LISJP
Scenariomall: Statistik - <beskrivning> <ämne>  ska finnas i statistiktjänsten
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
	
	När jag går in på Statistiktjänsten
	Och jag är inloggad som läkare i Statistiktjänsten
	Och jag går till statistiksidan för "<beskrivning>"
	Och jag kollar värdena i tabellen
	
	Och jag anropar statitisk-APIet processIntyg
	Och jag går till statistiksidan för "<beskrivning>"
	Och jag kollar värdena i tabellen
    Så ska "<ämne>" i "<beskrivning>" vara "1" extra
	
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
  
  #TODO's
  #| Verksamhet meddelanden per ämne Tvärsnitt | getMeddelandenPerAmneTvarsnittVerksamhet | @MeddelandenPerAmneTvarsnittVerksamhet |
  #| Verksamhet meddelanden per ämne | getMeddelandenPerAmneVerksamhet | @MeddelandenPerAmneVerksamhet |
  #| Antal meddelanden per månad | getNumberOfMeddelandenPerMonth | @NumberOfMeddelandenPerMonth |
  #| Verksamhet Antal meddelanden per månad Tvärsnitt | getNumberOfMeddelandenPerMonthTvarsnittVerksamhet | @NumberOfMeddelandenPerMonthTvarsnittVerksamhet |
  #| Verksamhet Antal meddelanden per månad | getNumberOfMeddelandenPerMonthVerksamhet | @NumberOfMeddelandenPerMonthVerksamhet |