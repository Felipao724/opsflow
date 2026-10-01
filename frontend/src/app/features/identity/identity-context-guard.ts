import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { IdentityContextStore } from './identity-context.store';

export const requireOnboarding: CanActivateFn = () => {
  const store = inject(IdentityContextStore);
  const router = inject(Router);

  const identityState = store.state();

  if (identityState.status === 'onboarding-required') {
    return true;
  }

  if (identityState.status === 'active') {
    return router.createUrlTree(['/workspace']);
  }
  return router.createUrlTree(['/app']);
};

export const requireActiveIdentity: CanActivateFn = () => {
  const store = inject(IdentityContextStore);
  const router = inject(Router);

  const identityState = store.state();

  if (identityState.status === 'active') {
    return true;
  }

  if (identityState.status === 'onboarding-required') {
    return router.createUrlTree(['/onboarding']);
  }
  return router.createUrlTree(['/app']);
};
