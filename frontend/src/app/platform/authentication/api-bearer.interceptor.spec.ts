import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { apiBearerInterceptor } from './api-bearer.interceptor';
import { AuthenticationClient } from './authentication-client';

describe('apiBearerInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let authenticationClient: FakeAuthenticationClient;

  beforeEach(() => {
    authenticationClient = new FakeAuthenticationClient();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiBearerInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthenticationClient,
          useValue: authenticationClient,
        },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('adds the access token to requests for the OpsFlow API', async () => {
    authenticationClient.accessToken = 'test-access-token';

    httpClient.get('http://localhost:8080/api/work-orders').subscribe();
    await Promise.resolve();

    const request = httpTestingController.expectOne('http://localhost:8080/api/work-orders');

    expect(request.request.headers.get('Authorization')).toBe('Bearer test-access-token');
    expect(authenticationClient.tokenRequests).toBe(1);
    request.flush({});
  });

  it('does not expose the token to a different origin', async () => {
    authenticationClient.accessToken = 'test-access-token';

    httpClient.get('https://example.com/api/work-orders').subscribe();
    await Promise.resolve();

    const request = httpTestingController.expectOne('https://example.com/api/work-orders');

    expect(request.request.headers.has('Authorization')).toBe(false);
    expect(authenticationClient.tokenRequests).toBe(0);
    request.flush({});
  });

  it('does not expose the token outside the API path', async () => {
    authenticationClient.accessToken = 'test-access-token';

    httpClient.get('http://localhost:8080/actuator/health').subscribe();
    await Promise.resolve();

    const request = httpTestingController.expectOne('http://localhost:8080/actuator/health');

    expect(request.request.headers.has('Authorization')).toBe(false);
    expect(authenticationClient.tokenRequests).toBe(0);
    request.flush({});
  });

  it('leaves an API request anonymous when no access token is available', async () => {
    httpClient.get('http://localhost:8080/api/work-orders').subscribe();
    await Promise.resolve();

    const request = httpTestingController.expectOne('http://localhost:8080/api/work-orders');

    expect(request.request.headers.has('Authorization')).toBe(false);
    expect(authenticationClient.tokenRequests).toBe(1);
    request.flush({});
  });
});

class FakeAuthenticationClient {
  accessToken: string | undefined;
  tokenRequests = 0;

  getValidAccessToken(): Promise<string | undefined> {
    this.tokenRequests += 1;
    return Promise.resolve(this.accessToken);
  }
}
