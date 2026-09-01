import { TestBed } from '@angular/core/testing';
import { AuthenticationReturnUrlStore } from './authentication-return-url-store';

describe('AuthenticationReturnUrlStore', () => {
  let store: AuthenticationReturnUrlStore;

  beforeEach(() => {
    window.sessionStorage.clear();
    store = TestBed.inject(AuthenticationReturnUrlStore);
  });

  afterEach(() => {
    window.sessionStorage.clear();
  });

  it('remembers and consumes an allowed application route once', () => {
    store.remember('/protected?tab=activity#latest');

    expect(store.consume()).toBe('/protected?tab=activity#latest');
    expect(store.consume()).toBe('/');
  });

  it('rejects a return URL on a different origin', () => {
    store.remember('https://example.com/protected');

    expect(store.consume()).toBe('/');
  });

  it('rejects an application route that is not explicitly allowed', () => {
    store.remember('/auth/callback');

    expect(store.consume()).toBe('/');
  });
});
