# OpsFlow Frontend

Angular frontend for OpsFlow.

## Requirements

- Node.js 24.15 or newer
- npm 11 or newer

The project uses npm as its package manager. The exact dependency versions are recorded in `package-lock.json`.

## Install dependencies

From the `frontend` directory, run:

```bash
npm ci
```

## Development server

Start the local development server:

```bash
npm start
```

Open [http://localhost:4200](http://localhost:4200) in your browser. The application reloads automatically when source files change.

## Run tests

Execute the Vitest test suite once:

```bash
npm test -- --watch=false
```

For watch mode during development:

```bash
npm test
```

## Testing conventions

- Test files live next to the source file they exercise and use the `.spec.ts` suffix.
- Component tests use Angular `TestBed` and assert user-visible behavior in the DOM.
- Pure functions are tested directly without starting Angular or using `TestBed`.
- Unit tests must not make real requests to external services. Replace external boundaries with controlled test doubles or Angular's dedicated testing utilities.
- Prefer focused behavioral assertions over broad snapshots and generated tests that only verify that an instance was created.

Vitest runs through the Angular CLI test builder configured in `angular.json`. TypeScript test settings are located in `tsconfig.spec.json`, while test commands and dependency versions are defined in `package.json`.

Use `npm test` while developing to keep Vitest watching for file changes. Use `npm test -- --watch=false` for a single headless run in continuous integration or before committing changes.

## Production build

Create an optimized production build:

```bash
npm run build
```

Build artifacts are generated under `dist/` and are not committed to version control.

## Project baseline

The frontend uses:

- Angular 22
- Standalone components
- Zoneless change detection
- Angular Router
- Strict TypeScript and Angular template checking
- Vitest
- CSS

No UI library, design system, backend integration, or business functionality has been introduced yet.
