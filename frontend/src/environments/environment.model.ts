export interface AppEnvironment {
  readonly production: boolean;
  readonly apiBaseUrl: string;
  readonly oidc: {
    readonly serverUrl: string;
    readonly realm: string;
    readonly clientId: string;
  };
}
