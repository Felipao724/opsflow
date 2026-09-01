import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationClient } from './authentication-client';

export const requireAuthentication: CanActivateFn = (_router, routerState) => {
  const authenticationClient = inject(AuthenticationClient);
  const router = inject(Router);

  return authenticationClient.isAuthenticated()
    ? true
    : router.createUrlTree(['/'], {
        queryParams: { returnUrl: routerState.url },
      });
};
