import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  it('renders the OpsFlow product identity', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('h1')?.textContent?.trim()).toBe('OpsFlow');
    expect(element.querySelector('p')?.textContent?.trim()).toBe(
      'Operations workflow management.',
    );
  });
});
