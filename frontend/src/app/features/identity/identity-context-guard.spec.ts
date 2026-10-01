import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { ActiveIdentityContext } from './identity-context';
import { requireActiveIdentity, requireOnboarding } from './identity-context-guard';
import { IdentityContextState, IdentityContextStore } from './identity-context.store';

describe('identity context guards', () => {
  const activeContext: ActiveIdentityContext = {
    status: 'ACTIVE',
    userProfileId: '00000000-0000-0000-0000-000000000001',
    organizationId: '00000000-0000-0000-0000-000000000002',
    organizationName: 'Acme Operations',
    membershipRole: 'OWNER',
  };
  const identityState = signal<IdentityContextState>({ status: 'idle' });

  let router: Router;

  beforeEach(() => {
    identityState.set({ status: 'idle' });

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: IdentityContextStore,
          useValue: { state: identityState.asReadonly() },
        },
      ],
    });

    router = TestBed.inject(Router);
  });

  function executeGuard(guard: CanActivateFn): ReturnType<CanActivateFn> {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  function expectRedirect(result: ReturnType<CanActivateFn>, expectedUrl: string): void {
    expect(router.serializeUrl(result as UrlTree)).toBe(expectedUrl);
  }

  it('allows onboarding when onboarding is required', () => {
    identityState.set({ status: 'onboarding-required', submission: { status: 'idle' } });

    expect(executeGuard(requireOnboarding)).toBe(true);
  });

  it('redirects an active identity away from onboarding', () => {
    identityState.set({ status: 'active', context: activeContext });

    expectRedirect(executeGuard(requireOnboarding), '/workspace');
  });

  it('redirects unresolved onboarding access through the context gateway', () => {
    expectRedirect(executeGuard(requireOnboarding), '/app');
  });

  it('allows workspace access for an active identity', () => {
    identityState.set({ status: 'active', context: activeContext });

    expect(executeGuard(requireActiveIdentity)).toBe(true);
  });

  it('redirects an identity requiring onboarding away from the workspace', () => {
    identityState.set({ status: 'onboarding-required', submission: { status: 'idle' } });

    expectRedirect(executeGuard(requireActiveIdentity), '/onboarding');
  });

  it('redirects unresolved workspace access through the context gateway', () => {
    expectRedirect(executeGuard(requireActiveIdentity), '/app');
  });
});
