import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActiveIdentityContext } from './identity-context';
import { IdentityContextClient } from './identity-context.client';

export type OnboardingSubmissionState =
  | { readonly status: 'idle' }
  | { readonly status: 'submitting' }
  | { readonly status: 'validation-error'; readonly fieldErrors: Readonly<Record<string, string>> }
  | { readonly status: 'failure' };

export type IdentityContextState =
  | { readonly status: 'idle' }
  | { readonly status: 'loading'; readonly operation: 'loading-context' | 'recovering-conflict' }
  | { readonly status: 'onboarding-required'; readonly submission: OnboardingSubmissionState }
  | { readonly status: 'active'; readonly context: ActiveIdentityContext }
  | { readonly status: 'failure' };

type ValidationProblemDetail = {
  readonly code: 'VALIDATION_ERROR';
  readonly fieldErrors: Readonly<Record<string, string>>;
};

function hasProblemCode(value: unknown, code: string): boolean {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  return (value as Record<string, unknown>)['code'] === code;
}

function isValidationProblemDetail(value: unknown): value is ValidationProblemDetail {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const candidate = value as Record<string, unknown>;
  const fieldErrors = candidate['fieldErrors'];

  if (
    candidate['code'] !== 'VALIDATION_ERROR' ||
    typeof fieldErrors !== 'object' ||
    fieldErrors === null ||
    Array.isArray(fieldErrors)
  ) {
    return false;
  }

  return Object.values(fieldErrors).every((message) => typeof message === 'string');
}

@Service()
export class IdentityContextStore {
  private readonly identityContextClient = inject(IdentityContextClient);
  private readonly identityState = signal<IdentityContextState>({ status: 'idle' });

  readonly state = this.identityState.asReadonly();

  load(): Promise<void> {
    return this.loadContext('loading-context');
  }

  async onboardOrganization(organizationName: string): Promise<void> {
    const currentState = this.identityState();

    if (currentState.status !== 'onboarding-required') return;

    this.identityState.set({ status: 'onboarding-required', submission: { status: 'submitting' } });

    try {
      const context = await firstValueFrom(
        this.identityContextClient.onboardOrganization(organizationName),
      );

      this.identityState.set({ status: 'active', context });
    } catch (error: unknown) {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 400 &&
        isValidationProblemDetail(error.error)
      ) {
        this.identityState.set({
          status: 'onboarding-required',
          submission: {
            status: 'validation-error',
            fieldErrors: error.error.fieldErrors,
          },
        });

        return;
      }

      if (
        error instanceof HttpErrorResponse &&
        error.status === 409 &&
        hasProblemCode(error.error, 'USER_ALREADY_ONBOARDED')
      ) {
        await this.loadContext('recovering-conflict');
        return;
      }

      this.identityState.set({
        status: 'onboarding-required',
        submission: { status: 'failure' },
      });
    }
  }

  private async loadContext(operation: 'loading-context' | 'recovering-conflict'): Promise<void> {
    this.identityState.set({ status: 'loading', operation });

    try {
      const context = await firstValueFrom(this.identityContextClient.getCurrent());

      if (context.status === 'ONBOARDING_REQUIRED') {
        this.identityState.set({ status: 'onboarding-required', submission: { status: 'idle' } });
      }

      if (context.status === 'ACTIVE') {
        this.identityState.set({ status: 'active', context });
      }
    } catch {
      this.identityState.set({ status: 'failure' });
    }
  }
}
