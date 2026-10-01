import { Component, computed, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IdentityContextStore } from '../identity-context.store';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-organization-onboarding',
  styleUrl: './organization-onboarding.css',
  templateUrl: './organization-onboarding.html',
})
export class OrganizationOnboarding {
  private readonly identityContextStore = inject(IdentityContextStore);
  private readonly router = inject(Router);

  protected readonly identityState = this.identityContextStore.state;

  protected readonly isSubmitting = computed(() => {
    const state = this.identityState();

    return state.status === 'onboarding-required' && state.submission.status === 'submitting';
  });

  protected readonly organizationNameServerError = computed(() => {
    const state = this.identityState();

    if (state.status !== 'onboarding-required' || state.submission.status !== 'validation-error') {
      return undefined;
    }

    return state.submission.fieldErrors['organizationName'];
  });

  protected readonly organizationNameControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(120)],
  });

  protected async submit(): Promise<void> {
    if (this.organizationNameControl.invalid) {
      this.organizationNameControl.markAsTouched();
      return;
    }

    const formValue = this.organizationNameControl.value.trim();

    await this.identityContextStore.onboardOrganization(formValue);

    const state = this.identityState();

    if (state.status === 'active') {
      await this.router.navigateByUrl('/workspace');
    }
  }
}
