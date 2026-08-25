import { Injectable } from '@angular/core';
import { injectKeycloakAdapter } from './keycloak-adapter';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationClient {
  private readonly redirectUri = `${window.location.origin}/auth/callback`;

  private readonly keycloak = injectKeycloakAdapter();

  initialize(): Promise<boolean> {
    return this.keycloak.init({
      flow: 'standard',
      pkceMethod: 'S256',
      responseMode: 'fragment',
      redirectUri: this.redirectUri,
      checkLoginIframe: false,
    });
  }

  login(): Promise<void> {
    return this.keycloak.login({
      redirectUri: this.redirectUri,
    });
  }

  async getValidAccessToken(): Promise<string | undefined> {
    if (!this.keycloak.authenticated) {
      return undefined;
    }

    await this.keycloak.updateToken(30);

    return this.keycloak.token;
  }

  isAuthenticated(): boolean {
    return this.keycloak.authenticated === true;
  }
}
