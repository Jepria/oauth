import Base64 from 'crypto-js/enc-base64';
import SHA256 from 'crypto-js/sha256';

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
  return new Promise((resolve, reject) => {
    try {
      let hash = SHA256(str);
      let encoded = Base64.stringify(hash);
      encoded = encoded.replace(/\+/gi, '-');
      encoded = encoded.replace(/\//gi, '_');
      encoded = encoded.replace(/=/gi, '');
      resolve(encoded);
    } catch (e) {
      reject(e);
    }
  });
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

export { toBase64Url, fromBase64Url, textEncode, sha256, getRandomString };