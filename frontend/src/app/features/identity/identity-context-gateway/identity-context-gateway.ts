import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IdentityContextStore } from '../identity-context.store';

@Component({
  imports: [],
  selector: 'app-identity-context-gateway',
  styleUrl: './identity-context-gateway.css',
  templateUrl: './identity-context-gateway.html',
})
export class IdentityContextGateway implements OnInit {
  private readonly identityContextStore = inject(IdentityContextStore);
  private readonly router = inject(Router);

  protected readonly identityState = this.identityContextStore.state;

  ngOnInit(): void {
    void this.resolveDestination();
  }

  private async resolveDestination(): Promise<void> {
    await this.identityContextStore.load();

    const state = this.identityState();

    if (state.status === 'onboarding-required') {
      await this.router.navigateByUrl('/onboarding');
      return;
    }

    if (state.status === 'active') {
      await this.router.navigateByUrl('/workspace');
      return;
    }
  }

  protected retry(): void {
    void this.resolveDestination();
  }
}
