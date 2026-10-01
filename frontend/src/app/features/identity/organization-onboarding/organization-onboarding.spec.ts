import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActiveIdentityContext } from '../identity-context';
import { IdentityContextState, IdentityContextStore } from '../identity-context.store';
import { OrganizationOnboarding } from './organization-onboarding';

describe('OrganizationOnboarding', () => {
  const activeContext: ActiveIdentityContext = {
    status: 'ACTIVE',
    userProfileId: '00000000-0000-0000-0000-000000000001',
    organizationId: '00000000-0000-0000-0000-000000000002',
    organizationName: 'Acme Operations',
    membershipRole: 'OWNER',
  };
  const identityState = signal<IdentityContextState>({
    status: 'onboarding-required',
    submission: { status: 'idle' },
  });
  const identityContextStore = {
    state: identityState.asReadonly(),
    onboardOrganization: vi.fn(),
  };
  const router = {
    navigateByUrl: vi.fn(),
  };

  let component: OrganizationOnboarding;
  let fixture: ComponentFixture<OrganizationOnboarding>;

  beforeEach(async () => {
    identityState.set({ status: 'onboarding-required', submission: { status: 'idle' } });
    identityContextStore.onboardOrganization.mockReset();
    router.navigateByUrl.mockReset();
    router.navigateByUrl.mockResolvedValue(true);

    await TestBed.configureTestingModule({
      imports: [OrganizationOnboarding],
      providers: [
        { provide: IdentityContextStore, useValue: identityContextStore },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OrganizationOnboarding);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps an invalid organization name in the form', async () => {
    enterOrganizationName('   ');

    submitForm();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(identityContextStore.onboardOrganization).not.toHaveBeenCalled();
    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('.field-error')?.textContent).toContain(
      'Enter an organization name.',
    );
  });

  it('navigates to the workspace after successful onboarding', async () => {
    identityContextStore.onboardOrganization.mockImplementation(async () => {
      identityState.set({ status: 'active', context: activeContext });
    });
    enterOrganizationName('  Acme Operations  ');

    submitForm();
    await fixture.whenStable();

    expect(identityContextStore.onboardOrganization).toHaveBeenCalledWith('Acme Operations');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/workspace');
  });

  it('shows backend validation feedback without navigating', async () => {
    identityContextStore.onboardOrganization.mockImplementation(async () => {
      identityState.set({
        status: 'onboarding-required',
        submission: {
          status: 'validation-error',
          fieldErrors: { organizationName: 'Organization name is unavailable' },
        },
      });
    });
    enterOrganizationName('Acme Operations');

    submitForm();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('#organization-name-server-error')?.textContent).toContain(
      'Organization name is unavailable',
    );
  });

  function enterOrganizationName(value: string): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('#organization-name');

    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
  }

  function submitForm(): void {
    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
  }
});
