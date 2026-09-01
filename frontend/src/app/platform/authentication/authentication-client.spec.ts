import { TestBed } from '@angular/core/testing';
import type Keycloak from 'keycloak-js';
import { vi } from 'vitest';
import { AuthenticationClient } from './authentication-client';
import { injectKeycloakAdapter, provideKeycloakAdapter } from './keycloak-adapter';

describe('AuthenticationClient', () => {
  let client: AuthenticationClient;
  let keycloak: Keycloak;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideKeycloakAdapter()],
    });

    client = TestBed.inject(AuthenticationClient);
    keycloak = TestBed.runInInjectionContext(() => injectKeycloakAdapter());
  });

  it('represents a restored provider session as authenticated', async () => {
    vi.spyOn(keycloak, 'init').mockResolvedValue(true);

    await client.initialize();

    expect(client.state()).toEqual({ status: 'authenticated' });
    expect(client.isAuthenticated()).toBe(true);
  });

  it('represents the absence of a provider session as unauthenticated', async () => {
    vi.spyOn(keycloak, 'init').mockResolvedValue(false);

    await client.initialize();

    expect(client.state()).toEqual({ status: 'unauthenticated' });
  });

  it('represents an unavailable provider as a controlled failure', async () => {
    vi.spyOn(keycloak, 'init').mockRejectedValue(new Error('Provider unavailable'));

    await client.initialize();

    expect(client.state()).toEqual({
      status: 'failure',
      reason: 'provider-unavailable',
    });
  });

  it('renews the token before returning it to an API request', async () => {
    keycloak.authenticated = true;
    keycloak.token = 'renewed-access-token';
    const updateToken = vi.spyOn(keycloak, 'updateToken').mockResolvedValue(true);

    await expect(client.getValidAccessToken()).resolves.toBe('renewed-access-token');

    expect(updateToken).toHaveBeenCalledOnce();
    expect(updateToken).toHaveBeenCalledWith(30);
    expect(client.state()).toEqual({ status: 'authenticated' });
  });

  it('clears an unrecoverable session and exposes a controlled failure', async () => {
    keycloak.authenticated = true;
    vi.spyOn(keycloak, 'updateToken').mockRejectedValue(new Error('Refresh failed'));
    const clearToken = vi.spyOn(keycloak, 'clearToken');

    await expect(client.getValidAccessToken()).rejects.toThrow(
      'Unable to obtain a valid access token',
    );

    expect(clearToken).toHaveBeenCalledOnce();
    expect(client.isAuthenticated()).toBe(false);
    expect(client.state()).toEqual({
      status: 'failure',
      reason: 'token-refresh-failed',
    });
  });
});
