import { signal, type WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { AuthenticationClient } from '../../platform/authentication/authentication-client';
import type { AuthenticationState } from '../../platform/authentication/authentication-state';
import { SecurityProbeClient } from '../../platform/security/security-probe-client';
import { IdentityGateway } from './identity-gateway';

describe('IdentityGateway', () => {
  let authenticationState: WritableSignal<AuthenticationState>;

  beforeEach(() => {
    authenticationState = signal<AuthenticationState>({
      status: 'unauthenticated',
    });

    TestBed.configureTestingModule({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: () => null,
              },
            },
          },
        },
        {
          provide: AuthenticationClient,
          useValue: {
            state: authenticationState.asReadonly(),
            login: () => Promise.resolve(),
            logout: () => Promise.resolve(),
          },
        },
        {
          provide: SecurityProbeClient,
          useValue: {},
        },
      ],
    });
  });

  it('renders the OpsFlow product identity', async () => {
    const fixture = TestBed.createComponent(IdentityGateway);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('h1')?.textContent?.trim()).toBe('OpsFlow');
    expect(element.querySelector('.product-description')?.textContent?.trim()).toBe(
      'Operations workflow management.',
    );
  });

  it('offers sign in when there is no authenticated session', async () => {
    const fixture = TestBed.createComponent(IdentityGateway);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('You are not signed in.');
    expect(element.querySelector('button')?.textContent?.trim()).toBe('Sign in with Keycloak');
  });

  it('shows the current authentication operation while loading', async () => {
    authenticationState.set({
      status: 'loading',
      operation: 'signing-out',
    });
    const fixture = TestBed.createComponent(IdentityGateway);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Signing out…');
  });

  it('offers authenticated actions when the session is established', async () => {
    authenticationState.set({ status: 'authenticated' });
    const fixture = TestBed.createComponent(IdentityGateway);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;
    const actions = [...element.querySelectorAll('button')].map((button) =>
      button.textContent?.trim(),
    );

    expect(element.textContent).toContain('Identity session established.');
    expect(actions).toContain('Verify backend access');
    expect(actions).toContain('Sign out');
  });

  it('shows a safe message when authentication fails', async () => {
    authenticationState.set({
      status: 'failure',
      reason: 'provider-unavailable',
    });
    const fixture = TestBed.createComponent(IdentityGateway);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain(
      'The identity provider is currently unavailable.',
    );
  });
});
