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
