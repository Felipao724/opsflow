import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticationClient } from './authentication-client';

export const apiBearerInterceptor: HttpInterceptorFn = (request, next) => {
  if (!isOpsFlowApiRequest(request.url)) {
    return next(request);
  }

  const authenticationClient = inject(AuthenticationClient);

  return from(authenticationClient.getValidAccessToken()).pipe(
    switchMap((accessToken) => {
      if (!accessToken) {
        return next(request);
      }

      return next(
        request.clone({
          setHeaders: {
            Authorization: `Bearer ${accessToken}`,
          },
        }),
      );
    }),
  );
};

function isOpsFlowApiRequest(requestUrl: string): boolean {
  const apiBaseUrl = new URL(environment.apiBaseUrl);
  const resolvedRequestUrl = new URL(requestUrl, window.location.origin);
  const apiBasePath = apiBaseUrl.pathname.replace(/\/+$/, '') || '/';

  const isAllowedPath =
    apiBasePath === '/'
      ? resolvedRequestUrl.pathname.startsWith('/')
      : resolvedRequestUrl.pathname === apiBasePath ||
        resolvedRequestUrl.pathname.startsWith(`${apiBasePath}/`);

  return resolvedRequestUrl.origin === apiBaseUrl.origin && isAllowedPath;
}
