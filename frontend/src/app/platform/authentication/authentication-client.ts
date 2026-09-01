import { Injectable, computed, signal } from '@angular/core';
import { AuthenticationState } from './authentication-state';
import { injectKeycloakAdapter } from './keycloak-adapter';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationClient {
  private readonly redirectUri = `${window.location.origin}/auth/callback`;
  private readonly postLogoutRedirectUri = `${window.location.origin}/`;
  private readonly silentCheckSsoRedirectUri = `${window.location.origin}/silent-check-sso.html`;
  private readonly authenticationState = signal<AuthenticationState>({
    status: 'loading',
    operation: 'initializing',
  });
  readonly state = this.authenticationState.asReadonly();
  private readonly keycloak = injectKeycloakAdapter();

  readonly isAuthenticated = computed(() => this.state().status === 'authenticated');

  async initialize(): Promise<void> {
    this.authenticationState.set({
      status: 'loading',
      operation: 'initializing',
    });

    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        flow: 'standard',
        pkceMethod: 'S256',
        responseMode: 'fragment',
        silentCheckSsoRedirectUri: this.silentCheckSsoRedirectUri,
        checkLoginIframe: false,
      });

      this.authenticationState.set({
        status: authenticated ? 'authenticated' : 'unauthenticated',
      });
    } catch {
      this.authenticationState.set({
        status: 'failure',
        reason: 'provider-unavailable',
      });
    }
  }

  async login(): Promise<void> {
    this.authenticationState.set({
      status: 'loading',
      operation: 'signing-in',
    });

    try {
      await this.keycloak.login({
        redirectUri: this.redirectUri,
      });
    } catch {
      this.authenticationState.set({
        status: 'failure',
        reason: 'login-failed',
      });
    }
  }

  async logout(): Promise<void> {
    this.authenticationState.set({
      status: 'loading',
      operation: 'signing-out',
    });

    try {
      await this.keycloak.logout({
        redirectUri: this.postLogoutRedirectUri,
      });
    } catch {
      this.authenticationState.set({
        status: 'failure',
        reason: 'logout-failed',
      });
    }
  }

  async getValidAccessToken(): Promise<string | undefined> {
    if (!this.keycloak.authenticated) {
      this.authenticationState.set({
        status: 'unauthenticated',
      });
      return undefined;
    }

    try {
      await this.keycloak.updateToken(30);

      if (!this.keycloak.token) {
        this.authenticationState.set({
          status: 'unauthenticated',
        });
        return undefined;
      }

      this.authenticationState.set({
        status: 'authenticated',
      });

      return this.keycloak.token;
    } catch {
      this.keycloak.clearToken();

      this.authenticationState.set({
        status: 'failure',
        reason: 'token-refresh-failed',
      });
      throw new Error('Unable to obtain a valid access token');
    }
  }
}
