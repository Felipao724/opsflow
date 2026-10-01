import { Component, computed, inject } from '@angular/core';
import { IdentityContextStore } from '../../identity/identity-context.store';

@Component({
  imports: [],
  selector: 'app-organization-workspace',
  styleUrl: './organization-workspace.css',
  templateUrl: './organization-workspace.html',
})
export class OrganizationWorkspace {
  private readonly identityContextStore = inject(IdentityContextStore);

  protected readonly activeContext = computed(() => {
    const state = this.identityContextStore.state();

    return state.status === 'active' ? state.context : undefined;
  });
}
