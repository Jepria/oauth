import Base64 from 'crypto-js/enc-base64';
import Utf8 from 'crypto-js/enc-utf8';
import SHA256 from 'crypto-js/sha256';

const toBase64Url = (str) => {
  let encoded = Base64.stringify(Utf8.parse(str));
  encoded = encoded.replace(/\+/gi, '-');
  encoded = encoded.replace(/\//gi, '_');
  encoded = encoded.replace(/=/gi, '');
  return encoded;
}

const fromBase64Url = (encodedStr) => {
  encodedStr = encodedStr.replace(/-/gi, '+');
  encodedStr = encodedStr.replace(/_/gi, '/');
  let str = Base64.parse(encodedStr);
  return Utf8.stringify(str);
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
  let crypto;
  if (window) {
    crypto = window.crypto || window.msCrypto;
  }
  if (crypto) {
    let values = new Uint32Array(16);
    crypto.getRandomValues(values);
    for (let i = 0; i < 16; i++) {
      result += charset[values[i] % charset.length];
    }
    return result;
  } else {
    return Array(16).join().split(',').map(function() { return charset.charAt(Math.floor(Math.random() * charset.length)); }).join('');
  }

}

export { toBase64Url, fromBase64Url, sha256, getRandomString };