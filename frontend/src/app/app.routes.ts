import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth/callback',
    redirectTo: '',
    pathMatch: 'full',
  },
];
