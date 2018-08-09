#language: sv
@AVLIDEN
Egenskap: Avliden patient

@PS-01
Scenario: Varning om patient är avliden
    Givet att jag är inloggad som läkare
    När jag går in på en patient som är avliden
    Så ska jag varnas om att "Patienten är avliden"

@GE-003 @PS-01
Scenario: Dödsbevis utkast ska kunna skapas på avliden patient
    Givet jag har raderat alla intyg och utkast för "första" "avliden" testpatienten
	Och att jag är inloggad som djupintegrerad läkare

    När jag går in på en patient som är avliden
	Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsbevis"
	Och jag går in på intyget via djupintegrationslänk
	Så ska jag varnas om att "Patienten är avliden"
	
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

@GE-003 @PS-01
Scenario: Dödsorsaksintyg utkast ska kunna skapas på avliden patient
    Givet jag har raderat alla intyg och utkast för "första" "avliden" testpatienten
	Och att jag är inloggad som djupintegrerad läkare

    När jag går in på en patient som är avliden
	Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsorsaksintyg"
	Och jag går in på intyget via djupintegrationslänk
	Så ska jag varnas om att "Patienten är avliden"
	
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

@FRÅGA-FRÅN-VÅRDEN
Scenario: Avliden Patient - Vården kan ställa frågor på ett intyg
    Givet att jag är inloggad som djupintegrerad läkare
	Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Och jag går in på intyget via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan
	
	Och jag går in på intyget via djupintegrationslänk med parameter "avliden=true"
	Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
	Och Försäkringskassan skickar ett svar
	Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
    Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
	 
@FRÅGA-FRÅN-FK @SMI
Scenario: Avliden Patient - Försäkringskassan kan ställa frågor på ett intyg 
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	Och jag går in på intyget via djupintegrationslänk med parameter "avliden=true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan ställer en "OVRIGT" fråga om intyget
    Och jag svarar på frågan
    Så ska det synas vem som svarat

@SVARA-PA-KOMPLETTERING @SMI
Scenario: Avliden Patient - Vården kan svara på komplettering med ett nytt intyg
    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget

	När jag går in på intyget via djupintegrationslänk med parameter "avliden=true"
    Och jag skickar intyget till Försäkringskassan
    När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
    Och jag går in på intyget via djupintegrationslänk
    Och jag väljer att svara med ett nytt intyg
    Så ska jag se kompletteringsfrågan på utkast-sidan
    Och jag signerar intyget
    Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
