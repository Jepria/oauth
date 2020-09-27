import i18n from 'i18next';
import { initReactI18next } from 'react-i18next'
import Backend from 'i18next-http-backend';
import LanguageDetecor from 'i18next-browser-languagedetector';

i18n
  .use(Backend)
  .use(LanguageDetecor)
  .use(initReactI18next)
  .init({
    backend: {
      loadPath: `${process.env.PUBLIC_URL}/locales/{{lng}}/translation.json`
    },
    fallbackLng: 'ru-RU',
    debug: process.env.NODE_ENV !== "production",
    interpolation: {
      escapeValue: false
    }
  })

export default i18n;