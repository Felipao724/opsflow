import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AuthenticationClient } from './platform/authentication/authentication-client';
import { SecurityProbeClient } from './platform/security/security-probe-client';

describe('App', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthenticationClient,
          useValue: {
            isAuthenticated: () => false,
            login: () => Promise.resolve(),
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
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('h1')?.textContent?.trim()).toBe('OpsFlow');
    expect(element.querySelector('.product-description')?.textContent?.trim()).toBe(
      'Operations workflow management.',
    );
  });

  it('offers sign in when there is no authenticated session', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('You are not signed in.');
    expect(element.querySelector('button')?.textContent?.trim()).toBe('Sign in with Keycloak');
  });
});
