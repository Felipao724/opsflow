import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { apiBearerInterceptor } from './platform/authentication/api-bearer.interceptor';
import { AuthenticationClient } from './platform/authentication/authentication-client';
import { provideKeycloakAdapter } from './platform/authentication/keycloak-adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideKeycloakAdapter(),
    provideAppInitializer(() => inject(AuthenticationClient).initialize()),
    provideHttpClient(withInterceptors([apiBearerInterceptor])),
    provideRouter(routes),
  ],
};
