import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IdentityContextStore } from '../../identity/identity-context.store';
import { OrganizationWorkspace } from './organization-workspace';

describe('OrganizationWorkspace', () => {
  let component: OrganizationWorkspace;
  let fixture: ComponentFixture<OrganizationWorkspace>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrganizationWorkspace],
      providers: [
        {
          provide: IdentityContextStore,
          useValue: {
            state: signal({
              status: 'active',
              context: {
                status: 'ACTIVE',
                userProfileId: 'user-profile-id',
                organizationId: 'organization-id',
                organizationName: 'Northwind Operations',
                membershipRole: 'OWNER',
              },
            }).asReadonly(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OrganizationWorkspace);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the active organization context', () => {
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain('Northwind Operations');
    expect(element.querySelector('.role-badge')?.textContent).toContain('OWNER');
    expect(element.querySelector('.workspace-context')?.textContent).toContain('user-profile-id');
    expect(element.querySelector('.workspace-context')?.textContent).toContain('organization-id');
  });
});
