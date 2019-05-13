//-----------------------------------------------------------------------------
// UTF8 converter
//-----------------------------------------------------------------------------
var UTF8 = {
    encode: function(string) {
        var utftext = "";
        var n = 0;
        var k = 0;
        var c = 0;
        if (string != null) {
            string = string.replace(/\r\n/g, "\n");
            for (n = 0, k = string.length; n < k; n++) {
                c = string.charCodeAt(n);
                if (c < 128) {
                    utftext += String.fromCharCode(c);
                }
                else if ((c > 127) && (c < 2048)) {
                    utftext += String.fromCharCode((c >> 6) | 192);
                    utftext += String.fromCharCode((c & 63) | 128);
                }
                else {
                    utftext += String.fromCharCode((c >> 12) | 224);
                    utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                    utftext += String.fromCharCode((c & 63) | 128);
                }
            }
        }
        return utftext;
    },
    decode: function(utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        if (utftext != null) {
            while (i < utftext.length) {
                c = utftext.charCodeAt(i);
                if (c < 128) {
                    string += String.fromCharCode(c);
                    i++;
                }
                else if ((c > 191) && (c < 224)) {
                    c2 = utftext.charCodeAt(i + 1);
                    string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                    i += 2;
                }
                else {
                    c2 = utftext.charCodeAt(i + 1);
                    c3 = utftext.charCodeAt(i + 2);
                    string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                    i += 3;
                }
            }
        }
        return string;
    },
    needEncoding: function(text) {
        var encode = false;
        var i = 0;
        if (text != null) {
            // Text is either latin-1 or utf-8 encoded, let's assume utf8 if text contain
            // C3h followed by a character with 0x80 set. C3h is Ã in latin-1, so we may
            // fail, but not likely...
            for (i = 0; i < text.length; i++) {
                if (((i + 1) < text.length) && (text.charCodeAt(i) == 0xC3)) {
                    // Text have utf8 start character
                    encode = false;
                    i++;
                    if ((text.charCodeAt(i) & 0x80) == 0) {
                        // Not two bytes, so text contain latin1 Ã
                        encode = true;
                        break;
                    }
                }
                else if ((text.charCodeAt(i) & 0x80) != 0) {
                    // Text contain non-ascii character without utf8 start character
                    encode = true;
                    break;
                }
            }
        }
        return encode;
    }
}
//-----------------------------------------------------------------------------
// BASE64 converter
//-----------------------------------------------------------------------------
var B64_STR = 
    "ABCDEFGHIJKLMNOP" +
    "QRSTUVWXYZabcdef" +
    "ghijklmnopqrstuv" +
    "wxyz0123456789+/" +
    "=";
var BASE64 = {
    encode: function(input) {
        var output = "";
        var chr1, chr2, chr3 = "";
        var enc1, enc2, enc3, enc4 = "";
        var i = 0;
        if (input != null) {
            do {
                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);
                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;
                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                }
                else if (isNaN(chr3)) {
                    enc4 = 64;
                }
                output +=
              B64_STR.charAt(enc1) +
              B64_STR.charAt(enc2) +
              B64_STR.charAt(enc3) +
              B64_STR.charAt(enc4);
                chr1 = chr2 = chr3 = "";
                enc1 = enc2 = enc3 = enc4 = "";
            }
            while (i < input.length);
        }
        return output;
    },
    decode: function(input) {
        var output = "";
        var chr1, chr2, chr3 = "";
        var enc1, enc2, enc3, enc4 = "";
        var i = 0;
        if (input != null) {
            do {
                enc1 = B64_STR.indexOf(input.charAt(i++));
                enc2 = B64_STR.indexOf(input.charAt(i++));
                enc3 = B64_STR.indexOf(input.charAt(i++));
                enc4 = B64_STR.indexOf(input.charAt(i++));
                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;
                output += String.fromCharCode(chr1);
                if (enc3 != 64)
                    output += String.fromCharCode(chr2);
                if (enc4 != 64)
                    output += String.fromCharCode(chr3);
                chr1 = chr2 = chr3 = "";
                enc1 = enc2 = enc3 = enc4 = "";
            } while (i < input.length);
        }
        return output;
    },
    valid: function(input) {
        valid = false;
        var tmp = null;
        try {
            tmp = BASE64.encode(BASE64.decode(input));
            if (tmp == input) {
                valid = true;
            }
        }
        catch (ex) {
        }
        return valid;
    }
}
//-----------------------------------------------------------------------------
// HEXADECIMAL converter
//-----------------------------------------------------------------------------
var HEX_STR = "0123456789ABCDEF";
var HEX = {
    encode: function(input) {
        var value = "";
        var i = 0;
        var c = 0;
        var c1 = 0;
        var c2 = 0;
        if (input != null) {
            for (i = 0; i < input.length; i += 1) {
                c = input.charCodeAt(i);
                c1 = (c >> 4) & 0x0f;
                c2 = (c) & 0x0f;
                value += HEX_STR.charAt(c1);
                value += HEX_STR.charAt(c2);
            }
        }
        return value;
    },
    decode: function(input) {
        var value = "";
        var i = 0;
        var c1 = 0;
        var c2 = 0;
        if (input != null) {
            input = input.toUpperCase();
            for (i = 0; i < input.length; i += 2) {
                c1 = HEX_STR.indexOf(input.charAt(i));
                c2 = HEX_STR.indexOf(input.charAt(i + 1));
                value += String.fromCharCode((c1 * 16) + c2);
            }
        }
        return value;
    },
    convert: function(input) {
        var value = null;
        value = input.toString(16);
        if ((value.length % 2) == 1) {
            value = "0" + value;
        }
        value = "0x" + value;
        return value;
    }
}
//-----------------------------------------------------------------------------
// URL converter
//-----------------------------------------------------------------------------
var URL = {
    encode: function(value) {
        if (value != null) {
            // Replace all '+' with '%2B' to avoid problem below
            value = ReplaceAll(value, "+", "%2B");
            // Convert all
            value = escape(value);
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
//-----------------------------------------------------------------------------
// PEM converter
//-----------------------------------------------------------------------------
var PEM = {
    encode: function(name, data) {
        var value = "";
        var i = 0;
        var n = 0;
        value += "-----BEGIN " + name + "-----\r\n";
        while (i < data.length) {
            if ((i + 64) > data.length)
                n = data.length - i;
            else
                n = 64;
            value += data.substr(i, n) + "\r\n";
            i += n;
        }
        value += "-----END " + name + "-----\r\n";
        return value;
    },
    decode: function(value) {
        var data = "";
        var i = 0;
        var j = 0;
        if ((i = value.indexOf("BEGIN")) != -1) {
            if ((i = value.indexOf("-----", i)) != -1) {
                i += 5;
                if ((j = value.indexOf("-----", i)) != -1) {
                    data = value.substr(i, j - i);
                    data = data.replace(/\n/g, '');
                    data = data.replace(/\s/g, '').replace(/, /g, '');
                }
            }
        }
        return data;
    }
}
//-----------------------------------------------------------------------
// ConvertNumber
//-----------------------------------------------------------------------
function ConvertNumber(number) {
    var result = 0;
    if (number.substr(0, 2) == "0x") {
        result = parseInt(number.substr(2), 16);
    }
    else if (number.substr(0, 1) == "#") {
        result = parseInt(number.substr(1), 16);
    }
    else {
        result = parseInt(number, 10);
    }
    return result;
}
//-----------------------------------------------------------------------------
// FormatXML
//-----------------------------------------------------------------------------
function FormatXML(value, cls_tag, cls_data) {
    var result = "";
    var i = 0;
    var tag = null;
    var data = null;
    var indent = 0;
    // Remove all rubbish
    value = ReplaceAll(value, "\n", "");
    value = ReplaceAll(value, "\r", "");
    value = ReplaceAll(value, " <", "<");
    value = ReplaceAll(value, "> ", ">");
    // All tags should get __TAG_START__/__TAG_END__, also add new line if two tags 
    // are beside each other. All values should get __VALUE_START__/__VALUE_END__.
    while (value.length > 0) {
        if (value.charAt(0) == '<') {
            if ((i = value.indexOf('>')) != -1) {
                tag = value.substr(0, i + 1);
                value = value.substr(i + 1);
                // Update indent counter when another tag following
                if (value.charAt(0) == '<') {
                    // Start and end tag
                    if ((tag.length > 2) && (tag.charAt(tag.length - 2) == '/')) {
                    }
                    // End tag
                    else if (tag.charAt(1) == '/') {
                    }
                    // Start tag
                    else {
                        indent++;
                    }
                    // Check next tag too
                    if (value.charAt(1) == '/') {
                        if (indent > 0) {
                            indent--;
                        }
                    }
                    // Create new tag
                    tag = "__TAG_START__" + tag + "__TAG_END__";
                    tag += "__NEW_LINE__";
                    // Indent value
                    for (i = 0; i < indent; i++) {
                        tag += "__INDENT__";
                    }
                }
                else {
                    tag = "__TAG_START__" + tag + "__TAG_END__";
                }
                // Add formatted tag
                result += tag;
            }
        }
        else {
            // Copy all text to next tag
            if ((i = value.indexOf("<")) == -1) {
                data = value;
                value = "";
            }
            else {
                data = value.substr(0, i);
                value = value.substr(i);
            }
            result += "__DATA_START__" + data + "__DATA_END__";
            indent = (indent > 0) ? indent-- : 0;
        }
    }
    result = ReplaceAll(result, "<", "&lt;");
    result = ReplaceAll(result, ">", "&gt;");
    result = ReplaceAll(result, "__INDENT__", "&nbsp;&nbsp;");
    result = ReplaceAll(result, "__TAG_START__", "<span class='" + cls_tag + "'>");
    result = ReplaceAll(result, "__TAG_END__", "</span>");
    result = ReplaceAll(result, "__DATA_START__", "<span class='" + cls_data + "'>");
    result = ReplaceAll(result, "__DATA_END__", "</span>");
    result = ReplaceAll(result, "__NEW_LINE__", "<br />");
    return result;
}
//-----------------------------------------------------------------------------
// ReplaceAll
//-----------------------------------------------------------------------------
function ReplaceAll(text, find, replace) {
    var i = 0;
    if ((text != null) && (find != null) && (replace != null)) {
        while (text.indexOf(find) != -1) {
            text = text.replace(find, replace);
        }
    }
    return text;
}
//-----------------------------------------------------------------------------
// FormatHTML
//-----------------------------------------------------------------------------
function FormatHTML(input) {
    var i = 0;
    var c = 0;
    var c2 = 0;
    var output = "";
    if (input != null) {
        for (i = 0; i < input.length; i++) {
            c = input.charCodeAt(i);
            // utf-8 (2-char)
            if ((c == 0xC3) && ((i + 1) < input.length)) {
                c2 = input.charCodeAt(i + 1);
                if (c2 == 0x84)
                    output += "&Auml;";
                else if (c2 == 0x85)
                    output += "&Aring;";
                else if (c2 == 0x96)
                    output += "&Ouml;";
                else if (c2 == 0x98)
                    output += "&Oslash;";
                else if (c2 == 0x9C)
                    output += "&Uuml;";
                else if (c2 == 0xA1)
                    output += "&aacute;";
                else if (c2 == 0xA4)
                    output += "&auml;";
                else if (c2 == 0xA5)
                    output += "&aring;";
                else if (c2 == 0xA7)
                    output += "&ccedil;";
                else if (c2 == 0xA9)
                    output += "&eacute;";
                else if (c2 == 0xAA)
                    output += "&ecirc;";
                else if (c2 == 0xB6)
                    output += "&ouml;";
                else if (c2 == 0xB8)
                    output += "&oslash;";
                else if (c2 == 0xBC)
                    output += "&uuml;";
                else
                    output += String.fromCharCode(c) + String.fromCharCode(c2);
                i++;
            }
            // latin-1 (1-char)
            else {
                if (c == 0x0D)
                    ;  // ignored since uses 0x0A below
                else if (c == 0x0A)
                    output += "<br />";
                else if (c == 0x20)
                    output += "&nbsp;";
                else if (c == 0xC4)
                    output += "&Auml;";
                else if (c == 0xC5)
                    output += "&Aring;";
                else if (c == 0xD6)
                    output += "&Ouml;";
                else if (c == 0xD8)
                    output += "&Oslash;";
                else if (c == 0xDC)
                    output += "&Uuml;";
                else if (c == 0xE1)
                    output += "&aacute;";
                else if (c == 0xE4)
                    output += "&auml;";
                else if (c == 0xE5)
                    output += "&aring;";
                else if (c == 0xE7)
                    output += "&ccedil;";
                else if (c == 0xE9)
                    output += "&eacute;";
                else if (c == 0xEA)
                    output += "&ecirc;";
                else if (c == 0xF6)
                    output += "&ouml;";
                else if (c == 0xF8)
                    output += "&oslash;";
                else if (c == 0xFC)
                    output += "&uuml;";
                // html codes
                else if (c == 0x3C)
                    output += "&lt;";
                else if (c == 0x3E)
                    output += "&gt;";
                // Accept all other as-is    
                else
                    output += String.fromCharCode(c);
            }
        }
    }
    return output;
}
//-----------------------------------------------------------------------------
// UnformatHTML
//-----------------------------------------------------------------------------
function UnformatHTML(value) {
    // Remove "some" HTML formatting for sending to Clipboard
    value = ReplaceAll(value, "<br />", "\n");
    value = ReplaceAll(value, "&nbsp;", " ");
    value = ReplaceAll(value, "&lt;", "<");
    value = ReplaceAll(value, "&gt;", ">");
    return value;
}
//-----------------------------------------------------------------------
// FormatSize
//-----------------------------------------------------------------------
function FormatSize(size) {
    var value = null;
    if (size >= (1024 * 1024)) {
        size = size / (1024 * 1024);
        value = size.toFixed(1) + "MB";
    }
    else if (size >= (10 * 1024)) {
        size = size / (1024);
        value = size.toFixed(0) + "KB";
    }
    else if (size >= 1024) {
        size = size / (1024);
        value = size.toFixed(1) + "KB";
    }
    else {
        value = size.toString(10) + "B";
    }
    return value;
}
