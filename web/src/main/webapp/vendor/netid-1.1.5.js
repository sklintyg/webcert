/******************************************************************************
Net iD Javascript API
 
@version: 1.1.5 - 2016-03-02
@copyright: SecMaker AB (http://www.secmaker.com/)

This script should work with all versions of Net iD plugin. The intention is
to hide all problems with different web-browser versions and/or OS platforms. 
The only supported functions are:

- iid_IsAvailable
- iid_GetProperty
- iid_SetProperty
- iid_EnumProperty
- iid_Invoke

Everything else may/will change with future updates. An update will replace the 
existing file with a new version and only the functions listed above will 
be guaranteed to exist. 

The function iid_IsAvailable is used for detection of the plugin, the other 
functions are documented in Net iD Enterprise Developers Guide.

USAGE: Add this script and start using the functions above:

<script type="text/javascript" src="_netid.js"></script>
 
ERROR: If the functions above isn't loading correct, i.e. new version of IE, 
contact support@secmaker.com for an updated version of this script.

NOTE: The plugin object must not be 'hidden', i.e. display:none, since some
web-browsers will not allow it to execute while hidden. The default plugin 
object will be created with 0px width/height at bottom of page within a 'div'
using the object id from constant IID_NAME_PLACE. If you get presentation
problem with this approach, you may declare a 'div' with that id somewhere else 
and it will be used, since this place holder 'div' is only created when it is 
missing. 

DISCLAIMER: The script is provided "as is" and SecMaker expressly disclaims any 
warranties, including as regards fitness for purpose, freedom from errors and 
bugs or that defects in the script will be corrected. 
******************************************************************************/

//-----------------------------------------------------------------------------
// Constants
//-----------------------------------------------------------------------------
var IID_NAME_APP = "Net iD";
var IID_NAME_OBJECT = "netid_object";
var IID_NAME_PLACE = "netid_place_holder";
//-----------------------------------------------------------------------------
// Globals
//-----------------------------------------------------------------------------
var IID_DEVICE_INFO = null;
var IID_AVAILABLE_CHECK = false;
var IID_JS_INTERFACE = null;
var IID_JS_BRIDGE = null;
var IID_JS_RESPONSE = null;
var IID_NO_STORAGE = false;
var IID_INDEX_AS_STRING = null;
//-----------------------------------------------------------------------------
// iid_SetProperty
//-----------------------------------------------------------------------------
function iid_SetProperty(name, value) {
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            if (!iid_IgnoreProperty(name)) {
                iid.SetProperty(name, value);
            }
        }
        catch (ex) {
        }
    }
    return;
}
//-----------------------------------------------------------------------------
// iid_GetProperty
//-----------------------------------------------------------------------------
function iid_GetProperty(name) {
    var value = "";
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            if ((value = iid.GetProperty(name)) == null) {
                value = "";
            }
        }
        catch (ex) {
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// iid_EnumProperty
//-----------------------------------------------------------------------------
function iid_EnumProperty(name, index) {
    var value = "";
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            if (index == null) {
                index = "0";
            }
            if (typeof index != "string") {
                index = index.toString();
            }
            if (iid_IndexAsInteger()) {
                index = parseInt(index);
            }
            if ((value = iid.EnumProperty(name, index)) == null) {
                value = "";
            }
        }
        catch (ex) {
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// iid_Invoke
//-----------------------------------------------------------------------------
function iid_Invoke(name) {
    var rv = 0;
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            rv = parseInt(iid.Invoke(name));
        }
        catch (ex) {
        }
    }
    return rv;
}
//-----------------------------------------------------------------------------
// iid_Application
//-----------------------------------------------------------------------------
function iid_Application(command, status, response) {
    var iid = null;
    try {
        if ((iid = iid_GetObject()) != null) {
            if (typeof iid.Application == "function") {
                iid.Application(command, status, response);
            }
            else {
                iid = null;
            }
        }
        if (iid == null) {
            if (typeof document.form1 == "object") {
                if ((status != null) && (typeof document.form1.status == "object")) {
                    document.form1.status.value = status;
                }
                if ((response != null) && (typeof document.form1.response == "object")) {
                    document.form1.response.value = response;
                }
                document.form1.action = "iid://" + command;
                document.form1.submit();
            }
        }
    }
    catch (ex) {
    }
    return;
}
//-----------------------------------------------------------------------
// iid_IgnoreProperty
//-----------------------------------------------------------------------
function iid_IgnoreProperty(name) {
    var ignore = false;
    if (iid_IsDeviceType("Android")) {
        if (name.toLowerCase() == "invokethread") {
            ignore = true;
        }
    }
    return ignore;
}
//-----------------------------------------------------------------------
// iid_GetDeviceInfo
//-----------------------------------------------------------------------
function iid_GetDeviceInfo() {
    if (IID_DEVICE_INFO == null) {
        IID_DEVICE_INFO = new iid_DeviceInfo(null);
    }
    return IID_DEVICE_INFO;
}
//-----------------------------------------------------------------------
// iid_IsExplorer
//-----------------------------------------------------------------------
function iid_IsExplorer() {
    var explorer = false;
    var info = null;
    if ((info = iid_GetDeviceInfo()) != null) {
        explorer = info.browser.explorer;
    }
    return explorer;
}
//-----------------------------------------------------------------------
// iid_IsDeviceType
//-----------------------------------------------------------------------
function iid_IsDeviceType(types) {
    var found = false;
    var info = null;
    var i = 0;
    var type = null;
    if ((info = iid_GetDeviceInfo()) != null) {
        while ((type = iid_GetPartBy(types, i, ';')) != "") {
            if (info.device.name.indexOf(type) != -1) {
                found = true;
                break;
            }
            i++;
        }
    }
    return found;
}
//-----------------------------------------------------------------------
// iid_IsAvailable
//-----------------------------------------------------------------------
function iid_IsAvailable() {
    var available = false;
    var explorer = false;
    var name = null;
    var elem = null;
    // At first call mark that available check is completed
    IID_AVAILABLE_CHECK = true;
    // Always start check for Javascript interface
    if (iid_HasJavascriptInterface()) {
        available = true;
    }
    // follow with check for Javascript bridge
    else if (iid_HasJavascriptBridge()) {
        available = true;
    }
    else {
        // Always use same div id as place holder for create of plugin
        name = IID_NAME_PLACE;
        if (document.getElementById(name) == null) {
            // Not declared, so add our special tag last in body
            if (!iid_SkipDeclare()) {
                elem = document.createElement("div");
                elem.setAttribute("id", name);
                document.body.appendChild(elem);
            }
        }
        // Start checking already created
        if (iid_GetObject() != null) {
            available = true;
        }
        // Require place holder defined (used to declare plugin object)
        else if (document.getElementById(name) != null) {
            explorer = iid_IsExplorer();
            // First try Net iD Enterprise plugin
            if (!(available = iid_Declare(name, explorer, false))) {
                // Second try Net iD Live plugin (only explorer)
                if (explorer) {
                    available = iid_Declare(name, explorer, true);
                }
            }
        }
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_HasApplication
//-----------------------------------------------------------------------
function iid_HasApplication() {
    var available = false;
    if (navigator.userAgent.indexOf(IID_NAME_APP) != -1) {
        available = true;
    }
    else if (iid_IsDeviceType("Android;iOS;iPhone;iPad")) {
        available = true;
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_HasJavascriptInterface
//-----------------------------------------------------------------------
function iid_HasJavascriptInterface() {
    var available = false;
    if (iid_HasApplication()) {
        if (iid_IsDeviceType("Android")) {
            if (window.JSInterface != null) {
                if ((window.JSInterface.SetProperty != null) &&
                    (window.JSInterface.GetProperty != null) &&
                    (window.JSInterface.EnumProperty != null) &&
                    (window.JSInterface.Invoke != null)) {
                    available = true;
                    IID_JS_INTERFACE = window.JSInterface;
                }
            }
        }
        else if (iid_IsDeviceType("Windows")) {
            if (typeof window.external == "object") {
                if ((window.external.SetProperty != null) &&
                    (window.external.GetProperty != null) &&
                    (window.external.EnumProperty != null) &&
                    (window.external.Invoke != null)) {
                    available = true;
                    IID_JS_INTERFACE = window.external;
                }
            }
        }
        else if (iid_IsDeviceType("Mac")) {
            if (typeof window.NetiD == "object") {
                if ((window.NetiD.SetProperty != null) &&
                    (window.NetiD.GetProperty != null) &&
                    (window.NetiD.EnumProperty != null) &&
                    (window.NetiD.Invoke != null)) {
                    available = true;
                    IID_JS_INTERFACE = window.NetiD;
                }
            }
        }
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_GetJavascriptObject
//-----------------------------------------------------------------------
function iid_GetJavascriptObject() {
    var obj = null;
    if (typeof NetiD == "object") {
        if ((typeof NetiD.SetProperty == "function") &&
            (typeof NetiD.GetProperty == "function") &&
            (typeof NetiD.EnumProperty == "function") &&
            (typeof NetiD.Invoke == "function")) {
            obj = NetiD;
        }
    }
    return obj;
}
//-----------------------------------------------------------------------
// iid_HasJavascriptBridge
//-----------------------------------------------------------------------
function iid_HasJavascriptBridge() {
    var available = false;
    if (iid_HasApplication()) {
        if (iid_IsDeviceType("iOS;iPhone;iPad")) {
            if (IID_JS_BRIDGE == null) {
                IID_JS_BRIDGE = new IID_JavascriptBridge();
            }
            if (IID_JS_BRIDGE != null) {
                available = IID_JS_BRIDGE.Available();
            }
        }
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_GetDeclare
//-----------------------------------------------------------------------
function iid_GetDeclare(explorer, live) {
    var value = "";
    var clsid = "";
    var name = "";
    if (live) {
        name = "application/x-iid-live"
        clsid = "5BF56AD2-E297-416E-BC49-00B327C4428E";
    }
    else {
        name = "application/x-iid"
        clsid = "5BF56AD2-E297-416E-BC49-00B327C4426E";
    }
    if (explorer) {
        value = "<object name='" + IID_NAME_OBJECT + "' id='" + IID_NAME_OBJECT + "' classid='CLSID:";
        value += clsid;
        value += "' width='0' height='0'></object>";
    }
    else {
        value = "<object name='" + IID_NAME_OBJECT + "' id='" + IID_NAME_OBJECT + "' type='";
        value += name;
        value += "' width='0' height='0'></object>";
    }
    return value;
}
//-----------------------------------------------------------------------
// iid_GetObject
//-----------------------------------------------------------------------
function iid_GetObject() {
    var obj = null;
    if (!IID_AVAILABLE_CHECK) {
        iid_IsAvailable();
    }
    if ((obj = iid_GetJavascriptObject()) == null) {
        if (iid_HasJavascriptInterface()) {
            obj = IID_JS_INTERFACE;
        }
        else if (iid_HasJavascriptBridge()) {
            obj = IID_JS_BRIDGE;
        }
        else {
            obj = document.getElementById(IID_NAME_OBJECT);
        }
    }
    return obj;
}
//-----------------------------------------------------------------------
// iid_Declare
//-----------------------------------------------------------------------
function iid_Declare(name, explorer, live) {
    var success = false;
    var iid_place = null;
    var version = 0;
    iid_place = document.getElementById(name);
    if (iid_place != null) {
        // Get object declaration
        iid_place.innerHTML = iid_GetDeclare(explorer, live);
        // Require plugin object return something useful
        version = iid_GetProperty("Version");
        if ((version != null) && (version.length > 0)) {
            success = true;
        }
    }
    return success;
}
//-----------------------------------------------------------------------
// iid_SkipDeclare
//-----------------------------------------------------------------------
function iid_SkipDeclare() {
    skip = false;
    try {
        // Not allowed for setup
        if (window.location.href.indexOf("setup.html") != -1) {
            skip = true;
        }
    }
    catch (ex) {
    }
    return skip;
}
//-----------------------------------------------------------------------------
// iid_IndexAsInteger
//-----------------------------------------------------------------------------
function iid_IndexAsInteger() {
    if (IID_INDEX_AS_STRING == null) {
        if (iid_IsDeviceType("Android") && (iid_GetProperty("Version") < "06050012")) {
            IID_INDEX_AS_STRING = "true";
        }
        else {
            IID_INDEX_AS_STRING = "false";
        }
    }
    return (IID_INDEX_AS_STRING == "true");
}
//-----------------------------------------------------------------------------
// Javascript bridge 
//-----------------------------------------------------------------------------
function iid_GetJavascriptBridgeResponseValue() {
    var value = null;
    try {
        if (IID_NO_STORAGE) {
            value = IID_JS_RESPONSE;
        }
        else if (typeof localStorage == "object") {
            if (localStorage.getItem != null) {
                value = localStorage.getItem("iid_JavascriptBridgeResponseValue");
            }
        }
    }
    catch (ex) {
    }
    return value;
}
function iid_SetJavascriptBridgeResponseValue(value) {
    try {
        if (IID_NO_STORAGE) {
            IID_JS_RESPONSE = value;
        }
        else if (typeof localStorage == "object") {
            if (localStorage.setItem != null) {
                localStorage.setItem("iid_JavascriptBridgeResponseValue", value);
            }
        }
    }
    catch (ex) {
    }
    return;
}
function iid_JavascriptBridgeResponse(result) {
    return iid_SetJavascriptBridgeResponseValue(IID_URL.decode(result));
}
function IID_JavascriptBridge() {
    // Members
    this._available = false;
    this._count = 0;
    // Function Available
    this.Available = function() {
        if (this._available) {
            this._count += 1;
        }
        else {
            this._available = true;
            if (this.GetProperty("Version") == null) {
                this._available = false;
            }
        }
        return this._available;
    }
    // Function GetProperty
    this.GetProperty = function(name) {
        var result = null;
        if (this.Available()) {
            result = this.Send("GetProperty", name, null, null);
        }
        return result;
    }
    // Function SetProperty
    this.SetProperty = function(name, value) {
        var result = null;
        if (this.Available()) {
            result = this.Send("SetProperty", name, null, value);
        }
        return result;
    }
    // Function EnumProperty
    this.EnumProperty = function(name, index) {
        var result = null;
        if (this.Available()) {
            result = this.Send("EnumProperty", name, index, null);
        }
        return result;
    }
    // Function Invoke
    this.Invoke = function(name) {
        var result = null;
        if (this.Available()) {
            result = this.Send("Invoke", name, null, null);
        }
        return result;
    }
    // Function Send
    this.Send = function(func, name, index, value) {
        var result = null;
        var info = "";
        var iframe = null;
        try {
            if ((func != null) && (name != null)) {
                if (index == null) {
                    index = "";
                }
                if (value == null) {
                    value = "";
                }
                if ((iframe = document.createElement("iframe")) != null) {
                    info = "iidjs://?count=" + this._count;
                    info += "&func=" + func;
                    info += "&name=" + IID_URL.encode(name);
                    info += "&index=" + index;
                    info += "&value=" + IID_URL.encode(value);
                    info += "&response=iid_JavascriptBridgeResponse";
                    iframe.src = info;
                    iframe.style.display = "none";
                    iframe.style.visibility = "hidden";
                    iframe.name = "iid_JavascriptBridgeFrame";
                    iframe.id = "iid_JavascriptBridgeFrame";
                    iframe.width = 0;
                    iframe.height = 0;
                    document.getElementsByTagName("body")[0].appendChild(iframe);
                    iframe.parentNode.removeChild(iframe);
                    iframe = null;
                    result = iid_GetJavascriptBridgeResponseValue();
                }
            }
        }
        catch (ex) {
        }
        return result;
    }
}
//-----------------------------------------------------------------------------
// iid_NameVersionInfo
//-----------------------------------------------------------------------------
function iid_NameVersionInfo(name, version) {
    this.name = name;
    this.version = version;
    this.explorer = (name.indexOf("MSIE") >= 0);
    this.language = (window.navigator.userLanguage || window.navigator.language);
}
//-----------------------------------------------------------------------------
// iid_ParseBrowserInfo
//-----------------------------------------------------------------------------
function iid_ParseBrowserInfo(value) {
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
    return new iid_NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// iid_ParseDeviceInfo
//-----------------------------------------------------------------------------
function iid_ParseDeviceInfo(value) {
    var name = null;
    var version = null;
    var i = 0;
    var j = 0;
    var k = 0;
    var part = null;
    // For internal Net iD WebApp may name and version be stored after Net iD
    // name/version info.
    if (((i = value.indexOf(IID_NAME_APP)) > 0) &&
        ((i = value.indexOf(" ", i + IID_NAME_APP.length + 2)) > 0)) {
        part = value.substr(i + 1);
        if ((i = part.indexOf("/")) > 0) {
            name = part.substr(0, i);
            version = part.substr(i + 1);
        }
    }
    if ((name == null) || (version == null)) {
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
        while ((part = iid_GetPartBy(value, i, ';')) != "") {
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
            // Linux only name no version
            else if ((j = part.indexOf("Linux")) >= 0) {
                name = "Linux";
            }
            i++;
        }
    }
    // Need default value
    if (name == null) {
        name = navigator.platform;
    }
    if (version == null) {
        version = "0.0";
    }
    // Create name version info object
    return new iid_NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// iid_DeviceInfo
//-----------------------------------------------------------------------------
function iid_DeviceInfo(info) {
    if (info == null) {
        info = navigator.userAgent;
    }
    // Get os name and version
    this.device = iid_ParseDeviceInfo(info);
    // Get browser name and version
    this.browser = iid_ParseBrowserInfo(info);
}
//-----------------------------------------------------------------------------
// iid_GetPartBy
//-----------------------------------------------------------------------------
function iid_GetPartBy(text, pos, c) {
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
// URL converter
//-----------------------------------------------------------------------------
var IID_URL = {
    encode: function(value) {
        if (value != null) {
            // Replace all '+' with '%2B' to avoid problem with unescape
            value = ReplaceAll(value, "+", "___OOO___OOO___");
            // Convert all
            value = escape(value);
            // Set URL coding for '+'
            value = ReplaceAll(value, "___OOO___OOO___", "%2B");
        }
        return value;
    },
    decode: function(value) {
        if (value != null) {
            // '+' are not removed by unescape, so start with replacing them
            value = ReplaceAll(value, "+", "%20");
            // Convert all
            value = unescape(value);
        }
        return value;
    }
}
