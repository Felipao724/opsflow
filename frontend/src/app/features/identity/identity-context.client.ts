import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ActiveIdentityContext, IdentityContext } from './identity-context';

@Service()
export class IdentityContextClient {
  private readonly apiBaseUrl = environment.apiBaseUrl;
  private readonly httpClient = inject(HttpClient);

  getCurrent(): Observable<IdentityContext> {
    return this.httpClient.get<IdentityContext>(`${this.apiBaseUrl}/identity/context`);
  }

  onboardOrganization(organizationName: string): Observable<ActiveIdentityContext> {
    return this.httpClient.post<ActiveIdentityContext>(`${this.apiBaseUrl}/identity/onboarding`, {
      organizationName,
    });
  }
}
