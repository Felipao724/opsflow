import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { buildPageTitle } from './app-title';
import { AuthenticationClient } from './platform/authentication/authentication-client';
import { SecurityProbeClient } from './platform/security/security-probe-client';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly authClient: AuthenticationClient = inject(AuthenticationClient);
  private readonly securityClient = inject(SecurityProbeClient);

  protected readonly title = signal(buildPageTitle());

  protected readonly isAuthenticated = signal(this.authClient.isAuthenticated());

  protected readonly verificationMessage = signal<string | undefined>(undefined);

  protected readonly isVerifying = signal(false);

  protected login(): void {
    void this.authClient.login();
  }

  protected async verifyBackendAccess(): Promise<void> {
    this.isVerifying.set(true);
    this.verificationMessage.set(undefined);

    try {
      const response = await firstValueFrom(this.securityClient.getAuthenticatedStatus());

      if (response.authenticated) {
        this.verificationMessage.set('Authenticated request accepted by OpsFlow API.');
      } else {
        this.verificationMessage.set('OpsFlow API rejected or could not complete the request.');
      }
    } catch {
      this.verificationMessage.set('OpsFlow API rejected or could not complete the request.');
    } finally {
      this.isVerifying.set(false);
    }
  }
}
