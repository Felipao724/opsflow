import type { AppEnvironment } from './environment.model';

export const environment = {
  production: true,
  apiBaseUrl: 'https://api.opsflow.invalid/api',
  oidc: {
    serverUrl: 'https://identity.opsflow.invalid',
    realm: 'opsflow',
    clientId: 'opsflow-web',
  },
} satisfies AppEnvironment;
