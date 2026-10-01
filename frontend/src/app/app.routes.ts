import { Routes } from '@angular/router';
import {
  requireActiveIdentity,
  requireOnboarding,
} from './features/identity/identity-context-guard';
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
  {
    path: 'app',
    canActivate: [requireAuthentication],
    loadComponent: () =>
      import('./features/identity/identity-context-gateway/identity-context-gateway').then(
        (module) => module.IdentityContextGateway,
      ),
  },
  {
    path: 'onboarding',
    canActivate: [requireAuthentication, requireOnboarding],
    loadComponent: () =>
      import('./features/identity/organization-onboarding/organization-onboarding').then(
        (module) => module.OrganizationOnboarding,
      ),
  },
  {
    path: 'workspace',
    canActivate: [requireAuthentication, requireActiveIdentity],
    loadComponent: () =>
      import('./features/workspace/organization-workspace/organization-workspace').then(
        (module) => module.OrganizationWorkspace,
      ),
  },
];
