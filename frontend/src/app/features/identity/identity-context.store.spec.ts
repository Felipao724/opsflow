import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { ActiveIdentityContext, IdentityContext } from './identity-context';
import { IdentityContextClient } from './identity-context.client';
import { IdentityContextStore } from './identity-context.store';

describe('IdentityContextStore', () => {
  const activeContext: ActiveIdentityContext = {
    status: 'ACTIVE',
    userProfileId: '00000000-0000-0000-0000-000000000001',
    organizationId: '00000000-0000-0000-0000-000000000002',
    organizationName: 'Acme Operations',
    membershipRole: 'OWNER',
  };
  const identityContextClient = {
    getCurrent: vi.fn(),
    onboardOrganization: vi.fn(),
  };

  let store: IdentityContextStore;

  beforeEach(() => {
    identityContextClient.getCurrent.mockReset();
    identityContextClient.onboardOrganization.mockReset();

    TestBed.configureTestingModule({
      providers: [{ provide: IdentityContextClient, useValue: identityContextClient }],
    });

    store = TestBed.inject(IdentityContextStore);
  });

  async function loadOnboardingRequired(): Promise<void> {
    identityContextClient.getCurrent.mockReturnValue(of({ status: 'ONBOARDING_REQUIRED' }));
    await store.load();
  }

  it('starts idle', () => {
    expect(store.state()).toEqual({ status: 'idle' });
  });

  it('reports loading while the request is pending', async () => {
    const response = new Subject<IdentityContext>();
    identityContextClient.getCurrent.mockReturnValue(response);

    const loadPromise = store.load();

    expect(store.state()).toEqual({ status: 'loading', operation: 'loading-context' });

    response.next({ status: 'ONBOARDING_REQUIRED' });
    await loadPromise;
  });

  it('reports that onboarding is required', async () => {
    identityContextClient.getCurrent.mockReturnValue(of({ status: 'ONBOARDING_REQUIRED' }));

    await store.load();

    expect(store.state()).toEqual({
      status: 'onboarding-required',
      submission: { status: 'idle' },
    });
  });

  it('stores the active identity context', async () => {
    identityContextClient.getCurrent.mockReturnValue(of(activeContext));

    await store.load();

    expect(store.state()).toEqual({ status: 'active', context: activeContext });
  });

  it('reports a failure when the context cannot be loaded', async () => {
    identityContextClient.getCurrent.mockReturnValue(
      throwError(() => new Error('Identity API unavailable')),
    );

    await store.load();

    expect(store.state()).toEqual({ status: 'failure' });
  });

  it('reports submission progress and stores the created context', async () => {
    await loadOnboardingRequired();
    const response = new Subject<ActiveIdentityContext>();
    identityContextClient.onboardOrganization.mockReturnValue(response);

    const onboardingPromise = store.onboardOrganization('Acme Operations');

    expect(identityContextClient.onboardOrganization).toHaveBeenCalledWith('Acme Operations');
    expect(store.state()).toEqual({
      status: 'onboarding-required',
      submission: { status: 'submitting' },
    });

    response.next(activeContext);
    await onboardingPromise;

    expect(store.state()).toEqual({ status: 'active', context: activeContext });
  });

  it('exposes validation errors returned by the backend', async () => {
    await loadOnboardingRequired();
    identityContextClient.onboardOrganization.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              code: 'VALIDATION_ERROR',
              fieldErrors: { organizationName: 'Organization name must not be blank' },
            },
          }),
      ),
    );

    await store.onboardOrganization('');

    expect(store.state()).toEqual({
      status: 'onboarding-required',
      submission: {
        status: 'validation-error',
        fieldErrors: { organizationName: 'Organization name must not be blank' },
      },
    });
  });

  it('reloads the active context after an onboarding conflict', async () => {
    identityContextClient.getCurrent
      .mockReturnValueOnce(of({ status: 'ONBOARDING_REQUIRED' }))
      .mockReturnValueOnce(of(activeContext));
    identityContextClient.onboardOrganization.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { code: 'USER_ALREADY_ONBOARDED' },
          }),
      ),
    );
    await store.load();

    await store.onboardOrganization('Acme Operations');

    expect(identityContextClient.getCurrent).toHaveBeenCalledTimes(2);
    expect(store.state()).toEqual({ status: 'active', context: activeContext });
  });

  it('reports an unexpected onboarding failure', async () => {
    await loadOnboardingRequired();
    identityContextClient.onboardOrganization.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500 })),
    );

    await store.onboardOrganization('Acme Operations');

    expect(store.state()).toEqual({
      status: 'onboarding-required',
      submission: { status: 'failure' },
    });
  });
});
