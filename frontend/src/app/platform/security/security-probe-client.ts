import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SecurityProbeClient {
  private readonly apiBaseUrl = environment.apiBaseUrl;
  private readonly httpClient = inject(HttpClient);

  getAuthenticatedStatus(): Observable<AuthenticatedStatus> {
    return this.httpClient.get<AuthenticatedStatus>(`${this.apiBaseUrl}/security/authenticated`);
  }
}

export interface AuthenticatedStatus {
  readonly authenticated: boolean;
}
