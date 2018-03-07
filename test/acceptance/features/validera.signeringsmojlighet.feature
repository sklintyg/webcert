# language: sv
@inkomplett
Egenskap: Validera signeringsmöjlighet

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@smi
Scenario: Det ska inte gå att signera ofullständigt SMI Intyg   
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg 
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag raderar ett slumpat obligatoriskt fält
	Och jag klickar på signera-knappen
	Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
	Och ska jag se en lista med vad som saknas

@saknasFaltUtkast
Scenario: Saknasfält ska visas först när man trycker på signera i utkastet   
	När jag går in på att skapa ett slumpat intyg
	Och jag klickar på signera-knappen
	Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
	
	När jag går till ej signerade utkast
	Och jag trycker på visa intyget
	Så ska jag inte se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"