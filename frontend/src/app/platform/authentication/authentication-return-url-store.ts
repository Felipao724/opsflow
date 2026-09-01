import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationReturnUrlStore {
  private readonly storageKey = 'opsflow.authentication.return-url';

  private readonly allowedPaths = new Set(['/protected']);

  remember(candidate: string | null): void {
    const safeReturnUrl = this.resolve(candidate);

    if (safeReturnUrl === '/') {
      window.sessionStorage.removeItem(this.storageKey);
      return;
    }

    window.sessionStorage.setItem(this.storageKey, safeReturnUrl);
  }

  consume(): string {
    const storedReturnUrl = window.sessionStorage.getItem(this.storageKey);

    window.sessionStorage.removeItem(this.storageKey);

    return this.resolve(storedReturnUrl);
  }

  private resolve(candidate: string | null): string {
    if (!candidate) {
      return '/';
    }

    try {
      const url = new URL(candidate, window.location.origin);

      if (url.origin !== window.location.origin || !this.allowedPaths.has(url.pathname)) {
        return '/';
      }

      return `${url.pathname}${url.search}${url.hash}`;
    } catch {
      return '/';
    }
  }
}
