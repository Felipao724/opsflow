import {
  inject,
  InjectionToken,
  makeEnvironmentProviders,
  type EnvironmentProviders,
} from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

const KEYCLOAK_ADAPTER = new InjectionToken<Keycloak>('OpsFlow Keycloak adapter');

export function provideKeycloakAdapter(): EnvironmentProviders {
  return makeEnvironmentProviders([
    {
      provide: KEYCLOAK_ADAPTER,
      useFactory: createKeycloakAdapter,
    },
  ]);
}

export function injectKeycloakAdapter(): Keycloak {
  return inject(KEYCLOAK_ADAPTER);
}

function createKeycloakAdapter(): Keycloak {
  return new Keycloak({
    url: environment.oidc.serverUrl,
    realm: environment.oidc.realm,
    clientId: environment.oidc.clientId,
  });
}
