import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { vi } from 'vitest';
import { AuthenticationClient } from './authentication-client';
import { requireAuthentication } from './authentication.guard';

describe('requireAuthentication', () => {
  const authenticationClient = {
    isAuthenticated: vi.fn(),
  };

  let router: Router;

  beforeEach(() => {
    authenticationClient.isAuthenticated.mockReset();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthenticationClient,
          useValue: authenticationClient,
        },
      ],
    });

    router = TestBed.inject(Router);
  });

  function executeGuard(url: string): boolean | UrlTree {
    return TestBed.runInInjectionContext(() =>
      requireAuthentication(
        {} as ActivatedRouteSnapshot,
        { url } as RouterStateSnapshot,
      ),
    ) as boolean | UrlTree;
  }

  it('allows navigation when the provider session is authenticated', () => {
    authenticationClient.isAuthenticated.mockReturnValue(true);

    expect(executeGuard('/workspace')).toBe(true);
  });

  it('redirects an unauthenticated user while preserving the requested URL', () => {
    authenticationClient.isAuthenticated.mockReturnValue(false);

    const result = executeGuard('/workspace') as UrlTree;

    expect(router.serializeUrl(result)).toBe('/?returnUrl=%2Fworkspace');
  });
});
