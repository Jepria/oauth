var parameters = getLocationParameters();

function getLocationParameters() {
  var vars = {};
  var url = window.location.href;
  if (url.indexOf('#') !== -1) {
    url = url.substring(0, url.indexOf('#'));
  }
  url.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
    vars[key] = value;
  });
  return vars;
}

function createWelcomeMessage() {
  var clientName = parameters['client_name'];
  var welcomeMessageContainer = document.getElementById('welcome-message');
  if (clientName) {
    var clientNameElement = document.createElement('h1');
    clientNameElement.innerText = decodeURIComponent(clientName);
    welcomeMessageContainer.appendChild(clientNameElement);
  }
  var paragraphElement = document.createElement('h2');
  paragraphElement.innerHTML += 'Добро пожаловать';
  welcomeMessageContainer.appendChild(paragraphElement);
}

function refreshFormParameters() {
  var form = document.getElementById('login-form') || null;
  if (form) {
    var responseType = parameters['response_type'] !== undefined ? parameters['response_type'] : '';
    var authCode = parameters['code'] !== undefined ? parameters['code'] : '';
    var redirectUri = parameters['redirect_uri'] !== undefined ? parameters['redirect_uri'] : '';
    var clientId = parameters['client_id'] !== undefined ? parameters['client_id'] : '';
    var clientName = parameters['client_name'] !== undefined ? parameters['client_name'] : '';
    var state = parameters['state'] !== undefined ? parameters['state'] : '';
    var hash = window.location.hash.slice(0);
    window.location.hash = '';
    form.action = form.action + '?response_type=' + responseType + '&code=' + authCode + '&redirect_uri=' + redirectUri + '&client_id=' + clientId + "&client_name=" + clientName + '&state=' + state + hash;
    form[0].focus();
  }
}

function isErrorResponse() {
  if (parameters['error-code'] === '401') {
    document.getElementById("error-message-container").className = "has-error";
    document.getElementById('error-message').appendChild(document.createTextNode("Неверные данные!"));
  } else {
    document.getElementById("error-message-container").className = "hidden";
    var span = document.getElementById('error-message');
    if (span.firstChild) span.removeChild(span.firstChild);
  }
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