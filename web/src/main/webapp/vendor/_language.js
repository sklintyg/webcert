//-----------------------------------------------------------------------------
// Globals
//-----------------------------------------------------------------------------
var STRING_TABLE = null;
var ERROR_TABLE = null;
//-----------------------------------------------------------------------------
// IsLanguageAvailable
//-----------------------------------------------------------------------------
function IsLanguageAvailable(language) {
    var available = false;
    switch (language) {
        case "English":
        case "Svenska":
        case "Svenska (SAMSET)":
            available = true;
            break;
    }
    return available;
}
//-----------------------------------------------------------------------------
// ReloadStringTable
//-----------------------------------------------------------------------------
function ReloadStringTable(language) {
    STRING_TABLE = LoadStringTable(language);
    ERROR_TABLE = LoadErrorTable(language);
    return;
}
//-----------------------------------------------------------------------------
// LoadStringTable
//-----------------------------------------------------------------------------
function LoadStringTable(language) {
    var table = null;
    var vars = null;
    if (language == null) {
        language = GetCurrentLanguage();
    }
    switch (language) {
        case "English":
            table = LoadStringTableEnglish(); 
            break;
        case "Svenska":
            table = LoadStringTableSwedish(); 
            break;
        case "Svenska (SAMSET)":
            table = LoadStringTableSwedishSamset(); 
            break;
        default:
            table = LoadStringTableEnglish(); 
            break;
    }
    return table;
}
//-----------------------------------------------------------------------------
// LoadStringTableEnglish
//-----------------------------------------------------------------------------
function LoadStringTableEnglish() {
    var table = new Array();

    table["TITLE_ABOUT"] = "About";
    table["TITLE_CERTIFICATE"] = "Certificate";
    table["TITLE_CERTIFICATES"] = "Certificates";
    table["TITLE_SUPPORT"] = "Support";
    table["TITLE_SUPPORT_ADVANCED"] = "Advanced";
    table["TITLE_SUPPORT_SERVICES"] = "Services";
    table["TITLE_LICENSE_AGREEMENT"] = "License";
    table["TITLE_UPDATE"] = "Update";

    table["BUTTON_OK"] = "OK";
    table["BUTTON_CANCEL"] = "Cancel";
    table["BUTTON_YES"] = "Yes";
    table["BUTTON_NO"] = "No";
    table["BUTTON_BACK"] = "Back";
    table["BUTTON_COPY"] = "Copy";
    table["BUTTON_OPEN"] = "Open";
    table["BUTTON_CLOSE"] = "Close";
    table["BUTTON_DELETE"] = "Delete";
    table["BUTTON_REFRESH"] = "Refresh";
    table["BUTTON_SETTINGS"] = "Settings";
    table["BUTTON_ABOUT"] = "About";
    table["BUTTON_RESET"] = "Reset";
    table["BUTTON_EXIT"] = "Exit";
    table["BUTTON_SEARCH"] = "Search";
    table["BUTTON_DOWNLOAD"] = "Download";
    table["BUTTON_UPGRADE"] = "Install";
    table["BUTTON_POSTPONE"] = "Postpone";
 
    table["TEXT_INVALID_LICENSE"] = "License is invalid";
    table["TEXT_ACTIVATE"] = "Activate";
    table["TEXT_SIGNATURE"] = "Signature";
    table["TEXT_AUTHENTICATE"] = "Authenticate";
    table["TEXT_SUBJECT"] = "Subject";
    table["TEXT_ISSUER"] = "Issuer";
    table["TEXT_USAGE"] = "Usage";
    table["TEXT_USAGE_DIGSIG"] = "Digital signature";
    table["TEXT_USAGE_NONREP"] = "Non repudiation";
    table["TEXT_USAGE_KEYENC"] = "Key encipherment";
    table["TEXT_USAGE_DATAENC"] = "Data encipherment";
    table["TEXT_USAGE_KEYEAGREE"] = "Key agreement";
    table["TEXT_USAGE_CERTSIGN"] = "Certificate signing";
    table["TEXT_USAGE_CRLSIGN"] = "CRL signing";
    table["TEXT_VALIDITY"] = "Validity";
    table["TEXT_AUTHKEYID"] = "Authority Key Identifier";
    table["TEXT_THUMBPRINT"] = "Thumbprint";
    table["TEXT_TOKENS"] = "Tokens";
    table["TEXT_SERVICE"] = "Service";
    table["TEXT_SERVICES"] = "Services";
    table["TEXT_ADD_SERVICE"] = "Add service";
    table["TEXT_ACTIVE_SERVICES"] = "Active services";
    table["TEXT_USERS"] = "Users";
    table["TEXT_REQUESTS"] = "Requests";
    table["TEXT_LANGUAGE"] = "Language";
    table["TEXT_ADDRESS"] = "Address";
    table["TEXT_CONTACT"] = "Contact";
    table["TEXT_TRACE"] = "Trace";
    table["TEXT_TRACE_ENABLE"] = "Enable";
    table["TEXT_TRACE_DISABLE"] = "Disable";
    table["TEXT_TRACE_OPEN"] = "Open";
    table["TEXT_SETUP"] = "Setup";
    table["TEXT_SETUP_RESET"] = "Reset user data";
    table["TEXT_SETUP_REPAIR"] = "Repair";
    table["TEXT_SETUP_UNINSTALL"] = "Uninstall";
    table["TEXT_ACTIONS"] = "Actions";
    table["TEXT_CERTIFICATES"] = "Certificates";
    table["TEXT_CHANGE_PIN"] = "Change password";
    table["TEXT_UNLOCK_PIN"] = "Unlock password";
    table["TEXT_RESET_TOKEN"] = "Reset Token";
    table["TEXT_RENEW_TOKEN"] = "Renew Token";
    table["TEXT_DELETE_TOKEN"] = "Delete Token";
    table["TEXT_CERT_DS"] = "Authentication";
    table["TEXT_CERT_NR"] = "Signature";
    table["TEXT_CERT_CA"] = "Issuer";
    table["TEXT_INSERT_TOKEN"] = "Insert a smart card";
    table["TEXT_VIEW_CERTIFICATE"] = "View details";
    table["TEXT_OPEN_CERTIFICATE"] = "Open";
    table["TEXT_DELETE_CERTIFICATE"] = "Delete";
    table["TEXT_USERID"] = "Identifier";
    table["TEXT_LICENSE_AGREEMENT"] = "I accept the license agreement";
    table["TEXT_VERSION"] = "Version";
    table["TEXT_PUK"] = "PUK";
    table["TEXT_PIN"] = "Password";
    table["TEXT_NEWPIN"] = "New password";
    table["TEXT_CONFIRMPIN"] = "Confirm new password";
    table["TEXT_SETTINGS"] = "Settings";
    table["TEXT_SETTINGS_ADVANCED"] = "Advanced settings";
    table["TEXT_OTHER"] = "Other";
    table["TEXT_LICENSE_NOT_ACCEPTED"] = "License agreement not accepted";
    table["TEXT_1_HOUR"] = "1 hour";
    table["TEXT_1_DAY"] = "1 day";
    table["TEXT_1_WEEK"] = "1 week";
    table["TEXT_1_MONTH"] = "1 month";
    table["TEXT_NEVER"] = "Never";
    table["TEXT_CURRENT_VERSION"] = "Current version:";
    table["TEXT_LAST_UPDATE"] = "Last update:";
    table["TEXT_LAST_SEARCH"] = "Last search:";
    table["TEXT_STATUS_SEARCH"] = "Search for update";
    table["TEXT_STATUS_SEARCHING"] = "Searching...";
    table["TEXT_STATUS_DOWNLOAD"] = "Download update (<size>)";
    table["TEXT_STATUS_DOWNLOADING"] = "Downloading <current> of <size>, <percent>%";
    table["TEXT_STATUS_UPGRADE"] = "Install update (<size>)";
    table["TEXT_STATUS_UPGRADING"] = "Installing...";
    
    table["INFO_TOKEN_NOT_PRESENT"] = "Insert a smart card in the smart card reader and try again.";
    table["INFO_BAD_SERVICE_ADDRESS"] = "Enter a valid address to a service provider and try again.";
    table["INFO_SERVICE_ALREADY_PRESENT"] = "The specified address is already present in the list of active services.";
    table["INFO_SERVICE_NOT_RESPONDING"] = "Unable to connect to the service via the specified address. The address is invalid or the service may be temporary offline.";
    table["INFO_REMOVE_SERVICE"] = "You will not be able to use the service any longer and all registered users will be unregistered for the device.";
    table["INFO_TRACE_ENABLE"] = "Enable product trace to file.";
    table["INFO_TRACE_DISABLE"] = "Disable product trace.";
    table["INFO_TRACE_OPEN"] = "Open product trace.";
    table["INFO_SETUP_RESET"] = "Reset all user data, including soft tokens.";
    table["INFO_SETUP_RESET2"] = "All user data will be removed, including possible soft tokens.";
    table["INFO_SETUP_REPAIR"] = "Repair the current product installation.";
    table["INFO_SETUP_REPAIR2"] = "The installation will be repaired.";
    table["INFO_SETUP_UNINSTALL"] = "Uninstall the product.";
    table["INFO_SETUP_UNINSTALL2"] = "After uninstall none of the functions associated with the product may be used.";
    table["INFO_CERTIFICATES"] = "Show a list of all certificates available for the token.";
    table["INFO_CHANGE_PIN"] = "Change password for the token.";
    table["INFO_UNLOCK_PIN"] = "Unlock password for the token.";
    table["INFO_RESET_TOKEN"] = "Remove all objects on the smart card that is updateable by the user.";
    table["INFO_RENEW_TOKEN"] = "Renew the certificates for the token.";
    table["INFO_DELETE_TOKEN"] = "Delete the token including all certificates and private keys.";
    table["INFO_VIEW_CERTIFICATE"] = "View certificate content details.";
    table["INFO_OPEN_CERTIFICATE"] = "Open the certificate in an external viewer.";
    table["INFO_DELETE_CERTIFICATE"] = "Delete the certificate.";
    table["INFO_SETTINGS_ADVANCED"] = "Update advanced settings";
    table["INFO_SERVICES"] = "Administrate connected services.";
    table["INFO_LICENSE_NOT_ACCEPTED"] = "You must accept the license agreement to install the product.";
    table["INFO_INSTALLATION_DONE"] = "The installation was successful.";
    table["INFO_TOKEN_RESET"] = "Token is reset.";
    table["INFO_PIN_CHANGED"] = "Password is changed.";
    table["INFO_PIN_UNLOCKED"] = "Password is unlocked.";
    
    table["ERROR_TOKEN_NOT_PRESENT"] = "Smart card not present.";
    table["ERROR_BAD_SERVICE_ADDRESS"] = "Invalid service address.";
    table["ERROR_SERVICE_ALREADY_PRESENT"] = "Address is already present.";
    table["ERROR_SERVICE_REQUEST_MISSING"] = "Service request missing.";
    table["ERROR_INSTALLATION_FAILED"] = "The installation failed.";

    table["CONFIRM_DELETE_CERTIFICATE"] = "Do you really want to delete the certificate?";
    table["CONFIRM_REMOVE_SERVICE"] = "Do you really want to remove the service?";
    table["CONFIRM_SETUP_RESET"] = "Do you really want to reset the user data?";
    table["CONFIRM_SETUP_REPAIR"] = "Do you really want to repair the product?";
    table["CONFIRM_SETUP_UNINSTALL"] = "Do you really want to uninstall the product?";
    table["CONFIRM_DELETE_TOKEN"] = "Do you really want to remove the soft token?";

    return table;
}
//-----------------------------------------------------------------------------
// LoadStringTableSwedish
//-----------------------------------------------------------------------------
function LoadStringTableSwedish() {
    var table = new Array();
    
    table["TITLE_ABOUT"] = "Om";
    table["TITLE_CERTIFICATE"] = "Certifikat";
    table["TITLE_CERTIFICATES"] = "Certifikat";
    table["TITLE_SUPPORT"] = "Support";
    table["TITLE_SUPPORT_ADVANCED"] = "Avancerat";
    table["TITLE_SUPPORT_SERVICES"] = "Tj&auml;nster";
    table["TITLE_LICENSE_AGREEMENT"] = "Licens";
    table["TITLE_UPDATE"] = "Update";
    
    table["BUTTON_OK"] = "OK";
    table["BUTTON_CANCEL"] = "Avbryt";
    table["BUTTON_YES"] = "Ja";
    table["BUTTON_NO"] = "Nej";
    table["BUTTON_BACK"] = "Tillbaka";
    table["BUTTON_COPY"] = "Kopiera";
    table["BUTTON_OPEN"] = "&Ouml;ppna";
    table["BUTTON_CLOSE"] = "St&auml;ng";
    table["BUTTON_DELETE"] = "Ta bort";
    table["BUTTON_REFRESH"] = "Uppdatera";
    table["BUTTON_SETTINGS"] = "Inst&auml;llningar";
    table["BUTTON_ABOUT"] = "Om";
    table["BUTTON_RESET"] = "&Aring;terst&auml;ll";
    table["BUTTON_EXIT"] = "Avsluta";
    table["BUTTON_SEARCH"] = "S&ouml;k";
    table["BUTTON_DOWNLOAD"] = "H&auml;mta";
    table["BUTTON_UPGRADE"] = "Installera";
    table["BUTTON_POSTPONE"] = "Skjut upp";
 
    table["TEXT_INVALID_LICENSE"] = "Licensen &auml;r ogiltig";
    table["TEXT_ACTIVATE"] = "Aktivera";
    table["TEXT_SIGNATURE"] = "Underskrift";
    table["TEXT_AUTHENTICATE"] = "Identifiera";
    table["TEXT_SUBJECT"] = "Anv&auml;ndare";
    table["TEXT_ISSUER"] = "Utf&auml;rdare";
    table["TEXT_USAGE"] = "Anv&auml;ndningsomr&aring;de";
    table["TEXT_USAGE_DIGSIG"] = "Digital signatur";
    table["TEXT_USAGE_NONREP"] = "Oavvislighet";
    table["TEXT_USAGE_KEYENC"] = "Nyckelchiffrering";
    table["TEXT_USAGE_DATAENC"] = "Datachiffrering";
    table["TEXT_USAGE_KEYEAGREE"] = "Key agreement";
    table["TEXT_USAGE_CERTSIGN"] = "Certifikatsignering";
    table["TEXT_USAGE_CRLSIGN"] = "Signering av lista &ouml;ver &aring;terkallade certifikat";
    table["TEXT_VALIDITY"] = "Giltighetstid";
    table["TEXT_AUTHKEYID"] = "Nyckelidentifierare f&ouml;r utf&auml;rdare";
    table["TEXT_THUMBPRINT"] = "Tumavtryck";
    table["TEXT_TOKENS"] = "Enheter";
    table["TEXT_SERVICE"] = "Tj&auml;nst";
    table["TEXT_SERVICES"] = "Tj&auml;nster";
    table["TEXT_ADD_SERVICE"] = "L&auml;gg till tj&auml;nst";
    table["TEXT_ACTIVE_SERVICES"] = "Aktiva tj&auml;nster";
    table["TEXT_USERS"] = "Anv&auml;ndare";
    table["TEXT_REQUESTS"] = "F&ouml;rfr&aring;gningar";
    table["TEXT_LANGUAGE"] = "Spr&aring;k";
    table["TEXT_ADDRESS"] = "Adress";
    table["TEXT_CONTACT"] = "Kontakt";
    table["TEXT_TRACE"] = "Sp&aring;rning";
    table["TEXT_TRACE_ENABLE"] = "Aktivera";
    table["TEXT_TRACE_DISABLE"] = "Avaktivera";
    table["TEXT_TRACE_OPEN"] = "&Ouml;ppna";
    table["TEXT_SETUP"] = "Installation";
    table["TEXT_SETUP_RESET"] = "Ta bort anv&auml;ndarinfo";
    table["TEXT_SETUP_REPAIR"] = "Reparera";
    table["TEXT_SETUP_UNINSTALL"] = "Avinstallera";
    table["TEXT_ACTIONS"] = "Aktiviteter";
    table["TEXT_CERTIFICATES"] = "Certifikat";
    table["TEXT_CHANGE_PIN"] = "&Auml;ndra s&auml;kerhetskod";
    table["TEXT_UNLOCK_PIN"] = "L&aring;s upp s&auml;kerhetskod";
    table["TEXT_RESET_TOKEN"] = "&Aring;terst&auml;ll enheten";
    table["TEXT_RENEW_TOKEN"] = "F&ouml;rnya enheten";
    table["TEXT_DELETE_TOKEN"] = "Ta bort enheten";
    table["TEXT_CERT_DS"] = "Identifiering";
    table["TEXT_CERT_NR"] = "Underskrift";
    table["TEXT_CERT_CA"] = "Utf&auml;rdare";
    table["TEXT_INSERT_TOKEN"] = "S&auml;tt i ett smartkort";
    table["TEXT_VIEW_CERTIFICATE"] = "Visa detaljer";
    table["TEXT_OPEN_CERTIFICATE"] = "&Ouml;ppna";
    table["TEXT_DELETE_CERTIFICATE"] = "Ta bort";
    table["TEXT_USERID"] = "Identifierare";
    table["TEXT_LICENSE_AGREEMENT"] = "Jag accepterar licensavtalet";
    table["TEXT_VERSION"] = "Version";
    table["TEXT_PUK"] = "PUK";
    table["TEXT_PIN"] = "S&auml;kerhetskod";
    table["TEXT_NEWPIN"] = "Ny s&auml;kerhetskod";
    table["TEXT_CONFIRMPIN"] = "Bekr&auml;fta ny s&auml;kerhetskod";
    table["TEXT_SETTINGS"] = "Inst&auml;llningar";
    table["TEXT_SETTINGS_ADVANCED"] = "Avancerade inst&auml;llningar";
    table["TEXT_OTHER"] = "&Ouml;vrigt";
    table["TEXT_LICENSE_NOT_ACCEPTED"] = "Licensavtalet inte accepterat";
    table["TEXT_1_HOUR"] = "1 timme";
    table["TEXT_1_DAY"] = "1 dag";
    table["TEXT_1_WEEK"] = "1 vecka";
    table["TEXT_1_MONTH"] = "1 m&aring;nad";
    table["TEXT_NEVER"] = "Aldrig";
    table["TEXT_CURRENT_VERSION"] = "Nuvarande version:";
    table["TEXT_LAST_UPDATE"] = "Senaste uppdatering:";
    table["TEXT_LAST_SEARCH"] = "Senaste s&ouml;kning:";
    table["TEXT_STATUS_SEARCH"] = "S&ouml;k efter uppdatering";
    table["TEXT_STATUS_SEARCHING"] = "S&ouml;ker...";
    table["TEXT_STATUS_DOWNLOAD"] = "H&auml;mta uppdatering (<size>)";
    table["TEXT_STATUS_DOWNLOADING"] = "H&auml;mtar <current> av <size>, <percent>%";
    table["TEXT_STATUS_UPGRADE"] = "Installera uppdatering (<size>)";
    table["TEXT_STATUS_UPGRADING"] = "Installerar...";

    table["INFO_TOKEN_NOT_PRESENT"] = "S&auml;tt i ett kort i kortl&auml;saren och f&ouml;rs&ouml;k igen.";
    table["INFO_BAD_SERVICE_ADDRESS"] = "Ange en korrekt adress f&ouml;r en tj&auml;nsteleverant&ouml;r och f&ouml;rs&ouml;k igen.";
    table["INFO_SERVICE_ALREADY_PRESENT"] = "Den angivna adressen finns redan med i listan &ouml;ver anslutna tj&auml;nster.";
    table["INFO_SERVICE_NOT_RESPONDING"] = "Kan inte ansluta till tj&auml;nsten p&aring; angiven adress. Adressen &auml;r ogiltig eller tj&auml;nsten kan vara tillf&auml;lligt otillg&auml;nglig.";
    table["INFO_REMOVE_SERVICE"] = "Tj&auml;nsten kommer inte kunna anv&auml;ndas l&auml;ngre och alla registrerade anv&auml;ndare kommer avregistreras fr&aring;n enheten.";
    table["INFO_TRACE_ENABLE"] = "Aktivera sp&aring;rning till fil f&ouml;r fels&ouml;kning.";
    table["INFO_TRACE_DISABLE"] = "Avaktivera sp&aring;rning till fil.";
    table["INFO_TRACE_OPEN"] = "&Ouml;ppna sp&aring;rningsfil f&ouml;r fels&ouml;kning.";
    table["INFO_SETUP_RESET"] = "Ta bort all anv&auml;ndarinformation, inklusive filbaserade enheter.";
    table["INFO_SETUP_RESET2"] = "All anv&auml;ndarinformation kommer tas bort, inklusive eventuella filbaserade enheter.";
    table["INFO_SETUP_REPAIR"] = "Reparera nuvarande installation av produkten.";
    table["INFO_SETUP_REPAIR2"] = "Installationen kommer repareras.";
    table["INFO_SETUP_UNINSTALL"] = "Avinstallera produkten.";
    table["INFO_SETUP_UNINSTALL2"] = "Efter avinstallation kommer funktioner knutna till produkten inte kunna anv&auml;ndas.";
    table["INFO_CERTIFICATES"] = "Visa en lista &ouml;ver tillg&auml;ngliga certifikat knutna till enheten.";
    table["INFO_CHANGE_PIN"] = "&Auml;ndra s&auml;kerhetskod f&ouml;r enheten.";
    table["INFO_UNLOCK_PIN"] = "L&aring;s upp s&auml;kerhetskod f&ouml;r enheten.";
    table["INFO_RESET_TOKEN"] = "Ta bort alla objekt fr&aring;n enheten som anv&auml;ndaren har r&auml;tt att uppdatera.";
    table["INFO_RENEW_TOKEN"] = "F&ouml;rnya certifikat f&ouml;r enheten.";
    table["INFO_DELETE_TOKEN"] = "Ta bort enheten inklusive alla certifikat och privat nycklar.";
    table["INFO_VIEW_CERTIFICATE"] = "Visa detaljerad information om certifikatets inneh&aring;ll.";
    table["INFO_OPEN_CERTIFICATE"] = "&Ouml;ppna certifikatet i en extern visare.";
    table["INFO_DELETE_CERTIFICATE"] = "Ta bort certifikatet.";
    table["INFO_SETTINGS_ADVANCED"] = "Uppdatera avancerade inst&auml;llningar";
    table["INFO_SERVICES"] = "Hantera anslutna tj&auml;nster.";
    table["INFO_LICENSE_NOT_ACCEPTED"] = "Du m&aring;ste acceptera licensvillkoren f&ouml;r att installera produkten.";
    table["INFO_INSTALLATION_DONE"] = "Installationen lyckades.";
    table["INFO_TOKEN_RESET"] = "Enheten &auml;r &aring;terst&auml;lld.";
    table["INFO_PIN_CHANGED"] = "S&auml;kerhetskod &auml;r &auml;ndrad.";
    table["INFO_PIN_UNLOCKED"] = "S&auml;kerhetskod &auml;r uppl&aring;st.";
    
    table["ERROR_TOKEN_NOT_PRESENT"] = "Kort saknas i kortl&auml;saren.";
    table["ERROR_BAD_SERVICE_ADDRESS"] = "Tj&auml;nsteleverant&ouml;rens address &auml;r ogiltig.";
    table["ERROR_SERVICE_ALREADY_PRESENT"] = "Adressen finns redan.";
    table["ERROR_SERVICE_REQUEST_MISSING"] = "Tj&auml;nstens beg&auml;ran saknas.";
    table["ERROR_INSTALLATION_FAILED"] = "Installationen misslyckades.";

    table["CONFIRM_DELETE_CERTIFICATE"] = "Vill du verkligen ta bort certifikatet?";
    table["CONFIRM_REMOVE_SERVICE"] = "Vill du verkligen ta bort tj&auml;nsten?";
    table["CONFIRM_SETUP_RESET"] = "Vill du verkligen ta bort all anv&auml;ndarinformation?";
    table["CONFIRM_SETUP_REPAIR"] = "Vill du verkligen reparera produkten?";
    table["CONFIRM_SETUP_UNINSTALL"] = "Vill du verkligen avinstallera produkten?";
    table["CONFIRM_DELETE_TOKEN"] = "Vill du verkligen ta bort den filbaserade enheten?";

    return table;
}
//-----------------------------------------------------------------------------
// LoadStringTableSwedishSamset
//-----------------------------------------------------------------------------
function LoadStringTableSwedishSamset() {
    var table = null;
    if ((table = LoadStringTableSwedish()) != null) {
    
        table["TITLE_CERTIFICATES"] = "E-legitimationer";
        table["TITLE_CERTIFICATE"] = "E-legitimation";

        table["TEXT_CHANGE_PIN"] = "&Auml;ndra s&auml;kerhetskod";
        table["TEXT_UNLOCK_PIN"] = "L&aring;s upp s&auml;kerhetskod";
        table["TEXT_SIGNATURE"] = "Underteckna";
        table["TEXT_AUTHENTICATE"] = "Legitimera";
        table["TEXT_CERTIFICATES"] = "E-legitimationer";
        table["TEXT_CHANGE_PIN"] = "&Auml;ndra s&auml;kerhetskod";
        table["TEXT_UNLOCK_PIN"] = "L&aring;s upp s&auml;kerhetskod";
        table["TEXT_CERT_DS"] = "Legitimering";
        table["TEXT_CERT_NR"] = "Undertecknande";
        table["TEXT_PUK"] = "Uppl&aring;sningskod";
        table["TEXT_PIN"] = "S&auml;kerhetskod";
        table["TEXT_NEWPIN"] = "Ny s&auml;kerhetskod";
        table["TEXT_CONFIRMPIN"] = "Bekr&auml;fta ny s&auml;kerhetskod";

        table["INFO_CERTIFICATES"] = "Visa en lista &ouml;ver tillg&auml;ngliga e-legitimationer f&ouml;r enheten.";
        table["INFO_CHANGE_PIN"] = "&Auml;ndra s&auml;kerhetskod f&ouml;r enheten.";
        table["INFO_UNLOCK_PIN"] = "L&aring;s upp s&auml;kerhetskod f&ouml;r enheten.";
        table["INFO_RENEW_TOKEN"] = "F&ouml;rnya din e-legitimation.";
        table["INFO_DELETE_TOKEN"] = "Ta bort din e-legitimation.";
        table["INFO_VIEW_CERTIFICATE"] = "Visa detaljerad information om e-legitimationens inneh&aring;ll.";
        table["INFO_OPEN_CERTIFICATE"] = "&Ouml;ppna e-legitimationen i en extern visare.";
        table["INFO_DELETE_CERTIFICATE"] = "Ta bort e-legitimationen.";
        table["INFO_PIN_CHANGED"] = "S&auml;kerhetskoden &auml;r &auml;ndrad.";
        table["INFO_PIN_UNLOCKED"] = "S&auml;kerhetskoden &auml;r uppl&aring;st.";
        
        table["CONFIRM_DELETE_CERTIFICATE"] = "Vill du verkligen ta bort din e-legitimation?";
        table["CONFIRM_DELETE_TOKEN"] = "Vill du verkligen ta bort din filbaserade e-legitimation?";
    }
    return table;
}
//-----------------------------------------------------------------------------
// GetString
//-----------------------------------------------------------------------------
function GetString(key) {
    var value = "";
    if (STRING_TABLE == null) {
        STRING_TABLE = LoadStringTable(null);
    }
    if ((value = STRING_TABLE[key]) == null) {
        value = key;
    }
    return value;
}
//-----------------------------------------------------------------------------
// LoadErrorTable
//-----------------------------------------------------------------------------
function LoadErrorTable(language) {
    var table = null;
    if (language == null) {
        language = GetCurrentLanguage();
    }
    switch (language) {
        case "English":
            table = LoadErrorTableEnglish(); 
            break;
        case "Svenska":
            table = LoadErrorTableSwedish(); 
            break;
        case "Svenska (SAMSET)":
            table = LoadErrorTableSwedishSamset(); 
            break;
        default:
            table = LoadErrorTableEnglish(); 
            break;
    }
    return table;
}
//-----------------------------------------------------------------------------
// LoadErrorTableEnglish
//-----------------------------------------------------------------------------
function LoadErrorTableEnglish() {
    var table = new Array();
    table["CKR_CANCEL"] = "Function was aborted";
    table["CKR_FUNCTION_FAILED"] = "Function failed";
    table["CKR_INVALID_LICENSE"] = "License is invalid|The action is not allowed with the current product license.";
    table["CKR_PIN_INCORRECT"] = "Password is incorrect";
    table["CKR_PIN_INVALID"] = "Password is invalid|The entered password does not fulfill the current password policy.";
    table["CKR_PIN_LEN_RANGE"] = "Invalid password length|The entered password does not fulfill the current password policy.";
    table["CKR_PIN_LOCKED"] = "Password is locked";
    table["CKR_PINS_NOT_EQUAL"] = "Entered passwords do not correspond|Enter the same password in both fields for new password.";
    table["CKR_TOKEN_NOT_PRESENT"] = "Smart card not present|Insert a smart card in the smart card reader and try again.";
    return table;
}
//-----------------------------------------------------------------------------
// LoadErrorTableSwedish
//-----------------------------------------------------------------------------
function LoadErrorTableSwedish() {
    var table = new Array();
    table["CKR_CANCEL"] = "Funktionen avbr&ouml;ts";
    table["CKR_FUNCTION_FAILED"] = "Funktionen misslyckades";
    table["CKR_INVALID_LICENSE"] = "Licensen &auml;r ogiltig|&Aringtg&auml;rden &auml;r inte till&aring;ten med nuvarande produktlicens.";
    table["CKR_PIN_INCORRECT"] = "S&auml;kerhetskoden &auml;r felaktig";
    table["CKR_PIN_INVALID"] = "S&auml;kerhetskoden &auml;r ogiltig|Angiven s&auml;kerhetskoden uppfyller inte nuvarande kodpolicy.";
    table["CKR_PIN_LEN_RANGE"] = "Ogiltig s&auml;kerhetskodsl&auml;ngd|Angiven s&auml;kerhetskod uppfyller inte nuvarande kodpolicy.";
    table["CKR_PIN_LOCKED"] = "S&auml;kerhetskoden &auml;r l&aring;st";
    table["CKR_PINS_NOT_EQUAL"] = "Angivna s&auml;kerhetskod &auml;r inte lika|Ange samma s&auml;kerhetskod i b&aring;da f&auml;lten f&ouml;r ny s&auml;kerhetskod.";
    table["CKR_TOKEN_NOT_PRESENT"] = "Kort saknas i kortl&auml;saren|S&auml;tt i ett kort i kortl&auml;saren och f&ouml;rs&ouml;k igen.";
    return table;
}
//-----------------------------------------------------------------------------
// LoadErrorTableSwedishSamset
//-----------------------------------------------------------------------------
function LoadErrorTableSwedishSamset() {
    var table = new Array();
    table["CKR_CANCEL"] = "Funktionen avbr&ouml;ts";
    table["CKR_FUNCTION_FAILED"] = "Funktionen misslyckades";
    table["CKR_INVALID_LICENSE"] = "Licensen &auml;r ogiltig|&Aringtg&auml;rden &auml;r inte till&aring;ten med nuvarande produktlicens.";
    table["CKR_PIN_INCORRECT"] = "S&auml;kerhetskoden &auml;r felaktig";
    table["CKR_PIN_INVALID"] = "S&auml;kerhetskoden &auml;r ogiltig|Angiven s&auml;kerhetskod uppfyller inte nuvarande kodpolicy.";
    table["CKR_PIN_LEN_RANGE"] = "Ogiltig l&auml;ngd p&aring; s&auml;kerhetskod|Angiven s&auml;kerhetskod uppfyller inte nuvarande kodpolicy.";
    table["CKR_PIN_LOCKED"] = "S&auml;kerhetskoden &auml;r l&aring;st";
    table["CKR_PINS_NOT_EQUAL"] = "Angivna s&auml;kerhetskoder &auml;r inte lika|Ange samma s&auml;kerhetskod i b&aring;da f&auml;lten f&ouml;r ny kod.";
    table["CKR_TOKEN_NOT_PRESENT"] = "Kort saknas i kortl&auml;saren|S&auml;tt i ett kort i kortl&auml;saren och f&ouml;rs&ouml;k igen.";
    return table;
}
//-----------------------------------------------------------------------------
// GetErrorString
//-----------------------------------------------------------------------------
function GetErrorString(key) {
    var value = null;
    if (ERROR_TABLE == null) {
        ERROR_TABLE = LoadErrorTable(null);
    }
    if (ERROR_TABLE != null) {
        if ((value = ERROR_TABLE[key]) == null) {
            value = key;
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// PIN strings
//-----------------------------------------------------------------------------
var PIN = {
    text: function(language, index) {
        var value = "";
        switch (language) {
            case "de":
                switch (index) {
                    case 0: value = "PIN"; break;
                    case 1: value = "Neue PIN"; break;
                    case 2: value = "Best&auml;tigung der neuen PIN"; break;
                }
                break;
            case "dk":
                switch (index) {
                    case 0: value = "Pinkode"; break;
                    case 1: value = "Ny pinkode"; break;
                    case 2: value = "Bekr&aelig;ftelse af ny pinkode"; break;
                }
                break;
            case "en":
                switch (index) {
                    case 0: value = "Password"; break;
                    case 1: value = "New password"; break;
                    case 2: value = "New password confirmation"; break;
                }
                break;
            case "fi":
                switch (index) {
                    case 0: value = "PIN-tunnus"; break;
                    case 1: value = "Uusi PIN-tunnus"; break;
                    case 2: value = "Uuden PIN-tunnuksen vahvistus"; break;
                }
                break;
            case "fr":
                switch (index) {
                    case 0: value = "Code confidentiel"; break;
                    case 1: value = "Nouveau code confidentiel"; break;
                    case 2: value = "Confirmation du nouveau code confidentiel"; break;
                }
                break;
            case "no":
                switch (index) {
                    case 0: value = "PIN-kode"; break;
                    case 1: value = "Ny PIN-kode"; break;
                    case 2: value = "Bekreftelse av ny PIN-kode"; break;
                }
                break;
            case "pl":
                switch (index) {
                    case 0: value = "Numer PIN"; break;
                    case 1: value = "Nowy numer PIN"; break;
                    case 2: value = "Potwierdzenie nowego numeru PIN"; break;
                }
                break;
            case "pt":
                switch (index) {
                    case 0: value = "PIN"; break;
                    case 1: value = "Novo PIN"; break;
                    case 2: value = "Confirma&ccedil;&atilde;o de novo PIN"; break;
                }
                break;
            case "se":
                switch (index) {
                    case 0: value = "S&auml;kerhetskod"; break;
                    case 1: value = "Ny s&auml;kerhetskod"; break;
                    case 2: value = "Bekr&auml;fta ny s&auml;kerhetskod"; break;
                }
                break;
            case "tr":
                switch (index) {
                    case 0: value = "PIN"; break;
                    case 1: value = "Yeni PIN"; break;
                    case 2: value = "Yeni PIN onay"; break;
                }
                break;
        }
        return value;
    },
    label: function(language) {
        return PIN.text(language, 0);
    },
    newLabel: function(language) {
        return PIN.text(language, 1);
    },
    confirmLabel: function(language) {
        return PIN.text(language, 2);
    }
}
