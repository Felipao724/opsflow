import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { buildPageTitle } from '../../app-title';
import { AuthenticationClient } from '../../platform/authentication/authentication-client';
import { AuthenticationReturnUrlStore } from '../../platform/authentication/authentication-return-url-store';
import { SecurityProbeClient } from '../../platform/security/security-probe-client';

@Component({
  selector: 'app-identity-gateway',
  standalone: true,
  styleUrl: './identity-gateway.css',
  templateUrl: './identity-gateway.html',
})
export class IdentityGateway {
  private readonly authClient: AuthenticationClient = inject(AuthenticationClient);
  private readonly securityClient = inject(SecurityProbeClient);
  private readonly returnUrlStore = inject(AuthenticationReturnUrlStore);
  private readonly route = inject(ActivatedRoute);

  protected readonly authenticationState = this.authClient.state;

  protected readonly verificationMessage = signal<string | undefined>(undefined);

  protected readonly isVerifying = signal(false);

  protected readonly title = signal(buildPageTitle());

  protected login(): void {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    this.returnUrlStore.remember(returnUrl);

    void this.authClient.login();
  }

  protected logout(): void {
    void this.authClient.logout();
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

  protected readonly loadingMessage = computed(() => {
    const state = this.authenticationState();

    if (state.status !== 'loading') {
      return undefined;
    }

    switch (state.operation) {
      case 'initializing':
        return 'Restoring identity session…';
      case 'signing-in':
        return 'Redirecting to sign in…';
      case 'signing-out':
        return 'Signing out…';
    }
  });
}
