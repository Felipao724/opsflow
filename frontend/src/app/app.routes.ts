import { Routes } from '@angular/router';
import { requireAuthentication } from './platform/authentication/authentication.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/identity/identity-gateway').then((m) => m.IdentityGateway),
  },
  {
    path: 'auth/callback',
    loadComponent: () =>
      import('./features/identity/authentication-callback').then(
        (module) => module.AuthenticationCallback,
      ),
  },
  {
    path: 'protected',
    canActivate: [requireAuthentication],
    loadComponent: () =>
      import('./features/identity/authenticated-area').then((m) => m.AuthenticatedAreaComponent),
  },
];
