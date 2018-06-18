//-----------------------------------------------------------------------------
// Globals
//-----------------------------------------------------------------------------
var GUI_VERSION = "08x";
// Default configuration
var DEFAULT_SERVICE_ADDRESS = "";
// The following images are stored locally (images/srv_<id>.png), but
// correspond to a server image (avoid download)
var IMAGE_ID = "fk;inera;skv;sm;telia;service";
// The following images are stored locally (images/tile_<name>.png)
var IMAGE_NAME = null;
// The plugin info object is used for delayed start of plugin call
var PLUGIN_INFO = null;
// Device type variable contain type of system device
var DEVICE_TYPE = null;
//-----------------------------------------------------------------------------
// LoadDynamicImages
//-----------------------------------------------------------------------------
function LoadDynamicImages() {
    if (IMAGE_NAME == null) {
        // The following images are stored locally (images/tile_<name>.png)
        AddDynamicImage("SecMaker", "sm");
        AddDynamicImage("Telia", "telia");
        AddDynamicImage("Inera", "inera");
        AddDynamicImage("Carelink", "inera");
        AddDynamicImage("Region Sk&aring;ne", "skane");
        AddDynamicImage("Stockholms L&auml;ns Landsting", "sll");
        AddDynamicImage("Region Halland", "rh");
        AddDynamicImage("Landstinget i J&ouml;nk&ouml;ping", "ljl");
        AddDynamicImage("Landstinget Kronoberg", "ltk");
        AddDynamicImage("J&auml;mtlands l&auml;ns landsting", "jll");
        AddDynamicImage("Vaestorekisterikeskus", "vrk");
        AddDynamicImage("Tullverket 202100-0969", "tullen");
        AddDynamicImage("Rikspolisstyrelsen", "polisen");
        AddDynamicImage("Skatteverket", "skv");
        AddDynamicImage("F&ouml;rs&auml;kringskassan", "fk");
        AddDynamicImage("Swedish Government", "sw");
    }
    return;
}
//-----------------------------------------------------------------------------
// AddDynamicImage
//-----------------------------------------------------------------------------
function AddDynamicImage(name, image) {
    if (IMAGE_NAME == null) {
        IMAGE_NAME = "";
    }
    else {
        IMAGE_NAME += "|";
    }
    IMAGE_NAME += name + "," + image;
    return;
}
//-----------------------------------------------------------------------------
// GetVariables
//-----------------------------------------------------------------------------
function GetVariables(name) {
    var vars = null;
    var store = new DataStore();
    // Should we get more url parameters?
    if ((window.top.location.href.indexOf('?') == -1) ||
        ((value = store.getValue("href")) == null) ||
        (value.length == 0)) {
        value = window.top.location.href;
    }
    else {
        value += window.top.location.href.substr(top.window.location.href.indexOf('?') + 1);
    }
    store.setValue("href", null);
    if ((value.length > 512) && (value.substr(value.length - 3) == "...")) {
        // Need more data
        store.setValue("href", value.substr(0, value.length - 3));
        document.form1.action = "iid://more";
        document.form1.submit();
    }
    else {
        vars = GetVariablesHREF(value);
    }
    return vars;
}
//-----------------------------------------------------------------------------
// GetVariablesHREF
//-----------------------------------------------------------------------------
function GetVariablesHREF(href, vars) {
    var store = null;
    var value = null;
    var hash = null;
    var hashes = null;
    var i = 0;
    // Create new array if missing
    if (vars == null) {
        vars = new Array();
    }
    // Take top window if not specified
    if (href == null) {
        href = window.top.location.href;
    }
    // Only keep variables part
    href = href.substr(href.indexOf('?') + 1);
    // Separate to components
    hashes = href.split('&');
    for (i = 0; i < hashes.length; i++) {
        if (hashes[i].indexOf('=') != -1) {
            hash = hashes[i].split('=');
            if ((hash != null) && (hash[0] != null) && (hash[1] != null)) {
                vars.push(URL.decode(hash[0]));
                vars[URL.decode(hash[0])] = URL.decode(hash[1]);
            }
        }
    }
    return vars;
}
//-----------------------------------------------------------------------------
// GetVersionGUI
//-----------------------------------------------------------------------------
function GetVersionGUI() {
    var store = new DataStore();
    var type = "";
    if (store.useInternal())
        type = "i";
    else if (store.useLocalStorage())
        type = "l";
    else
        type = "c";
    return GUI_VERSION + "-" + type;
}
//-----------------------------------------------------------------------------
// IsSystem
//-----------------------------------------------------------------------------
function IsSystem(value) {
    var info = null;
    if (DEVICE_TYPE == null) {
        info = new DeviceInfo(null);
        if ((info.device != null) && (info.device.name != null)) {
            DEVICE_TYPE = info.device.name;
        }
    }
    if (DEVICE_TYPE == null) {
        DEVICE_TYPE = navigator.platform;
    }
    return (DEVICE_TYPE.indexOf(value) != -1);
}
//-----------------------------------------------------------------------------
// IsOpenCertificateAvailable
//-----------------------------------------------------------------------------
function IsOpenCertificateAvailable() {
    var available = false;
    if (IsSystem("Win")) {
        available = true;
    }
    return available;
}
//-----------------------------------------------------------------------------
// SetCustomFlags
//-----------------------------------------------------------------------------
function SetCustomFlags(flags) {
    var store = new DataStore();
    store.setValue("custom", flags);
    return;
}
//-----------------------------------------------------------------------------
// GetCustomFlagActive
//-----------------------------------------------------------------------------
function GetCustomFlagActive(position) {
    var active = false;
    var store = new DataStore();
    var flags = 0;
    if ((flags = store.getValue("custom")) != null) {
        if ((ConvertNumber(flags) & position) != 0) {
            active = true;
        }
    }
    return active;
}
//-----------------------------------------------------------------------------
// GetCustomFlagUseTitleImage
//-----------------------------------------------------------------------------
function GetCustomFlagUseTitleImage() {
    return GetCustomFlagActive(0x01);
}
//-----------------------------------------------------------------------------
// GetCustomFlagUseTextButtons
//-----------------------------------------------------------------------------
function GetCustomFlagUseTextButtons() {
    return GetCustomFlagActive(0x02);
}
//-----------------------------------------------------------------------------
// GetCustomFlagUseCustomerImage
//-----------------------------------------------------------------------------
function GetCustomFlagUseCustomerImage() {
    return GetCustomFlagActive(0x04);
}
//-----------------------------------------------------------------------------
// GetCustomFlagNoCertificateAction
//-----------------------------------------------------------------------------
function GetCustomFlagNoCertificateAction() {
    return GetCustomFlagActive(0x08);
}
//-----------------------------------------------------------------------------
// GetCustomFlagUseDynamicImage
//-----------------------------------------------------------------------------
function GetCustomFlagUseDynamicImage() {
    return GetCustomFlagActive(0x10);
}
//-----------------------------------------------------------------------------
// GetCustomFlagSimpleRefresh
//-----------------------------------------------------------------------------
function GetCustomFlagSimpleRefresh() {
    return GetCustomFlagActive(0x20);
}
//-----------------------------------------------------------------------------
// GetCustomFlagUseEditDigits
//-----------------------------------------------------------------------------
function GetCustomFlagUseEditDigits() {
    return GetCustomFlagActive(0x40);
}
//-----------------------------------------------------------------------------
// GetCustomFlagUpdateService
//-----------------------------------------------------------------------------
function GetCustomFlagUpdateService() {
    var update = false;
    if (GetCustomFlagActive(0x80)) {
        update = true;
    }
    else {
        update = NeedServiceUpdate();
    }
    return update;
}
//-----------------------------------------------------------------------------
// GetCustomFlagNoAsync
//-----------------------------------------------------------------------------
function GetCustomFlagNoAsync() {
    return GetCustomFlagActive(0x0100);
}
//-----------------------------------------------------------------------------
// NeedServiceUpdate
//-----------------------------------------------------------------------------
function NeedServiceUpdate() {
    var update = false;
    var i = 0;
    var value = null;
    var value2 = null;
    // Check that we have all values
    if ((value = GetServiceAddressList()) != null) {
        while ((value2 = GetPartBy(value, i, ';')) != "") {
            if (GetPartBy(value2, 1, ',') == "") {
                update = true;
                break;
            }
            i++;
        }
    }
    return update;
}
//-----------------------------------------------------------------------------
// ShouldAutoFocusEditField
//-----------------------------------------------------------------------------
function ShouldAutoFocusEditField() {
    var should = false;
    // Actually not available for touch screen
    if (IsSystem("Win") ||
        IsSystem("Linux") ||
        IsSystem("Mac")) {
        should = true;
    }
    return should;
}
//-----------------------------------------------------------------------------
// ShouldUseDigitsEditField
//-----------------------------------------------------------------------------
function ShouldUseDigitsEditField() {
    var type = null;
    // Part of custom flags
    if (GetCustomFlagUseEditDigits()) {
        // Only available for iOS
        if (IsSystem("iOS") ||
            IsSystem("iPad") ||
            IsSystem("iPhone")) {
            type = "pattern=\\d*";
        }
    }
    return type;
}
//-----------------------------------------------------------------------------
// UpdatePasswordFields
//-----------------------------------------------------------------------------
function UpdatePasswordFields() {
    var type = null;
    if ((type = ShouldUseDigitsEditField()) != null) {
        SetEditFieldDigits("pwd", type);
        SetEditFieldDigits("pwd_old", type);
        SetEditFieldDigits("pwd_new", type);
        SetEditFieldDigits("pwd_confirm", type);
    }
    return;
}
//-----------------------------------------------------------------------------
// HasPluginObject
//-----------------------------------------------------------------------------
function HasPluginObject() {
    var available = false;
    // Available on some platforms.
    if (iid_GetObject() != null) {
        available = true;
    }
    return available;
}
//-----------------------------------------------------------------------------
// HasPluginApp
//-----------------------------------------------------------------------------
function HasPluginApp() {
    var available = true;
    // Internal plugin App may modify user agent string to include "Net iD"
    if (navigator.userAgent.indexOf("Net iD") == -1) {
        if (IsSystem("Win")) {
            available = false;
        }
        else if (IsSystem("Mac")) {
            if (navigator.userAgent.indexOf("Safari") != -1) {
                available = false;
            }
        }
        else if (IsSystem("iOS") ||
            IsSystem("iPad") ||
            IsSystem("iPhone")) {
            available = true;
        }
        else if (IsSystem("Android")) {
            available = true;
        }
        else {
            available = false;
        }
    }
    return available;
}
//-----------------------------------------------------------------------------
// PluginInfo
//-----------------------------------------------------------------------------
function PluginInfo(next, action, param_set, param_invoke, param_enum, param_get, callback) {
    this.next = next;
    this.action = action;
    this.param_set = param_set;
    this.param_invoke = param_invoke;
    this.param_enum = param_enum;
    this.param_get = param_get;
    this.callback = callback;
    this.status = 0;
    this.result = 0;
}
//-----------------------------------------------------------------------------
// DelayedCallPluginObject
//-----------------------------------------------------------------------------
function DelayedCallPluginObject() {
    if (PLUGIN_INFO != null) {
        CallPluginObject(PLUGIN_INFO);
    }
    return;
}
//-----------------------------------------------------------------------------
// DelayedPluginResponse
//-----------------------------------------------------------------------------
function DelayedPluginResponse() {
    var rv = 0;
    if (PLUGIN_INFO != null) {
        rv = iid_GetProperty("InvokeResult");
        HandlePluginResponse(PLUGIN_INFO, rv);
    }
    return;
}
//-----------------------------------------------------------------------------
// HandlePluginResponse
//-----------------------------------------------------------------------------
function HandlePluginResponse(pluginInfo, rv) {
    // Continue to next part unless rv = 144 (CKR_OPERATION_ACTIVE)
    if (rv != 144) {
        pluginInfo.result = rv;
        pluginInfo.status = 1;
        CallPluginObject(pluginInfo);
    }
    else {
        PLUGIN_INFO = pluginInfo;
        setTimeout(DelayedPluginResponse, 999);
    }
    return;
}
//-----------------------------------------------------------------------------
// CallPluginObject
//-----------------------------------------------------------------------------
function CallPluginObject(pluginInfo) {
    var info = "";
    var value = "";
    var part = "";
    var limit = "";
    var rv = 0;
    var i = 0;
    var j = 0;
    var store = null;
    // Initial call (status = 0) will we set all parameters and initiate invoke,
    // second call should check for result and get responses.
    if (pluginInfo.status == 0) {
        // Clear events. When nothing available abort, should be either true or false.
        if (iid_GetProperty("EventPresent") != "") {
            // Set all properties
            i = 0;
            while ((part = GetPartBy(pluginInfo.param_set, i, ';')) != "") {
                iid_SetProperty(GetPartBy(part, 0, '='), GetPartBy(part, 1, '='));
                i++;
            }
            // Invoke all commands, last with InvokeThread=true
            i = 0;
            while ((part = GetPartBy(pluginInfo.param_invoke, i, ';')) != "") {
                if (GetPartBy(pluginInfo.param_invoke, i + 1, ';') == "") {
                    iid_SetProperty("InvokeThread", "true");
                }
                rv = iid_Invoke(part);
                i++;
            }
            HandlePluginResponse(pluginInfo, rv);
        }
    }
    // Second call
    else {
        // Get all results
        i = 0;
        while ((part = GetPartBy(pluginInfo.param_enum, i, ';')) != "") {
            if ((limit = GetPartBy(part, 1, ',')) != "") {
                iid_SetProperty("EnumLimit", limit);
            }
            part = GetPartBy(part, 0, ',');
            j = 0;
            while ((value = iid_EnumProperty(part, j)) != "") {
                j++;
                info += part + j + "=" + URL.encode(value) + "&";
            }
            info += part + "Count=" + j + "&";
            if (limit != "") {
                iid_SetProperty("EnumLimit", "");
            }
            i++;
        }
        i = 0;
        while ((part = GetPartBy(pluginInfo.param_get, i, ';')) != "") {
            info += part + "=" + URL.encode(iid_GetProperty(part)) + "&";
            i++;
        }
        info += "result=" + GetPartBy(iid_GetProperty("Error " + pluginInfo.result), 0, ';');
        // Send result unless none specified
        if (pluginInfo.next != "none") {
            // Reload if new events (card inserted/removed during loading). Still
            // some action like ResetToken will generate new events, so only
            // check for events when enumerating.
            if ((pluginInfo.result == 0) &&
                (pluginInfo.param_invoke.indexOf("Reset") == -1) &&
                (pluginInfo.param_enum != null) &&
                (pluginInfo.param_enum.length > 0) &&
                (iid_GetProperty("EventPresent") == "true")) {
                // Reset status and restart function
                pluginInfo.status = 0;
                CallPluginObject(pluginInfo);
            }
            else {
                if (pluginInfo.callback != null) {
                    pluginInfo.callback(GetVariablesHREF(info, null));
                }
                else {
                    // IE can't handle more than 2036 bytes, but store as variable as soon as >1024
                    if (info.length > 1024) {
                        store = new DataStore();
                        store.setValue("href", info);
                        info = "";
                    }
                    LoadPage(pluginInfo.next, info);
                }
            }
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// CallPluginEvent
//-----------------------------------------------------------------------------
function CallPluginEvent(next, callback) {
    var i = 0;
    var info = "";
    while ((iid_GetProperty("EventPresent") == "true") && (i < 10)) {
        i++;
    }
    if (i > 0) {
        info = "rv=CKR_OK";
        if (callback != null) {
            callback(GetVariablesHREF(info, null));
        }
        else {
            LoadPage(next, info);
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// CallPluginApp
//-----------------------------------------------------------------------------
function CallPluginApp(next, action, param_set, param_invoke, param_enum, param_get) {
    if (document.form1.next != null) {
        document.form1.next.value = next;
    }
    if (document.form1.iid_set != null) {
        document.form1.iid_set.value = param_set;
    }
    if (document.form1.iid_invoke != null) {
        document.form1.iid_invoke.value = param_invoke;
    }
    if (document.form1.iid_enum != null) {
        document.form1.iid_enum.value = param_enum;
    }
    if (document.form1.iid_get != null) {
        document.form1.iid_get.value = param_get;
    }
    document.form1.action = "iid://" + action;
    document.form1.submit();
    return;
}
//-----------------------------------------------------------------------------
// CallPluginComponent
//-----------------------------------------------------------------------------
function CallPluginComponent(next, action, param_set, param_invoke, param_enum, param_get, callback) {
    var available = true;
    // Action "reload" is only available for plugin app, so change to load of
    // start page
    if ((action.indexOf("reload") != -1) && !HasPluginApp()) {
        next = "index.html";
        action = "plugin";
    }
    // Call function based on action, always plugin object if available
    if ((action.indexOf("plugin") != -1) && HasPluginObject()) {
        if ((PLUGIN_INFO = new PluginInfo(next, action, param_set, param_invoke, param_enum, param_get, callback)) != null) {
            setTimeout(DelayedCallPluginObject, 111);
        }
    }
    else if ((action.indexOf("event") != -1) && HasPluginObject()) {
        CallPluginEvent(next, callback);
    }
    else if (HasPluginApp()) {
        CallPluginApp(next, action, param_set, param_invoke, param_enum, param_get);
    }
    else {
        available = false;
    }
    return available;
}
//-----------------------------------------------------------------------------
// PackAllVariables
//-----------------------------------------------------------------------------
function PackAllVariables(vars) {
    var name = null;
    var value = "";
    var info = "";
    // Enumerate all values
    for (name in vars) {
        // Never add result code since used to identify state of page
        if (name != "result") {
            // Only keep real name variables
            if (name.length > 2) {
                if ((value = vars[name]) != null) {
                    info += name + "=" + URL.encode(value) + "&"
                }
            }
        }
    }
    return info;
}
//-----------------------------------------------------------------------------
// StoreAllVariables
//-----------------------------------------------------------------------------
function StoreAllVariables(vars) {
    var store = new DataStore();
    var value = null;
    if (vars != null) {
        value = PackAllVariables(vars);
    }
    store.setValue("cache", value);
    return;
}
//-----------------------------------------------------------------------------
// RestoreAllVariables
//-----------------------------------------------------------------------------
function RestoreAllVariables() {
    var vars = new Array();
    var store = new DataStore();
    var info = "";
    var list = null;
    var elem = null;
    var i = 0;
    if ((info = store.getValue("cache")) != null) {
        list = info.split('&');
        for (i = 0; i < list.length; i++) {
            if (list[i].indexOf('=') != -1) {
                elem = list[i].split('=');
                vars.push(elem[0]);
                vars[elem[0]] = URL.decode(elem[1]);
            }
        }
    }
    return vars;
}
//-----------------------------------------------------------------------------
// ResetAllVariables
//-----------------------------------------------------------------------------
function ResetAllVariables() {
    StoreAllVariables(null);
    return;
}
//-----------------------------------------------------------------------------
// AppendToAllVariables
//-----------------------------------------------------------------------------
function AppendToAllVariables(varsAdd) {
    var vars = null;
    var name = null;
    var value = "";
    if ((vars = RestoreAllVariables()) != null) {
        // Replace all fram current list of values
        for (name in varsAdd) {
            // Only keep real name variables
            if (name.length > 2) {
                vars[name] = varsAdd[name];
            }
        }
        StoreAllVariables(vars);
    }
    return vars;
}
//-----------------------------------------------------------------------------
// ReplaceForAllVariables
//-----------------------------------------------------------------------------
function ReplaceForAllVariables(name, value) {
    var vars = null;
    if ((vars = RestoreAllVariables()) != null) {
        vars[name] = value;
        StoreAllVariables(vars);
    }
    return vars;
}
//-----------------------------------------------------------------------------
// LoadPluginVariables
//-----------------------------------------------------------------------------
function LoadPluginVariables(param_set, param_invoke, callback) {
    var param_get = null;
    var param_enum = null;
    ResetAllVariables();
    // Set parameter is optional
    if (param_set == null) {
        param_set = "";
    }
    // Invoke parameter is optional
    if (param_invoke == null) {
        param_invoke = "";
    }
    // Send GUI version to trace
    if (param_set.length > 0) {
        param_set += ";";
    }
    param_set += "gui=" + GetVersionGUI();
    // Get all available slots/tokens/certificates. To limit size of read skip
    // some certificate attributes.
    param_enum = "Slot;Token;CertificateEx,0x3FF2;Language";
    param_get = "ProductInfo;SupportInfo;Config:Language:Current;ConfigGlobal:Administration:View";
    // Call plugin component either using plugin object or plugin app
    return CallPluginComponent(document.form1.next.value, "plugin", param_set, param_invoke, param_enum, param_get, callback);
}
//-----------------------------------------------------------------------------
// PackPluginVariables
//-----------------------------------------------------------------------------
function PackPluginVariables(vars) {
    var info = "";
    var value = "";
    var i = 0;
    var count = 0;
    if (vars != null) {
        for (i = 0; i <= 3; i++) {
            switch (i) {
                case 0: name = "Slot"; break;
                case 1: name = "Token"; break;
                case 2: name = "CertificateEx"; break;
                case 3: name = "Language"; break;
            }
            count = 1;
            while ((value = vars[name + count]) != null) {
                info += name + count + "=" + URL.encode(value) + "&";
                count++;
            }
        }
        for (i = 0; i <= 3; i++) {
            switch (i) {
                case 0: name = "ProductInfo"; break;
                case 1: name = "SupportInfo"; break;
                case 2: name = "Config:Language:Current"; break;
                case 3: name = "ConfigGlobal:Administration:View"; break;
            }
            if ((value = vars[name]) != null) {
                info += name + "=" + URL.encode(value) + "&";
            }
        }
    }
    return info;
}
//-----------------------------------------------------------------------------
// LoadSite
//-----------------------------------------------------------------------------
function LoadSite(site, arguments) {
    var value = null;
    value = site;
    if (arguments != null) {
        value += "?" + arguments;
    }
    top.document.location = value;
    return;
}
//-----------------------------------------------------------------------------
// LoadPage
//-----------------------------------------------------------------------------
function LoadPage(page, arguments) {
    // Skip if none
    if (page != "none") {
        // Append .html if missing
        if (page.indexOf(".html") == -1) {
            page += ".html"
        }
        // Skip reload of initialize page
        if ((page == "index.html") && (arguments == null)) {
            if ((arguments = PackAllVariables()) != null) {
                arguments += "&result=CKR_OK"
            }
        }
        LoadSite(page, arguments);
    }
    return;
}
//-----------------------------------------------------------------------------
// GetCertificateImageByName
//-----------------------------------------------------------------------------
function GetCertificateImageByName(certificate) {
    var value = null;
    if ((value = GetImageByName(certificate.name, false)) == null) {
        value = GetImageByName(certificate.company, false);
    }
    return value;
}
//-----------------------------------------------------------------------------
// GetImageByName
//-----------------------------------------------------------------------------
function GetImageByName(name, force) {
    var value = null;
    var part = null;
    var part2 = null;
    var i = 0;
    if (force || GetCustomFlagUseDynamicImage()) {
        LoadDynamicImages();
        if (name != null) {
            name = FormatHTML(name);
            // Check list of those stored locally
            while ((part = GetPartBy(IMAGE_NAME, i, "|")) != "") {
                part2 = GetPartBy(part, 1, ",");
                part = GetPartBy(part, 0, ",");
                if (name.indexOf(FormatHTML(URL.decode(part))) != -1) {
                    if (part2 != "") {
                        part = part2;
                    }
                    part = part.toLowerCase();
                    part = ReplaceAll(part, " ", "_");
                    value = "tile_" + part + ".png";
                    break;
                }
                i++;
            }
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// GetBigImageByName
//-----------------------------------------------------------------------------
function GetBigImageByName(name, force) {
    var value = null;
    if ((value = GetImageByName(name, force)) != null) {
        value = value.substr(0, value.length - 4) + "2.png";
    }
    return value;
}
//-----------------------------------------------------------------------------
// GetImageByPath
//-----------------------------------------------------------------------------
function GetImageByPath(image, address) {
    var value = null;
    var id = null;
    var i = 0;
    var client = null;
    // Some images are stored locally, so only download unknown
    if (image != null) {
        id = image;
        if ((i = id.indexOf("/")) != -1) {
            id = id.substr(i + 1);
        }
        if ((i = id.lastIndexOf(".")) != -1) {
            id = id.substr(0, i);
        }
        i = 0;
        while ((value = GetPart(IMAGE_ID, i)) != "") {
            if (id == value) {
                break;
            }
            i++;
        }
        // Take it if found
        if (value != "") {
            value = "srv_" + id + ".png";
        }
        // Third server may have it (server image)
        else if (address != null) {
            client = new AccessClient(address);
            value = client.GetURL();
            value = value.substr(0, value.lastIndexOf("/") + 1);
            value += image;
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// GetPartBy
//-----------------------------------------------------------------------------
function GetPartBy(text, pos, c) {
    var text2 = "";
    if ((text != null) && (text != "")) {
        text2 = text;
        while ((text2 != "") && (pos > 0)) {
            if (text2.indexOf(c) > 0) {
                text2 = text2.substr(text2.indexOf(c) + 1, text2.length - text2.indexOf(c) - 1);
            }
            else if (text2.indexOf(c) == 0) {
                text2 = text2.substr(1, text2.length);
            }
            else {
                text2 = "";
            }
            pos--;
        }
        if (text2 != "") {
            if (text2.indexOf(c) > 0) {
                text2 = text2.substr(0, text2.indexOf(c));
            }
            else if (text2.indexOf(c) == 0) {
                text2 = "";
            }
        }
    }
    return text2;
}
//-----------------------------------------------------------------------------
// GetPart
//-----------------------------------------------------------------------------
function GetPart(text, pos) {
    return GetPartBy(text, pos, ';');
}
//-----------------------------------------------------------------------------
// GetPartCount
//-----------------------------------------------------------------------------
function GetPartCount(text) {
    var count = 0;
    while ((count < 32) && (GetPart(text, count) != "")) {
        count += 1;
    }
    return count;
}
//-----------------------------------------------------------------------------
// GetRdnPart
//-----------------------------------------------------------------------------
function GetRdnPart(text, pos) {
    var text2 = "";
    var i = 0;
    var j = 0;
    while (true) {
        if ((i = text.indexOf('=')) == -1) {
            text2 = "";
            break;
        }
        else {
            text2 = text.substr(i + 1, text.length);
            if (text[i + 1] == '\"') {
                j = text2.indexOf('\"');
            }
            else {
                j = text2.indexOf(',');
            }
            if (j == -1) {
                j = text2.length;
            }
            text2 = text.substr(0, i + j + 1);
            if (text.indexOf('=') == -1) {
                text = "";
            }
            else {
                text = text.substr(i + j + 2, text.length);
            }
        }
        if (pos == 0)
            break;
        pos--;
    }
    while ((text2.length > 0) && (text2.charAt(0) == ' ')) {
        text2 = text2.substr(1, text2.length);
    }
    return text2;
}
//-----------------------------------------------------------------------------
// GetRdnCount
//-----------------------------------------------------------------------------
function GetRdnCount(text) {
    var count = 0;
    while ((count < 16) && (GetRdnPart(text, count) != "")) {
        count += 1;
    }
    return count;
}
//-----------------------------------------------------------------------------
// GetRdnOid
//-----------------------------------------------------------------------------
function GetRdnOid(text) {
    return GetPartBy(text, 0, '=');
}
//-----------------------------------------------------------------------------
// GetRdnValue
//-----------------------------------------------------------------------------
function GetRdnValue(text, convert) {
    text = GetPartBy(text, 1, '=');
    if (convert) {
        if (text.indexOf("%", 0) != -1) {
            text = URL.decode(text);
        }
        text = FormatHTML(text);
    }
    return text;
}
//-----------------------------------------------------------------------------
// GetValueByOid
//-----------------------------------------------------------------------------
function GetValueByOid(text, oid, convert) {
    var i = 0;
    var part = "";
    var value = "";
    while ((part = GetRdnPart(text, i)) != "") {
        if (GetRdnOid(part) == oid) {
            if (value != "") {
                value += ", ";
            }
            value += GetRdnValue(part, convert);
        }
        i++;
    }
    return value;
}
//-----------------------------------------------------------------------------
// ResetDataStore
//-----------------------------------------------------------------------------
function ResetDataStore() {
    var store = new DataStore();
    var service_user = null;
    var service_srv = null;
    var authenticated = null;
    // Only need to keep active user values and service_srv
    service_user = store.getValue("service_user");
    service_srv = store.getValue("service_srv");
    authenticated = store.getValue("authenticated");
    // Reset all variables
    store.resetAll();
    // Restore kept variables
    store.setValue("service_user", service_user)
    store.setValue("service_srv", service_srv)
    store.setValue("authenticated", authenticated)
    return;
}
//-----------------------------------------------------------------------------
// GetServiceInfo
//-----------------------------------------------------------------------------
function GetServiceInfo(address) {
    var store = new DataStore();
    var client = null;
    var info = null;
    var service = null;
    client = new AccessClient(address);
    if ((info = client.GetConnectInfo()) != null) {
        service = new ServiceInfo(address, info.name, info.version, info.image, info.keyid, info.functions, info.site);
    }
    return service;
}
//-----------------------------------------------------------------------------
// IsSlotConnected
//-----------------------------------------------------------------------------
function IsSlotConnected(slotid1, slotid2, slotList) {
    var connected = false;
    var i = 0;
    var description = null;
    // Same slot are always connected
    if (slotid1 == slotid2) {
        connected = true;
    }
    else {
        // All "connected" slots share the same slot description. A multi-pin token
        // will be separated into several slots, but information like certificates
        // should be presented as a single token.
        for (i = 0; i < slotList.length; i++) {
            if (slotList[i].slotid == slotid1) {
                description = slotList[i].description;
            }
        }
        for (i = 0; i < slotList.length; i++) {
            if (slotList[i].slotid == slotid2) {
                if (description == slotList[i].description) {
                    connected = true;
                }
            }
        }
    }
    return connected;
}
//-----------------------------------------------------------------------------
// AllowedCertificate
//-----------------------------------------------------------------------------
function AllowedCertificate(certificate, service, slotid, slotList) {
    var allowed = false;
    var authID = null;
    var value = null;
    var i = 0;
    // Either by slotid or service
    if (slotid != null) {
        allowed = IsSlotConnected(slotid, certificate.slotid, slotList);
    }
    // Null service will allow all
    else if (service == null) {
        allowed = true;
    }
    else if (certificate.authID != null) {
        authID = certificate.authID.toUpperCase();
        while ((value = GetPartBy(service.keyid, i, "|")) != "") {
            value = value.toUpperCase();
            if (value == authID) {
                allowed = true;
                break;
            }
            i++;
        }
    }
    return allowed;
}
//-----------------------------------------------------------------------------
// ActiveUserInfo
//-----------------------------------------------------------------------------
function ActiveUserInfo(address, personalNumber, name, company, secret) {
    this.address = address;
    this.personalNumber = personalNumber;
    this.name = name;
    this.company = company;
    this.secret = secret;
}
//-----------------------------------------------------------------------------
// PackActiveUserInfo
//-----------------------------------------------------------------------------
function PackActiveUserInfo(user, secret) {
    var information = null;
    if ((user != null) && (secret != null)) {
        information = URL.encode(user.address) + ",";
        information += URL.encode(user.personalNumber) + ",";
        information += URL.encode(user.name) + ",";
        information += URL.encode(user.company) + ",";
        information += URL.encode(secret);
    }
    return information;
}
//-----------------------------------------------------------------------------
// ParseActiveUserInfo
//-----------------------------------------------------------------------------
function ParseActiveUserInfo(information) {
    var userInfo = null;
    if ((information != null) && (information != null)) {
        userInfo = new ActiveUserInfo(
            URL.decode(GetPartBy(information, 0, ',')),
            URL.decode(GetPartBy(information, 1, ',')),
            URL.decode(GetPartBy(information, 2, ',')),
            URL.decode(GetPartBy(information, 3, ',')),
            URL.decode(GetPartBy(information, 4, ',')));
        if ((userInfo.address == "") ||
            (userInfo.personalNumber == "") ||
            (userInfo.name == "") ||
            (userInfo.company == "")) {
            userInfo = null;
        }
    }
    return userInfo;
}
//-----------------------------------------------------------------------------
// SetActiveUserInfo
//-----------------------------------------------------------------------------
function SetActiveUserInfo(userInfo, secret) {
    var store = new DataStore();
    var information = null;
    var userInfo2 = null;
    var value = null;
    var newValue = "";
    var part = null;
    var i = 0;
    // Copy all values except specified personalNumber
    if ((value = store.getValue("service_user")) != null) {
        while ((part = GetPartBy(value, i, ';')) != "") {
            if ((userInfo2 = ParseActiveUserInfo(part)) != null) {
                if ((userInfo2.address != userInfo.address) ||
                    (userInfo2.personalNumber != userInfo.personalNumber)) {
                    newValue += part + ";";
                }
            }
            i++;
        }
    }
    // Set new value for personalNumber unless empty (remove)
    if ((secret != null) && (secret != "")) {
        if ((information = PackActiveUserInfo(userInfo, secret)) != null) {
            newValue += information;
        }
    }
    // Keep value less than 2K. Updated values are added at end, so
    // remove from start
    while (newValue.length > 2048) {
        newValue = newValue.substr(newValue.indexOf(';') + 1);
    }
    // Set value
    store.setValue("service_user", newValue);
    return;
}
//-----------------------------------------------------------------------------
// ResetActiveUserInfo
//-----------------------------------------------------------------------------
function ResetActiveUserInfo(userInfo) {
    SetActiveUserInfo(userInfo, null);
    return;
}
//-----------------------------------------------------------------------------
// ResetActiveUserInfos
//-----------------------------------------------------------------------------
function ResetActiveUserInfos(address) {
    var i = 0;
    list = null;
    if ((list = CreateActiveUserList(address)) != null) {
        for (i = 0; i < list.length; i++) {
            ResetActiveUserInfo(list[i]);
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// GetActiveUserInfo
//-----------------------------------------------------------------------------
function GetActiveUserInfo(address, personalNumber) {
    var userInfo = null;
    var store = new DataStore();
    var value = null;
    var part = null;
    var i = 0;
    if ((value = store.getValue("service_user")) != null) {
        while ((part = GetPartBy(value, i, ';')) != "") {
            if ((userInfo = ParseActiveUserInfo(part)) != null) {
                if ((userInfo.address == address) &&
                    (userInfo.personalNumber == personalNumber)) {
                    break;
                }
                userInfo = null;
            }
            i++;
        }
    }
    return userInfo;
}
//-----------------------------------------------------------------------------
// GetAllActiveUserInfo
//-----------------------------------------------------------------------------
function GetAllActiveUserInfo() {
    var store = new DataStore();
    return store.getValue("service_user");
}
//-----------------------------------------------------------------------------
// HasActiveUserInfo
//-----------------------------------------------------------------------------
function HasActiveUserInfo(address, personalNumber) {
    return (GetActiveUserInfo(address, personalNumber) != null);
}
//-----------------------------------------------------------------------------
// CreateActiveUserList
//-----------------------------------------------------------------------------
function CreateActiveUserList(address) {
    var list = new Array();
    var userInfo = null;
    var store = new DataStore();
    var value = null;
    var part = null;
    var i = 0;
    if ((value = store.getValue("service_user")) != null) {
        while ((part = GetPartBy(value, i, ';')) != "") {
            if ((userInfo = ParseActiveUserInfo(part)) != null) {
                if (userInfo.address == address) {
                    list.push(userInfo);
                }
            }
            i++;
        }
    }
    return list;
}
//-----------------------------------------------------------------------------
// HasActiveRequest
//-----------------------------------------------------------------------------
function HasActiveRequest(listRequest, address, personalNumber) {
    var available = false;
    var i = 0;
    if (listRequest != null) {
        for (i = 0; i < listRequest.length; i++) {
            if (listRequest[i].address == address) {
                if (listRequest[i].personalNumber == personalNumber) {
                    available = true;
                    break;
                }
            }
        }
    }
    return available;
}
//-----------------------------------------------------------------------------
// SetSettingsValue
//-----------------------------------------------------------------------------
function SetSettingsValue(name, data) {
    var store = new DataStore();
    var value = null;
    var newValue = "";
    var part = null;
    var i = 0;
    // Copy all values except specified
    if ((value = store.getValue("service_srv")) != null) {
        while ((part = GetPartBy(value, i, ';')) != "") {
            if (GetPartBy(part, 0, '=') != name) {
                newValue += part + ";";
            }
            i++;
        }
    }
    // Set new value for unless empty (remove)
    if ((data != null) && (data != "")) {
        newValue += name + "=" + URL.encode(data) + ";";
    }
    // Set value
    store.setValue("service_srv", newValue);
    return;
}
//-----------------------------------------------------------------------------
// GetSettingsValue
//-----------------------------------------------------------------------------
function GetSettingsValue(name) {
    var data = null;
    var store = new DataStore();
    var value = null;
    var part = null;
    var i = 0;
    // Enumerate all values
    if ((value = store.getValue("service_srv")) != null) {
        while ((part = GetPartBy(value, i, ';')) != "") {
            if (GetPartBy(part, 0, '=') == name) {
                data = URL.decode(GetPartBy(part, 1, '='));
                break;
            }
            i++;
        }
    }
    return data;
}
//-----------------------------------------------------------------------------
// NameVersionInfo
//-----------------------------------------------------------------------------
function NameVersionInfo(name, version) {
    this.name = name;
    this.version = version;
    this.explorer = (name.indexOf("MSIE") >= 0);
}
//-----------------------------------------------------------------------------
// ParseBrowserInfo
//-----------------------------------------------------------------------------
function ParseBrowserInfo(value) {
    var name = null;
    var version = null;
    var i = 0;
    var j = 0;
    // Get browser name and version, either last entry or known string
    if ((i = value.indexOf("MSIE ")) > 0) {
        // Microsoft Internet Explorer
        name = "MSIE";
        i += 5;
        j = 0;
        while ((value.charAt(i + j) != '\0') && (value.charAt(i + j) != ';')) {
            j++;
        }
        version = value.substr(i, j);
    }
    // For IE11 Microsoft have changed completly, look for .NET or Trident
    else if ((value.indexOf(".NET") > 0) || (value.indexOf("Trident") > 0)) {
        name = "MSIE";
        if ((i = value.indexOf("; rv:")) > 0) {
            i += 5;
            j = 0;
            while ((value.charAt(i + j) != '\0') && (value.charAt(i + j) != ')')) {
                j++;
            }
            version = value.substr(i, j);
        }
    }
    // Chrome/Chromium include WebKit, so take first (<name>/<version>)
    else if (((i = value.lastIndexOf("Chromium")) > 0) ||
        ((i = value.lastIndexOf("Chrome")) > 0)) {
        j = 0;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != '/')) {
            j++;
        }
        name = value.substr(i, j);
        version = value.substr(i + j + 1);
        if ((i = version.indexOf(' ')) > 0) {
            version = version.substr(0, i);
        }
    }
    // WebKit are availble in different flavors (???WebKit/<version>)
    else if ((i = value.indexOf("WebKit")) > 0) {
        j = i + 6;
        while ((i > 0) && (value.charAt(i - 1) != ' ')) {
            i--;
        }
        name = value.substr(i, j - i);
        i = j + 1;
        j = 0;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != ' ')) {
            j++;
        }
        version = value.substr(i, j);
    }
    // Let's hope last entry tells browser (<name>/<version>)
    else if ((i = value.lastIndexOf(" ")) > 0) {
        i++;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != '/')) {
            j++;
        }
        name = value.substr(i, j);
        version = value.substr(i + j + 1);
    }
    // Need default value
    if (name == null) {
        name = "Unknown";
    }
    if (version == null) {
        version = "0.0";
    }
    // Create name version info object
    return new NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// ParseDeviceInfo
//-----------------------------------------------------------------------------
function ParseDeviceInfo(value) {
    var name = null;
    var version = null;
    var i = 0;
    var j = 0;
    var k = 0;
    var part = null;
    // Get os name and version. Information is stored a little different
    // depending on browser, but available between '(' and ')'.
    if ((i = value.indexOf('(')) > 0) {
        value = value.substr(i + 1);
        if ((i = value.indexOf(')')) > 0) {
            value = value.substr(0, i);
        }
    }
    // Enumerate all arguments and try to extract information
    i = 0;
    while ((part = GetPartBy(value, i, ';')) != "") {
        // Format "<name>" or "...<name>...<version>"
        if (((j = part.indexOf("Windows")) >= 0) ||
            ((j = part.indexOf("Android")) >= 0) ||
            ((j = part.indexOf("iPhone")) >= 0) ||
            ((j = part.indexOf("iPad")) >= 0) ||
            ((j = part.indexOf("CPU OS")) >= 0) || // iPad
            ((j = part.indexOf("Macintosh")) >= 0) ||
            ((j = part.indexOf("Mac OS X")) >= 0)) {
            // Get eventual following version number
            k = 0;
            while ((part.length > (j + k)) &&
            ((part.charAt(j + k) < '0') || (part.charAt(j + k) > '9'))) {
                k++;
            }
            // Set all except version number as name (Android may be specified with Linux too)
            if ((name == null) || (name == "Linux")) {
                name = part.substr(j, k).replace(/^\s+|\s+$/g, "");
            }
            // Found version?
            part = part.substr(j + k).replace(/^\s+|\s+$/g, "");
            k = 0;
            if ((part.charAt(0) >= '0') && (part.charAt(0) <= '9')) {
                part = part.replace(new RegExp("_", 'g'), ".");
                k = 0;
                while ((part.charAt(k) == '.') ||
                ((part.charAt(k) >= '0') && (part.charAt(k) <= '9'))) {
                    k++;
                }
                version = part.substr(0, k);
            }
            // Break when we have both name and version
            if ((name != null) && (version != null)) {
                break;
            }
        }
        // Linux only namn no version
        else if ((j = part.indexOf("Linux")) >= 0) {
            name = "Linux";
        }
        i++;
    }
    // Need default value
    if (name == null) {
        name = navigator.platform;
    }
    if (version == null) {
        version = "0.0";
    }
    // Create name version info object
    return new NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// DeviceInfo
//-----------------------------------------------------------------------------
function DeviceInfo(info) {
    if (info == null) {
        info = navigator.userAgent;
    }
    // Get os name and version
    this.device = ParseDeviceInfo(info);
    // Get browser name and version
    this.browser = ParseBrowserInfo(info);
}
//-----------------------------------------------------------------------------
// ParseUserInfo
//-----------------------------------------------------------------------------
function ParseUserInfo(information) {
    var user = null;
    if ((information != null) && (information.length > 7)) {
        user = new UserInfo(
            GetPartBy(information, 0, ";"),
            GetPartBy(information, 1, ";"),
            GetPartBy(information, 2, ";"),
            GetPartBy(information, 3, ";"),
            GetPartBy(information, 4, ";"),
            GetPartBy(information, 5, ";"),
            GetPartBy(information, 6, ";"));
        // Some fields are mandatory
        if ((user.personalNumber == "") ||
            (user.name == "") ||
            (user.company == "") ||
            (user.token == "") ||
            (user.certificateID == "")) {
            user = null;
        }
    }
    return user;
}
//-----------------------------------------------------------------------------
// PackUserInfo
//-----------------------------------------------------------------------------
function PackUserInfo(user) {
    var information = "";
    var certificate = null;
    if (user != null) {
        information += user.personalNumber + ";";
        information += user.name + ";";
        information += user.company + ";";
        information += user.token + ";";
        information += user.certificateID + ";";
        if (user.certificateValue != null) {
            information += user.certificateValue;
        }
        information += ";";
        if (user.sharedSecret != null) {
            information += user.sharedSecret;
        }
    }
    return information;
}
//-----------------------------------------------------------------------------
// ParseRequestInfo
//-----------------------------------------------------------------------------
function ParseRequestInfo(information) {
    var request = null;
    if ((information != null) && (information.length > 8)) {
        request = new RequestInfo(
            GetPartBy(information, 0, ";"),
            GetPartBy(information, 1, ";"),
            GetPartBy(information, 2, ";"),
            GetPartBy(information, 3, ";"),
            GetPartBy(information, 4, ";"),
            GetPartBy(information, 5, ";"),
            GetPartBy(information, 6, ";"),
            GetPartBy(information, 7, ";"),
            GetPartBy(information, 8, ";"));
        // Some fields are mandatory
        if ((request.address == "") ||
            (request.personalNumber == "") ||
            (request.orderRef == "") ||
            (request.type == "") ||
            (request.orderRef == "") ||
            ((request.visibleData == "") && (request.nonVisibleData == "")) ||
            (request.date == "")) {
            request = null;
        }
    }
    return request;
}
//-----------------------------------------------------------------------------
// PackRequestInfo
//-----------------------------------------------------------------------------
function PackRequestInfo(request) {
    var information = "";
    if (request != null) {
        information += request.address + ";";
        information += request.personalNumber + ";";
        information += request.orderRef + ";";
        information += request.type + ";";
        information += request.serverName + ";";
        information += request.serverImage + ";";
        if (request.visibleData != null) {
            information += request.visibleData;
        }
        information += ";";
        if (request.nonVisibleData != null) {
            information += request.nonVisibleData;
        }
        information += ";";
        information += request.date;
    }
    return information;
}
//-----------------------------------------------------------------------------
// CertificateInfo
//-----------------------------------------------------------------------------
function CertificateInfo(slotid, issuer, subject, validFrom, validTo, ca, cred, certid, authid, usage, expire) {
    var name = null;
    var personalNumber = null;
    var company = null;
    // Take most values as is
    this.slotid = slotid;
    this.issuer = issuer;
    this.subject = subject;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.ca = ca;
    this.cred = cred;
    this.certID = certid;
    this.authID = authid;
    this.usage = usage;
    // Expire added for 6.0, position used for value earlier, so check length
    if (expire.length <= 4) {
        this.expire = expire;
    }
    else {
        this.expire = "";
    }
    // Name always common name from subject
    name = GetValueByOid(subject, "CN", false);
    // PersonalNumber (either serialNumber or UPN)
    if (((personalNumber = GetValueByOid(subject, "2.5.4.5", false)) == "") &&
        ((personalNumber = GetValueByOid(subject, "SERIALNUMBER", false)) == "")) {
        personalNumber = cred;
    }
    // Company may be used to display image. Will take organization from either
    // subject or issuer, and even try issuer commonName.
    if (((company = GetValueByOid(subject, "O", false)) == "") ||
        (GetImageByName(company, false) == null)) {
        if (((company = GetValueByOid(issuer, "O", false)) == "") ||
            (GetImageByName(company, false) == null)) {
            if (((company = GetValueByOid(issuer, "CN", false)) == "") ||
                (GetImageByName(company, false) == null)) {
                company = null;
            }
        }
    }
    // If unable to get image, still try to get company with same order as above.
    if ((company = GetValueByOid(subject, "O", false)) == "") {
        if ((company = GetValueByOid(issuer, "O", false)) == "") {
            company = GetValueByOid(issuer, "CN", false);
        }
    }
    // Set generated values
    this.personalNumber = personalNumber;
    this.name = name;
    this.company = company;
    if (ca == "0") {
        this.isCA = false;
    }
    else {
        this.isCA = true;
    }
}
//-----------------------------------------------------------------------------
// SlotInfo
//-----------------------------------------------------------------------------
function SlotInfo(slotid, description, token) {
    this.slotid = slotid;
    this.description = description;
    this.present = (token != null);
    this.token = token;
}
//-----------------------------------------------------------------------------
// TokenInfo
//-----------------------------------------------------------------------------
function TokenInfo(slotid, label, number, manufacturer, type, pinMin, pinMax, pinAttempts, pinType, pinPolicy) {
    this.slotid = slotid;
    this.label = label;
    this.number = number;
    this.manufacturer = manufacturer;
    this.type = type;
    this.pinMin = pinMin;
    this.pinMax = pinMax;
    this.pinAttempts = pinAttempts;
    this.pinType = pinType;
    this.pinPolicy = pinPolicy;
}
//-----------------------------------------------------------------------------
// ServiceInfo
//-----------------------------------------------------------------------------
function ServiceInfo(address, name, version, image, keyid, functions, site) {
    this.address = address.toLowerCase();
    this.name = name;
    this.version = version;
    this.image = image;
    this.keyid = keyid;
    this.functions = functions;
    this.site = site;
}
//-----------------------------------------------------------------------------
// ParseCertificateInfo
//-----------------------------------------------------------------------------
function ParseCertificateInfo(information) {
    var certificate = null;
    if ((information != null) && (information.length > 10)) {
        certificate = new CertificateInfo(
            GetPartBy(information, 0, ';'),
            URL.decode(GetPartBy(information, 1, ';')),
            URL.decode(GetPartBy(information, 2, ';')),
            GetPartBy(information, 3, ';'),
            GetPartBy(information, 4, ';'),
            GetPartBy(information, 5, ';'),
            GetPartBy(information, 6, ';'),
            GetPartBy(information, 7, ';'),
            GetPartBy(information, 8, ';'),
            GetPartBy(information, 9, ';'),
            GetPartBy(information, 10, ';'));
    }
    return certificate;
}
//-----------------------------------------------------------------------------
// PackCertificateInfo
//-----------------------------------------------------------------------------
function PackCertificateInfo(certificate) {
    var information = null;
    if (certificate != null) {
        // Need a format that will be recreated above, so skip unneeded
        // information
        information = certificate.slotid + ";";
        information += URL.encode(certificate.issuer) + ";";
        information += URL.encode(certificate.subject) + ";";
        information += certificate.validFrom + ";";
        information += certificate.validTo + ";";
        information += certificate.ca + ";";
        information += certificate.cred + ";";
        information += certificate.certID + ";";
        information += certificate.authID + ";";
        information += certificate.usage + ";";
        information += certificate.expire + ";";
    }
    return information;
}
//-----------------------------------------------------------------------------
// ParseSlotInfo
//-----------------------------------------------------------------------------
function ParseSlotInfo(information, tokenList) {
    var slotid = null;
    var token = null;
    var id = null;
    var i = 0;
    if ((information != null) && (information.length > 2)) {
        // Find eventual corresponding token object
        if ((slotid = GetPartBy(information, 0, ';')) != "") {
            if (tokenList != null) {
                for (i = 0; i < tokenList.length; i++) {
                    if (slotid == tokenList[i].slotid) {
                        token = tokenList[i];
                        break;
                    }
                }
            }
            // Create slot object
            slot = new SlotInfo(
                slotid,
                GetPartBy(information, 1, ';'),
                token);
        }
    }
    return slot;
}
//-----------------------------------------------------------------------------
// PackSlotInfo
//-----------------------------------------------------------------------------
function PackSlotInfo(slot) {
    var information = null;
    if (slot != null) {
        information = slot.slotid + ";";
        information += slot.description + ";";
        information += slot.present ? slot.token.label : "";
    }
    return information;
}
//-----------------------------------------------------------------------------
// ParseTokenInfo
//-----------------------------------------------------------------------------
function ParseTokenInfo(information) {
    var token = null;
    if ((information != null) && (information.length > 11)) {
        token = new TokenInfo(
            GetPartBy(information, 0, ';'),
            GetPartBy(information, 1, ';'),
            GetPartBy(information, 2, ';'),
            GetPartBy(information, 3, ';'),
            GetPartBy(information, 4, ';'),
            GetPartBy(information, 6, ';'),
            GetPartBy(information, 7, ';'),
            GetPartBy(information, 8, ';'),
            GetPartBy(information, 9, ';'),
            GetPartBy(information, 10, ';'));
    }
    return token;
}
//-----------------------------------------------------------------------------
// PackTokenInfo
//-----------------------------------------------------------------------------
function PackTokenInfo(token) {
    var information = null;
    if (token != null) {
        information = token.slotid + ";";
        information += token.label + ";";
        information += token.number + ";";
        information += token.manufacturer + ";";
        information += token.type + ";";
        information += ";";
        information += token.pinMin + ";";
        information += token.pinMax + ";";
        information += token.pinAttempts + ";";
        information += token.pinType + ";";
        information += token.pinPolicy;
    }
    return information;
}
//-----------------------------------------------------------------------------
// ParseServiceInfo
//-----------------------------------------------------------------------------
function ParseServiceInfo(information) {
    var service = null;
    if ((information != null) && (information.length > 5)) {
        service = new ServiceInfo(
            URL.decode(GetPartBy(information, 0, ',')),
            URL.decode(GetPartBy(information, 1, ',')),
            URL.decode(GetPartBy(information, 2, ',')),
            URL.decode(GetPartBy(information, 3, ',')),
            URL.decode(GetPartBy(information, 4, ',')),
            URL.decode(GetPartBy(information, 5, ',')),
            URL.decode(GetPartBy(information, 6, ',')));
    }
    return service;
}
//-----------------------------------------------------------------------------
// PackServiceInfo
//-----------------------------------------------------------------------------
function PackServiceInfo(service) {
    var information = null;
    if (service != null) {
        information = URL.encode(service.address) + ",";
        information += URL.encode(service.name) + ",";
        information += URL.encode(service.version) + ",";
        information += URL.encode(service.image) + ",";
        information += URL.encode(service.keyid) + ",";
        information += URL.encode(service.functions) + ",";
        information += URL.encode(service.site);
    }
    return information;
}
//-----------------------------------------------------------------------------
// CreateLanguageList
//-----------------------------------------------------------------------------
function CreateLanguageList(vars) {
    var list = null;
    var information = null;
    var index = 1;
    while ((information = vars["Language" + index]) != null) {
        if (list == null) {
            list = information;
        }
        else {
            list += "," + information;
        }
        index++;
    }
    return list;
}
//-----------------------------------------------------------------------------
// CreateCertificateList
//-----------------------------------------------------------------------------
function CreateCertificateList(vars, service, slotid, slotList) {
    var list = new Array();
    var information = null;
    var index = 1;
    var count = 0;
    var certificate = null;
    // Only return those certificates that are allowed
    while ((information = vars["CertificateEx" + index]) != null) {
        if ((certificate = ParseCertificateInfo(information)) != null) {
            if (AllowedCertificate(certificate, service, slotid, slotList)) {
                list[count++] = certificate;
            }
        }
        index++;
    }
    if (list.length == 0) {
        list = null;
    }
    return list;
}
//-----------------------------------------------------------------------------
// CreateTokenList
//-----------------------------------------------------------------------------
function CreateTokenList(vars) {
    var list = new Array();
    var information = null;
    var index = 1;
    var count = 0;
    var token = null;
    while ((information = vars["Token" + index]) != null) {
        if ((token = ParseTokenInfo(information)) != null) {
            list[count++] = token;
        }
        index++;
    }
    if (list.length == 0) {
        list = null;
    }
    return list;
}
//-----------------------------------------------------------------------------
// CreateSlotList
//-----------------------------------------------------------------------------
function CreateSlotList(vars, tokenList) {
    var list = new Array();
    var list2 = null;
    var information = null;
    var index = 1;
    var count = 0;
    var slot = null;
    var slotid = vars["slotid"];
    while ((information = vars["Slot" + index]) != null) {
        if ((slot = ParseSlotInfo(information, tokenList)) != null) {
            list[count++] = slot;
        }
        index++;
    }
    if ((list.length > 0) && (slotid != null) && (slotid.length > 0)) {
        count = 0;
        list2 = new Array();
        for (index=0; index < list.length; index++) {
            if (IsSlotConnected(parseInt(slotid), list[index].slotid, list)) {
                list[count++] = list[index];
            }
        }
    }
    if (count == 0) {
        list = null;
    }
    return list;
}
//-----------------------------------------------------------------------------
// CreateUserList
//-----------------------------------------------------------------------------
function CreateUserList(vars, service) {
    var listUser = new Array();
    var listCert = null;
    var listToken = null;
    var i = 0;
    var j = 0;
    var k = 0;
    var count = 0;
    var user = null;
    var certID = null;
    var tokenLabel = null;
    var found = false;
    // Get list of all available tokens (not present = no certificates)
    if ((listToken = CreateTokenList(vars)) != null) {
        // Get a list of certificates accepted by specified server address
        if ((listCert = CreateCertificateList(vars, service)) != null) {
            // Create a user list from those authentication certificates and
            // add eventual corresponding non-repudiation certificate
            for (i = 0; i < listCert.length; i++) {
                if (listCert[i].usage != "64") {
                    // Get token object
                    for (j = 0; j < listToken.length; j++) {
                        if (listToken[j].slotid == listCert[i].slotid) {
                            tokenLabel = listToken[j].label;
                            break;
                        }
                    }
                    // Need token object (and should always be present)
                    if (tokenLabel != null) {
                        // certID for auth certificate
                        certID = listCert[i].certID + "|" + listCert[i].authID;
                        // Try to find matching non-rep certificate
                        found = false;
                        for (j = 0; j < listCert.length; j++) {
                            if ((i != j) && (listCert[j].usage == "64")) {
                                if ((listCert[i].personalNumber == listCert[j].personalNumber) &&
                                    (listCert[i].name == listCert[j].name) &&
                                    (listCert[i].company == listCert[j].company)) {
                                    // Append certID for non-rep certificate
                                    certID += "," + listCert[j].certID + "|" + listCert[j].authID;
                                    // Append token label for non-rep certificate
                                    for (k = 0; k < listToken.length; k++) {
                                        if (listToken[k].slotid == listCert[j].slotid) {
                                            tokenLabel += "," + listToken[k].label;
                                            found = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        // Reuse auth certificate if non-rep not found
                        if (!found) {
                            certID += "," + certID;
                            tokenLabel += "," + tokenLabel;
                        }
                        // Create user object
                        if ((user = new UserInfo(
                            listCert[i].personalNumber,
                            listCert[i].name,
                            listCert[i].company,
                            tokenLabel,
                            certID,
                            null,
                            null,
                            null)) != null) {
                            listUser[count++] = user;
                        }
                    }
                }
            }
        }
    }
    if (listUser.length == 0) {
        listUser = null;
    }
    return listUser;
}
//-----------------------------------------------------------------------------
// GetActiveBySlotID
//-----------------------------------------------------------------------------
function GetActiveBySlotID(vars, list) {
    var slot = null;
    var slotid = 0;
    var i = 0;
    if ((slotid = vars["slotid"]) != null) {
        for (i = 0; i < list.length; i++) {
            if (slotid == list[i].slotid) {
                slot = list[i];
                break;
            }
        }
    }
    return slot;
}
//-----------------------------------------------------------------------------
// GetCurrentLanguage
//-----------------------------------------------------------------------------
function GetCurrentLanguage(vars) {
    var value = null;
    if (vars == null) {
        vars = RestoreAllVariables();
    }
    if (vars != null) {
        if ((value = vars["ProductInfo"]) != null) {
            value = GetPartBy(value, 4, ';');
        }
        else if ((value = vars["language"]) == null) {
            value = vars["Config:Language:Current"];
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// SetCurrentLanguage
//-----------------------------------------------------------------------------
function SetCurrentLanguage(vars) {
    var value = null;
    if ((value = GetCurrentLanguage(vars)) != null) {
        ReloadStringTable(value);
    }
    return;
}
//-----------------------------------------------------------------------------
// ImportTestToken
//-----------------------------------------------------------------------------
function ImportTestToken() {
    CallPluginComponent("none", "importtoken", "", "", "", "");
    return;
}
//-----------------------------------------------------------------------
// CopyClipboard
//-----------------------------------------------------------------------
function CopyClipboard(text) {
    if (HasPluginApp()) {
        document.form1.next.value = "none";
        document.form1.text.value = UnformatHTML(text);
        document.form1.action = "iid://clipboard";
        document.form1.submit();
    }
    return;
}
//-----------------------------------------------------------------------------
// GetServiceAddressList
//-----------------------------------------------------------------------------
function GetServiceAddressList() {
    var value = null;
    if ((value = GetSettingsValue("ServiceAddress")) == null) {
        value = DEFAULT_SERVICE_ADDRESS;
    }
    while ((value != null) && (value.charAt(0) == ';')) {
        value = value.substr(1);
    }
    return value;
}
//-----------------------------------------------------------------------------
// SetServiceAddressList
//-----------------------------------------------------------------------------
function SetServiceAddressList(value) {
    SetSettingsValue("ServiceAddress", value);
    return;
}
//-----------------------------------------------------------------------------
// AddServiceList
//-----------------------------------------------------------------------------
function AddServiceList(service) {
    var value = null;
    if ((value = GetServiceAddressList()) == null) {
        value = "";
    }
    value += PackServiceInfo(service) + ";";
    SetServiceAddressList(value);
    return service;
}
//-----------------------------------------------------------------------------
// RemoveServiceList
//-----------------------------------------------------------------------------
function RemoveServiceList(address) {
    var list = null;
    var i = 0;
    var valueList = "";
    address = address.toLowerCase();
    if ((list = GetServiceList()) != null) {
        for (i = 0; i < list.length; i++) {
            if (list[i].address != address) {
                valueList += PackServiceInfo(list[i]) + ";";
            }
        }
        SetServiceAddressList(valueList);
    }
    return;
}
//-----------------------------------------------------------------------------
// GetServiceList
//-----------------------------------------------------------------------------
function GetServiceList() {
    var list = null;
    var valueList = null;
    var value = null;
    var i = 0;
    var service = null;
    if ((valueList = GetServiceAddressList()) != null) {
        while ((value = GetPartBy(valueList, i++, ';')) != "") {
            if ((service = ParseServiceInfo(value)) != null) {
                if (list == null) {
                    list = new Array();
                }
                list.push(service);
            }
        }
    }
    return list;
}
//-----------------------------------------------------------------------------
// PresentServiceList
//-----------------------------------------------------------------------------
function PresentServiceList(address) {
    var present = false;
    var list = null;
    var i = 0;
    address = address.toLowerCase();
    if ((list = GetServiceList()) != null) {
        for (i = 0; i < list.length; i++) {
            if (list[i].address == address) {
                present = true;
                break;
            }
        }
    }
    return present;
}
//-----------------------------------------------------------------------------
// CreateServiceList
//-----------------------------------------------------------------------------
function CreateServiceList(update) {
    var serviceList = null;
    var valueList = null;
    var i = 0;
    var service = null;
    var updated = false;
    if ((serviceList = GetServiceList()) != null) {
        if (update) {
            for (i=0; i<serviceList.length; i++) {
                if ((service = GetServiceInfo(serviceList[i].address)) != null) {
                    if (PackServiceInfo(service) != PackServiceInfo(serviceList[i])) {
                        serviceList[i] = service;
                        updated = true;
                    }
                }
            }
            if (updated) {
                valueList = "";
                for (i = 0; i < serviceList.length; i++) {
                    valueList += PackServiceInfo(serviceList[i]) + ";";
                }
                SetServiceAddressList(valueList);
            }
        }
    }
    return serviceList;
}
//-----------------------------------------------------------------------------
// FormatVersion
//-----------------------------------------------------------------------------
function FormatVersion(verIn) {
    var verOut = "";
    if (verIn.length == 8) {
        for (i = 0; i < 4; i++) {
            if (!(verIn.charAt(2 * i) == '0'))
                verOut = verOut + verIn.substr(2 * i, 2);
            else
                verOut = verOut + verIn.substr(2 * i + 1, 1);
            if (i < 3)
                verOut = verOut + ".";
        }
        if (verOut.substr(verOut.length - 2, 2) == ".0")
            verOut = verOut.substr(0, verOut.length - 2);
        if (verOut.substr(verOut.length - 2, 2) == ".0")
            verOut = verOut.substr(0, verOut.length - 2);
    }
    return verOut;
}
//-----------------------------------------------------------------------------
// FormatPersonalNumber
//-----------------------------------------------------------------------------
function FormatPersonalNumber(value) {
    var i = 0;
    if (value != null) {
        if ((value.length == 12) || (value.length == 10)) {
            for (i = 0; i < value.length; i++) {
                if ((value[i] < '0') || (value[i] > '9')) {
                    break;
                }
            }
            if (i == value.length) {
                i = value.length - 4;
                value = value.substr(0, i) + "-" + value.substr(i);
            }
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// FormatDate
//-----------------------------------------------------------------------------
function FormatDate(date) {
    var value = null;
    var Y = date.getUTCFullYear();
    var M = date.getUTCMonth() + 1;
    var D = date.getUTCDate();
    var h = date.getUTCHours();
    var m = date.getUTCMinutes();
    var s = date.getUTCSeconds();
    value = Y + "-";
    value += (M <= 9 ? '0' + M : M) + "-";
    value += (D <= 9 ? '0' + D : D) + " ";
    value += (h <= 9 ? '0' + h : h) + ":";
    value += (m <= 9 ? '0' + m : m) + ":";
    value += (s <= 9 ? '0' + s : s) + " UTC";
    return value;
}
//-----------------------------------------------------------------------------
// FormatRDN
//-----------------------------------------------------------------------------
function FormatRDN(value) {
    value = FormatHTML(value);
    value = ReplaceAll(value, ",&nbsp;", "<br />");
    value = ReplaceAll(value, "=", ",");
    value = ReplaceAll(value, ",", " = ");
    return value;
}
//-----------------------------------------------------------------------------
// FormatLinkWeb
//-----------------------------------------------------------------------------
function FormatLinkWeb(text) {
    text = "<a href='javascript:void(0)' onclick='window.open(\"" + text + "\")'>" + text + "</a>";
    return text;
}
//-----------------------------------------------------------------------------
// FormatLinkMail
//-----------------------------------------------------------------------------
function FormatLinkMail(text) {
    var i = 0;
    if ((i = text.indexOf(":")) != -1) {
        text = text.substr(i + 1);
    }
    text = "<a href='mailto:" + text + "'>" + text + "</a>";
    return text;
}
//-----------------------------------------------------------------------------
// FormatLinkCopy
//-----------------------------------------------------------------------------
function FormatLinkCopy(text) {
    text = "<a href='javascript:OnCopy(\"" + text + "\")'>" + text + "</a>";
    return text;
}
//-----------------------------------------------------------------------------
// SetViewTheme
//-----------------------------------------------------------------------------
function SetViewTheme(value) {
    if ((value != null) && (value.length > 0)) {
        SetElementClass("body", value);
    }
    return;
}
//-----------------------------------------------------------------------------
// SetElementValue
//-----------------------------------------------------------------------------
function SetElementValue(id, value) {
    var elem = null;
    if ((id != null) && (value != null)) {
        if ((elem = document.getElementById(id)) != null) {
            elem.innerHTML = value;
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// SetElementLabel
//-----------------------------------------------------------------------------
function SetElementLabel(id, value) {
    SetElementValue(id, FormatHTML(value));
    return;
}
//-----------------------------------------------------------------------------
// GetElementValue
//-----------------------------------------------------------------------------
function GetElementValue(id) {
    var value = null;
    var elem = null;
    if (id != null) {
        if ((elem = document.getElementById(id)) != null) {
            if (elem.value != null) {
                value = elem.value;
            }
            else {
                value = elem.innerHTML;
            }
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// SetElementButton
//-----------------------------------------------------------------------------
function SetElementButton(id, value) {
    var elem = null;
    if ((id != null) && (value != null)) {
        if ((elem = document.getElementById(id)) != null) {
            if (elem.childNodes[0] != null) {
                elem.childNodes[0].nodeValue = value;
            }
            else if (elem.value != null) {
                elem.value = value;
            }
            else {
                elem.innerHTML = value;
            }
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// SetElementImage
//-----------------------------------------------------------------------------
function SetElementImage(id, value, width, height) {
    var elem = null;
    if ((id != null) && (value != null)) {
        if ((elem = document.getElementById(id)) != null) {
            if ((value != null) && (value.length > 0)) {
                // Either from local file store or full path
                if (value.indexOf("/") == -1) {
                    value = "<img src='images/" + value + "'";
                }
                else {
                    value = "<img src='" + value + "'";
                }
                // Set size if available
                if (width != null) {
                    if (height == null) {
                        height = width;
                    }
                    value += " width='" + width + "'";
                    value += " height='" + height + "'";
                }
                value += " alt='' />";
                elem.innerHTML = value;
            }
            else {
                elem.innerHTML = "";
            }
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// SetElementDefaultImage
//-----------------------------------------------------------------------------
function SetElementDefaultImage(id, value) {
    if ((value = MapDefaultImageNameToId(value)) != null) {
        SetElementValue(id, "&nbsp;");
        SetElementClass(id, "tile_logo_" + value);
    }
    return;
}
//-----------------------------------------------------------------------------
// MapDefaultImageNameToId
//-----------------------------------------------------------------------------
function MapDefaultImageNameToId(name) {
    var id = null;
    switch (name) {
        case "empty": id = "01"; break;
        case "token_card": id = "02"; break;
        case "token_soft": id = "03"; break;
        case "support": id = "04"; break;
        case "computer": id = "05"; break;
        case "contact": id = "06"; break;
        case "language": id = "07"; break;
        case "trace": id = "08"; break;
        case "advanced": id = "09"; break;
        case "setup_reset": id = "10"; break;
        case "setup_repair": id = "11"; break;
        case "setup_uninstall": id = "12"; break;
        case "service": id = "13"; break;
        case "certificates": id = "14"; break;
        case "change": id = "15"; break;
        case "unlock": id = "16"; break;
        case "renew": id = "17"; break;
        case "reset": id = "18"; break;
        case "delete": id = "19"; break;
        case "certificate_ds": id = "20"; break;
        case "certificate_nr": id = "21"; break;
        case "certificate_ca": id = "22"; break;
        case "details": id = "23"; break;
        case "open": id = "24"; break;
        case "license": id = "25"; break;
        case "insert_token": id = "26"; break;
    }
    return id;
}
//-----------------------------------------------------------------------------
// SetElementClass
//-----------------------------------------------------------------------------
function SetElementClass(id, name) {
    var elem = null;
    if ((elem = document.getElementById(id)) != null) {
        elem.setAttribute("class", name);
        elem.setAttribute("className", name);
    }
    return;
}
//-----------------------------------------------------------------------------
// GetElementClass
//-----------------------------------------------------------------------------
function GetElementClass(id) {
    var name = null;
    var elem = null;
    if ((elem = document.getElementById(id)) != null) {
        name = elem.getAttribute("className");
    }
    return name;
}
//-----------------------------------------------------------------------------
// SetElementVisible
//-----------------------------------------------------------------------------
function SetElementVisible(id) {
    SetElementClass(id, "visible");
    return;
}
//-----------------------------------------------------------------------------
// SetElementHidden
//-----------------------------------------------------------------------------
function SetElementHidden(id) {
    SetElementClass(id, "hidden");
    return;
}
//-----------------------------------------------------------------------------
// SetEditFieldDigits
//-----------------------------------------------------------------------------
function SetEditFieldDigits(id, type) {
    var elem = null;
    var name = null;
    var value = null;
    if ((elem = document.getElementById(id)) != null) {
        name = GetPartBy(type, 0, '=');
        value = GetPartBy(type, 1, '=');
        if ((name.length > 0) && (value.length > 0)) {
            elem.setAttribute(name, value);
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// ShowHeader
//-----------------------------------------------------------------------------
function ShowHeader() {
    if (GetCustomFlagUseTextButtons()) {
        SetElementLabel("text_btn_about", GetString("BUTTON_ABOUT"));
        SetElementLabel("text_btn_back", GetString("BUTTON_BACK"));
        SetElementLabel("text_btn_cancel", GetString("BUTTON_CANCEL"));
        SetElementLabel("text_btn_refresh", GetString("BUTTON_REFRESH"));
        SetElementVisible("header_btn_txt");
        SetElementClass("title", "header_text");
    }
    else {
        SetElementVisible("header_btn_img");
        SetElementClass("title", "header_button");
    }
    UpdatePasswordFields();
    return;
}
//-----------------------------------------------------------------------------
// ShowElementList
//-----------------------------------------------------------------------------
function ShowElementList(view, elements) {
    var i = 0;
    var element = null;
    while ((element = GetPartBy(elements, i, ';')) != "") {
        if ((view != null) && (view.length > 0) && ((ConvertNumber(view) & (0x01 << i)) == 0)) {
            SetElementHidden(element);
        }
        else {
            SetElementVisible(element);
        }
        i++;
    }
    return;
}
//-----------------------------------------------------------------------------
// IsElementVisible
//-----------------------------------------------------------------------------
function IsElementVisible(view, index) {
    var visible = true;
    if ((view != null) && (view.length > 0)) {
        if ((ConvertNumber(view) & (0x01 << index)) == 0) {
            visible = false;
        }
    }
    return visible;
}
//-----------------------------------------------------------------------------
// ProductInformation
//-----------------------------------------------------------------------------
function ProductInformation(productInfo) {
    var value = null;
    var value2 = null;
    // Product information
    this.productName = GetPartBy(productInfo, 0, ';');
    this.productCompany = GetPartBy(productInfo, 1, ';');
    this.productVersion = null;
    // Product version
    if ((value = GetPartBy(productInfo, 2, ';')) != "") {
        this.productVersion = FormatVersion(GetPartBy(value, 0, ','));
        value2 = GetPartBy(value, 1, ',');
        if (value2.length > 0) {
            this.productVersion += " " + value2;
        }
        value2 = GetPartBy(value, 2, ',');
        if (value2.length > 0) {
            this.productVersion += ", " + value2;
        }
    }
    // License information
    this.customName = null;
    this.customCompany = null;
    this.licenseValid = true;
    if ((value = GetPartBy(productInfo, 3, ';')) != "") {
        this.customName = URL.decode(GetPartBy(value, 0, ','));
        this.customCompany = URL.decode(GetPartBy(value, 1, ','));
        if ((value2 = GetPartBy(value, 2, ',')) != "") {
            if (value2 != "true") {
                this.licenseValid = false;
            }
        }
        if (this.customCompany == this.productCompany) {
            this.customCompany = "";
        }
    }
    // Language information
    if ((this.language = GetPartBy(productInfo, 4, ';')) != "") {
        LoadStringTable(this.language);
    }
    // Custom information
    this.custom = false;
    if ((value = GetPartBy(productInfo, 5, ';')) != "") {
        this.custom = GetCustomFlagUseCustomerImage();
        if (GetPartBy(value, 0, ',') != "") {
            this.customName = URL.decode(GetPartBy(value, 0, ','));
        }
        if ((value2 = URL.decode(GetPartBy(value, 1, ','))) != "") {
            this.customVersion = FormatVersion(value2);
            if ((value2 = URL.decode(GetPartBy(value, 2, ','))) != "") {
                this.customVersion += " " + value2;
            }
        }
    }
}
//-----------------------------------------------------------------------------
// SetProductInformation
//-----------------------------------------------------------------------------
function SetProductInformation(productInfo) {
    var value = null;
    var value2 = null;
    var version = null;
    var productName = null;
    var productCompany = null;
    var productVersion = null;
    var customName = null;
    var customCompany = null;
    var customVersion = null;
    var licenseValid = true;
    var custom = false;
    // Product information
    productName = GetPartBy(productInfo, 0, ';');
    productCompany = GetPartBy(productInfo, 1, ';');
    // Product version
    if ((value = GetPartBy(productInfo, 2, ';')) != "") {
        productVersion = FormatVersion(GetPartBy(value, 0, ','));
        value2 = GetPartBy(value, 1, ',');
        if (value2.length > 0) {
            productVersion += " " + value2;
        }
        value2 = GetPartBy(value, 2, ',');
        if (value2.length > 0) {
            productVersion += ", " + value2;
        }
    }
    // License information
    if ((value = GetPartBy(productInfo, 3, ';')) != "") {
        customName = URL.decode(GetPartBy(value, 0, ','));
        customCompany = URL.decode(GetPartBy(value, 1, ','));
        if ((value2 = GetPartBy(value, 2, ',')) != "") {
            if (value2 != "true") {
                licenseValid = false;
            }
        }
        if (customCompany == productCompany) {
            customCompany = "";
        }
    }
    // Language information
    if ((value = GetPartBy(productInfo, 4, ';')) != "") {
        LoadStringTable(value);
    }
    // Custom information
    if ((value = GetPartBy(productInfo, 5, ';')) != "") {
        SetCustomFlags(GetPartBy(value, 3, ','));
        custom = GetCustomFlagUseCustomerImage();
        if (GetPartBy(value, 0, ',') != "") {
            customName = URL.decode(GetPartBy(value, 0, ','));
        }
        if ((value2 = URL.decode(GetPartBy(value, 1, ','))) != "") {
            customVersion = FormatVersion(value2);
            if ((value2 = URL.decode(GetPartBy(value, 2, ','))) != "") {
                customVersion += " " + value2;
            }
        }
    }
    // Set parsed information
    SetProductInformationArea(custom, productName, productCompany, productVersion, customName, customCompany, customVersion, licenseValid);
    return;
}
//-----------------------------------------------------------------------------
// SetProductInformationArea
//-----------------------------------------------------------------------------
function SetProductInformationArea(custom, productName, productCompany, productVersion, customName, customCompany, customVersion, licenseValid) {
    var value = null;
    if (customVersion == null) {
        customVersion = productVersion;
    }
    // There are two areor, one with "big" image and one with "small"
    // image. Standard behavior will Net iD be part of "big" area, and
    // license information part of "small" area. For some custom package
    // will we allow focus to be set for customer information.
    if (custom) {
        // Big area
        SetElementImage("image", "assets/custom/tile_logo2.png");
        if (customName.length > 0) {
            SetElementLabel("name", customName);
        }
        else {
            SetElementHidden("name");
        }
        if (customCompany.length > 0) {
            SetElementLabel("text", customCompany);
        }
        else {
            SetElementHidden("text");
        }
        if (customVersion.length > 0) {
            SetElementLabel("info", customVersion);
        }
        else {
            SetElementHidden("info");
        }
        // Small area
        if ((value = GetImageByName(productCompany, false)) != null) {
            SetElementImage("image0", value);
        }
        else {
            SetElementImage("image0", "assets/images/tile_logo.png");
        }
        SetElementLabel("name0", productName);
        SetElementValue("text0", "Copyright &copy; " + productCompany);
        SetElementValue("info0", productVersion);
    }
    else {
        // Big area
        SetElementLabel("name", productName);
        SetElementValue("text", "Copyright &copy; " + productCompany);
        SetElementValue("info", productVersion);
        // Small area
        if ((value = GetImageByName(customCompany, false)) != null) {
            SetElementImage("image0", value);
        }
        else {
            SetElementDefaultImage("image0", "license");
        }
        if (customName.length > 0) {
            SetElementLabel("name0", customName);
        }
        else {
            SetElementHidden("name0");
        }
        if (customCompany.length > 0) {
            SetElementLabel("text0", customCompany);
        }
        else {
            SetElementHidden("text0");
        }
        if (customVersion.length > 0) {
            SetElementLabel("info0", customVersion);
        }
        else {
            SetElementHidden("info0");
        }
    }
    /* REMOVED - Not correct handled in Setup GUI
        if (!licenseValid) {
            SetElementClass("license", "tile_license_01");
            SetElementLabel("info0", GetString("TEXT_INVALID_LICENSE"));
        }
        else {
            if (productVersion == customVersion) {
                SetElementHidden("info0");
            }
        }
    */
    return;
}
//-----------------------------------------------------------------------------
// CreateElemList
//-----------------------------------------------------------------------------
function CreateElemList(count, type, title) {
    var elem = null;
    var innerHTML = "";
    var value = "";
    var i = 0;
    if ((elem = document.getElementById("elem0")) != null) {
        for (i = 0; i < count; i++) {
            value = elem.innerHTML;
            value = value.replace("elem0", type + i);
            value = value.replace("view0", type + i);
            value = value.replace("image0", type + "image" + i);
            value = value.replace("name0", type + "name" + i);
            value = value.replace("text0", type + "text" + i);
            value = value.replace("info0", type + "info" + i);
            value = value.replace("id0", type + "id" + i);
            value = "<div id='" + type + i + "'>" + value + "</div>";
            innerHTML += value;
        }
        if (title) {
            if ((elem = document.getElementById("head0")) != null) {
                value = elem.innerHTML;
                value = value.replace("title0", type + "title");
                innerHTML = value + innerHTML;
            }
        }
        if ((elem = document.getElementById(type + "s")) != null) {
            elem.innerHTML = innerHTML;
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// RemoveElemList
//-----------------------------------------------------------------------------
function RemoveElemList(type) {
    var elem = null;
    if ((elem = document.getElementById(type + "s")) != null) {
        elem.innerHTML = "";
    }
    return;
}
//-----------------------------------------------------------------------------
// StripParenthesisPart
//-----------------------------------------------------------------------------
function StripParenthesisPart(text) {
    if (text.indexOf('(') > 0)
        text = text.substr(0, text.indexOf('(') - 1);
    return text;
}
//-----------------------------------------------------------------------------
// GetParenthesisPart
//-----------------------------------------------------------------------------
function GetParenthesisPart(text) {
    var i = 0;
    i = text.indexOf('(');
    if (i > 0)
        text = text.substr(text.indexOf('(') + 1, text.length - i - 2);
    else
        text = "";
    return text;
}
//-----------------------------------------------------------------------------
// GetRequestString
//-----------------------------------------------------------------------------
function GetRequestString(type) {
    return GetString("TEXT_" + type.toUpperCase());
}
//-----------------------------------------------------------------------------
// SetFieldFocus
//-----------------------------------------------------------------------------
function SetFieldFocus(id) {
    var elem = null;
    if (ShouldAutoFocusEditField()) {
        if ((elem = document.getElementById(id)) != null) {
            elem.focus();
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// SetFieldUnfocus
//-----------------------------------------------------------------------------
function SetFieldUnfocus(id) {
    var elem = null;
    if (ShouldAutoFocusEditField()) {
        if ((elem = document.getElementById(id)) != null) {
            elem.blur();
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// OnEditChange
//-----------------------------------------------------------------------------
function OnEditChange() {
    var elem = null;
    var i = 0;
    for (i = 0; i <= 4; i++) {
        switch (i) {
            case 0: id = "pwd"; break;
            case 1: id = "pwd_old"; break;
            case 2: id = "pwd_new"; break;
            case 3: id = "pwd_confirm"; break;
            case 4: id = "address"; break;
        }
        if ((elem = document.getElementById(id)) != null) {
            if (elem.value.length == 0) {
                SetElementClass(id, "label_blur");
            }
            else {
                SetElementClass(id, "label_focus");
            }
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// OnDetectReturnKey
//-----------------------------------------------------------------------------
function OnDetectReturnKey(event) {
    // Look for window.event in case event isn't passed in
    if (typeof event == 'undefined' && window.event) {
        event = window.event;
    }
    if (event.keyCode == 13) {
        OnButtonOK();
    }
    return;
}
//-----------------------------------------------------------------------------
// SetDetectReload
//-----------------------------------------------------------------------------
function SetDetectReload() {
    document.body.onkeydown = OnDetectReload;
    return;
}
//-----------------------------------------------------------------------------
// OnDetectReload
//-----------------------------------------------------------------------------
function OnDetectReload(event) {
    var success = true;
    // Look for window.event in case event isn't passed in
    if (typeof event == 'undefined' && window.event) {
        event = window.event;
    }
    // Capture F5
    if (event.keyCode == 116) {
        OnReload();
        success = false;
    }
    return success;
}
//-----------------------------------------------------------------------------
// MayUpdateTokens
//-----------------------------------------------------------------------------
function MayUpdateTokens() {
    var may = false;
    var info = null;
    // PIN dialog not available yet for iOS/Android
    if ((info = new DeviceInfo(null)) != null) {
        if ((info.device.name.indexOf("Windows") != -1) ||
            (info.device.name.indexOf("Macintosh") != -1) ||
            (info.device.name.indexOf("Linux") != -1)) {
            may = true;
        }
    }
    return may;
}
//-----------------------------------------------------------------------------
// HideProgress
//-----------------------------------------------------------------------------
function HideProgress() {
    // Either progress popup or replace button with progress icon
    if (document.getElementById("progress") != null) {
        // Progress popup
        SetElementClass("progress", "progress_hide");
    }
    else {
        // Replace button with progress icon
        SetElementHidden("progress_on");
        SetElementVisible("progress_off");
    }
    return;
}
//-----------------------------------------------------------------------------
// ShowProgress
//-----------------------------------------------------------------------------
function ShowProgress() {
    var elem = null;
    var value = null;
    // Either progress popup or replace button with progress icon
    if (document.getElementById("progress") != null) {
        // Progress popup
        SetElementClass("progress", "progress_show");
    }
    else {
        // Replace button with progress icon
        SetElementHidden("progress_off");
        SetElementVisible("progress_on");
        // Resolve IE bug
        if ((elem = document.getElementById("loading")) != null) {
            value = elem.innerHTML;
            elem.innerHTML = "";
            elem.innerHTML = value;
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// HidePopup
//-----------------------------------------------------------------------------
function HidePopup() {
    SetElementClass("popup", "popup_hide");
    SetFieldFocus("btn_ok");
    SetFieldFocus("pwd_old");
    SetFieldFocus("pwd");
    return;
}
//-----------------------------------------------------------------------------
// ShowPopup
//-----------------------------------------------------------------------------
function ShowPopup(type) {
    SetElementClass("popup_type", type);
    SetElementClass("popup", "popup_show");
    SetFieldFocus("popup_btn_ok");
    SetFieldUnfocus("popup_btn_ok");
    // Disable until know how to clear enter key event, will cause dialog to
    // close direct if opened by enter key
    /*
    if (type == "confirm") {
        SetFieldFocus("popup_btn_cancel");
    }
    else {
        SetFieldFocus("popup_btn_ok");
    }
    */
    return;
}
//-----------------------------------------------------------------------------
// ShowMessage
//-----------------------------------------------------------------------------
function ShowMessage(text) {
    SetElementValue("popup_text", text);
    SetElementValue("popup_info", "");
    document.form1.popup_btn_ok.value = GetString("BUTTON_OK");
    document.form1.popup_btn_ok.onclick = function() { HidePopup(); };
    SetElementVisible("popup_btn_ok");
    SetElementHidden("popup_btn_cancel");
    ShowPopup("info");
    return;
}
//-----------------------------------------------------------------------------
// ShowError
//-----------------------------------------------------------------------------
function ShowError(text, info) {
    SetElementValue("popup_text", text);
    SetElementValue("popup_info", info);
    document.form1.popup_btn_ok.value = GetString("BUTTON_OK");
    document.form1.popup_btn_ok.onclick = function() { HidePopup(); };
    SetElementVisible("popup_btn_ok");
    SetElementHidden("popup_btn_cancel");
    ShowPopup("warning");
    return;
}
//-----------------------------------------------------------------------------
// ShowConfirm
//-----------------------------------------------------------------------------
function ShowConfirm(text, info, Callback, arg) {
    SetElementValue("popup_text", text);
    SetElementValue("popup_info", info);
    SetElementVisible("popup_btn_ok");
    SetElementVisible("popup_btn_cancel");
    document.form1.popup_btn_ok.value = GetString("BUTTON_YES");
    document.form1.popup_btn_ok.onclick = function() { Callback(arg); HidePopup(); };
    document.form1.popup_btn_cancel.value = GetString("BUTTON_NO");
    document.form1.popup_btn_cancel.onclick = function() { HidePopup(); };
    ShowPopup("confirm");
    return;
}
//-----------------------------------------------------------------------------
// ShowErrorPkcs11
//-----------------------------------------------------------------------------
function ShowErrorPkcs11(rv) {
    var text = null;
    var info = null;
    text = GetErrorString(rv);
    info = GetPartBy(text, 1, '|');
    text = GetPartBy(text, 0, '|');
    ShowError(text, info);
    return;
}
