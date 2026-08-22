export function buildPageTitle(pageName?: string): string {
  const appName = 'OpsFlow';

  const normalizedPageName = pageName?.trim();

  if (normalizedPageName) {
    return `${normalizedPageName} | ${appName}`;
  }
  return appName;
}
