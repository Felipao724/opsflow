import { Component } from '@angular/core';

@Component({
  selector: 'app-authenticated-area',
  standalone: true,
  template: `
    <section>
      <h2>Protected Area</h2>
      <p>This content requires an authenticated session</p>
    </section>
  `,
})
export class AuthenticatedAreaComponent {}
