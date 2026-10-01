export type MembershipRole = 'OWNER';

export type IdentityContext = OnboardingRequiredIdentityContext | ActiveIdentityContext;

export type OnboardingRequiredIdentityContext = { readonly status: 'ONBOARDING_REQUIRED' };

export type ActiveIdentityContext = {
  readonly status: 'ACTIVE';
  readonly userProfileId: string;
  readonly organizationId: string;
  readonly organizationName: string;
  readonly membershipRole: MembershipRole;
};
