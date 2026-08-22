import { buildPageTitle } from './app-title';

describe('buildPageTitle', () => {
  it('returns the app name when no page name is provided', () => {
    const title = buildPageTitle();
    expect(title).toBe('OpsFlow');
  });

  it('returns the page name followed by the app name when a page name is provided', () => {
    const title = buildPageTitle('Dashboard');
    expect(title).toBe('Dashboard | OpsFlow');
  });

  it('trims whitespace from the page name', () => {
    const title = buildPageTitle('  Dashboard  ');
    expect(title).toBe('Dashboard | OpsFlow');
  });

  it('returns the app name when the page name contains only whitespace', () => {
    const title = buildPageTitle('   ');
    expect(title).toBe('OpsFlow');
  });
});
