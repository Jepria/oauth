String.prototype.applyXSSprotection = function(){
    return this.replace(/</g, "&lt;").replace(/>/g, "&gt;");
};

var parameters = getLocationParameters();
var errors = {
    'invalid_request': 'Invalid request',
    'unauthorized_client' : 'Unauthorized client',
    'unsupported_response_type' : 'Unsupported response type',
    'server_error' : 'Server error',
    'access_denied' : 'Access denied',
    'unsupported_grant_type' : 'Unsupported grant type',
    'invalid_grant' : 'Invalid grant'
}


function getLocationParameters() {
    var vars = {};
    var url = window.location.href;
    if (url.indexOf('#') !== -1) {
        url = url.substring(0, url.indexOf('#'));
    }
    url.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

function addOnloadHandler(handler) {
    if (window.attachEvent) {
        window.attachEvent('onload', handler);
    } else {
        if (window.onload) {
            var currOnnLoad = window.onload;
            var newOnLoad = function (evt) {
                currOnnLoad(evt);
                handler(evt);
            };
            window.onload = newOnLoad;
        } else {
            window.onload = handler;
        }
    }
}

function preparePage() {
    var error = parameters['error'].applyXSSprotection();
    if (!parameters['error'] || errors[error] === undefined) {
        return;
    }
    var errorCode;
    switch (error) {
        case 'server_error':
            errorCode = '500';
            break;
        case 'access_denied':
            errorCode = '401';
            break;
        default:
            errorCode = '400';
    }
    error = errors[error];
    document.getElementById('error-code').innerHTML=errorCode;
    document.getElementById('error').innerHTML=error;
    if (parameters['error_id']) {
        document.getElementById('error-id').innerHTML="Error ID: " + parameters['error_id'];
    }
    var errorDescription = parameters['error_description'];
    if (errorDescription) {
        errorDescription = decodeURIComponent(errorDescription).applyXSSprotection();
        document.getElementById('error-description').innerHTML=errorDescription;
    }
    var redirectUri = parameters['redirect_uri'];
    if (redirectUri) {
        var redirectUri = decodeURIComponent(redirectUri).applyXSSprotection();
        document.getElementById('app-href').href=redirectUri;
    }
}

