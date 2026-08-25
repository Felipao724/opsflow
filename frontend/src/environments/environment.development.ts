import type { AppEnvironment } from './environment.model';

export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  oidc: {
    serverUrl: 'http://localhost:8081',
    realm: 'opsflow',
    clientId: 'opsflow-web',
  },
} satisfies AppEnvironment;
