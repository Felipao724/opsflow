export type AuthenticationFailureReason =
  'provider-unavailable' | 'login-failed' | 'logout-failed' | 'token-refresh-failed';

export type AuthenticationState =
  | { readonly status: 'loading'; readonly operation: AuthenticationOperation }
  | { readonly status: 'authenticated' }
  | { readonly status: 'unauthenticated' }
  | {
      readonly status: 'failure';
      readonly reason: AuthenticationFailureReason;
    };

export type AuthenticationOperation = 'initializing' | 'signing-in' | 'signing-out';

export type AuthenticationStatus = AuthenticationState['status'];
