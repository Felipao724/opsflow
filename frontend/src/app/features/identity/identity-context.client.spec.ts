import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ActiveIdentityContext } from './identity-context';
import { IdentityContextClient } from './identity-context.client';

describe('IdentityContextClient', () => {
  let client: IdentityContextClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    client = TestBed.inject(IdentityContextClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('loads the current identity context', async () => {
    const responsePromise = firstValueFrom(client.getCurrent());
    const request = httpTestingController.expectOne(`${environment.apiBaseUrl}/identity/context`);

    expect(request.request.method).toBe('GET');

    request.flush({ status: 'ONBOARDING_REQUIRED' });

    await expect(responsePromise).resolves.toEqual({ status: 'ONBOARDING_REQUIRED' });
  });

  it('submits the organization name and returns the active identity context', async () => {
    const activeContext: ActiveIdentityContext = {
      status: 'ACTIVE',
      userProfileId: '00000000-0000-0000-0000-000000000001',
      organizationId: '00000000-0000-0000-0000-000000000002',
      organizationName: 'Acme Operations',
      membershipRole: 'OWNER',
    };
    const responsePromise = firstValueFrom(client.onboardOrganization('Acme Operations'));
    const request = httpTestingController.expectOne(
      `${environment.apiBaseUrl}/identity/onboarding`,
    );

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ organizationName: 'Acme Operations' });

    request.flush(activeContext);

    await expect(responsePromise).resolves.toEqual(activeContext);
  });
});
