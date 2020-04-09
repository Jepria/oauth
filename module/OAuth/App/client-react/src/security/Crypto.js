const getCrypto = () => {
  let crypto;
  if (window) {
    crypto = window.crypto || window.msCrypto;
  }
  if (crypto) {
    return crypto;
  } else {
    throw new Error("Crypto API is not available");
  }
}

const textEncode = (str) => {
  if (window.TextEncoder) {
    return new TextEncoder('utf-8').encode(str);
  }
  var utf8 = unescape(encodeURIComponent(str));
  var result = new Uint8Array(utf8.length);
  for (var i = 0; i < utf8.length; i++) {
    result[i] = utf8.charCodeAt(i);
  }
  return result;
}

const toBase64Url = (str) => {
  let encoded = btoa(str);
  encoded = encoded.replace(/\+/gi, '-');
  encoded = encoded.replace(/\//gi, '_');
  encoded = encoded.replace(/=/gi, '');
  return encoded;
}

const fromBase64Url = (encodedStr) => {
  encodedStr = encodedStr.replace(/-/gi, '+');
  encodedStr = encodedStr.replace(/_/gi, '/');
  let str = atob(encodedStr);
  return str;
}

const sha256 = async (str) => {
  const crypto = getCrypto();
  if (crypto && crypto.subtle.digest) {
    const data = textEncode(str);
    let digest = await crypto.subtle.digest('SHA-256', data);
    return new Promise((resolve, reject) => {
      if (digest.result) {
        var binary = '';
        var bytes = new Uint8Array(digest.result);
        var len = bytes.byteLength;
        for (var i = 0; i < len; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        resolve(toBase64Url(binary))
      } else {
        var binary = '';
        var bytes = new Uint8Array(digest);
        var len = bytes.byteLength;
        for (var i = 0; i < len; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        resolve(toBase64Url(binary))
      }
    });
  } else {
    throw new Error("Digest API is not available");
  }
}

const getRandomString = () => {
  const charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  let result = "";
  let crypto = getCrypto();
  let values = new Uint32Array(16);
  crypto.getRandomValues(values);
  for (let i = 0; i < 16; i++) {
    result += charset[values[i] % charset.length];
  }
  return result;

}

export { getCrypto, toBase64Url, fromBase64Url, textEncode, sha256, getRandomString };