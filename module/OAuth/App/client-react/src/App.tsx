import React from 'react';
import { OAuthSecurityProvider } from './security/OAuthSecurityContext';
import AppRouter from './main/AppRouter';

function App() {
  return (
    <OAuthSecurityProvider clientId={'test'} redirectUri={`${window.location.origin}/oauth`} oauthContextPath={`http://localhost:8082/oauth/api`}>
      <AppRouter/>
    </OAuthSecurityProvider>
  );
}

export default App;
