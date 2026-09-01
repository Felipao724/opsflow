import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationClient } from '../../platform/authentication/authentication-client';
import { AuthenticationReturnUrlStore } from '../../platform/authentication/authentication-return-url-store';

@Component({
  selector: 'app-authentication-callback',
  standalone: true,
  template: '<p>Completing sign in…</p>',
})
export class AuthenticationCallback implements OnInit {
  private readonly authenticationClient = inject(AuthenticationClient);
  private readonly returnUrlStore = inject(AuthenticationReturnUrlStore);
  private readonly router = inject(Router);

  ngOnInit(): void {
    const returnUrl = this.returnUrlStore.consume();

    const destination = this.authenticationClient.isAuthenticated() ? returnUrl : '/';

    void this.router.navigateByUrl(destination);
  }
}
