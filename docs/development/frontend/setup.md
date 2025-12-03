# InTicky - Frontend Setup

## Célközönség

Ez a dokumentum a frontend fejlesztési környezet beállítását mutatja be. Frontend fejlesztők számára készült.

## Előfeltételek

- Node.js 18+ (LTS)
- npm vagy yarn
- Git
- IDE (VS Code ajánlott)

## React Projekt Inicializálás

### Vite Használatával (Ajánlott)

**Projekt létrehozása:**
```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
```

**Vagy yarn:**
```bash
yarn create vite frontend --template react-ts
cd frontend
yarn install
```

### Projekt Struktúra

**Alapvető struktúra:**
```
frontend/
├── src/
│   ├── components/     # React komponensek
│   ├── pages/          # Oldalak
│   ├── services/       # API hívások
│   ├── store/          # State management
│   ├── utils/          # Segédfunkciók
│   ├── types/          # TypeScript típusok
│   ├── hooks/          # Custom hooks
│   └── App.tsx         # Fő komponens
├── public/
├── package.json
├── tsconfig.json
├── vite.config.ts
└── .env
```

## TypeScript Konfiguráció

### tsconfig.json

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

## Package Management

### package.json Példa

```json
{
  "name": "inticky-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "test": "vitest",
    "test:ui": "vitest --ui"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "@reduxjs/toolkit": "^2.0.0",
    "react-redux": "^9.0.0",
    "axios": "^1.6.0",
    "zustand": "^4.4.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@vitejs/plugin-react": "^4.2.0",
    "typescript": "^5.2.0",
    "vite": "^5.0.0",
    "vitest": "^1.0.0",
    "@testing-library/react": "^14.0.0",
    "@testing-library/jest-dom": "^6.1.0"
  }
}
```

### Telepítés

**NPM:**
```bash
npm install
```

**Yarn:**
```bash
yarn install
```

## Lokális Fejlesztés Indítása

### Dev Server

**Vite dev server:**
```bash
npm run dev
```

**Elérhető:**
- Local: `http://localhost:5173`
- Network: `http://<your-ip>:5173`

**Hot Module Replacement (HMR):**
- Automatikus újratöltés változásoknál
- State megőrzése (ahol lehetséges)

### Build

**Production build:**
```bash
npm run build
```

**Output:** `dist/` mappa

**Preview:**
```bash
npm run preview
```

## Environment Változók

### .env Fájlok

**.env.development:**
```
VITE_API_URL=http://localhost:8080/api/v1
VITE_APP_NAME=InTicky
VITE_ENABLE_DEV_TOOLS=true
```

**.env.production:**
```
VITE_API_URL=https://api.inticky.com/api/v1
VITE_APP_NAME=InTicky
VITE_ENABLE_DEV_TOOLS=false
```

**Használat:**
```typescript
const apiUrl = import.meta.env.VITE_API_URL;
```

## IDE Beállítások

### VS Code

**Extensions:**
- ESLint
- Prettier
- TypeScript Vue Plugin (Volar)
- Tailwind CSS IntelliSense (ha Tailwind-et használunk)

**Settings (settings.json):**
```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.tsdk": "node_modules/typescript/lib"
}
```

### IntelliJ IDEA / WebStorm

1. **Projekt megnyitása:**
   - `File > Open > frontend`

2. **Node.js beállítása:**
   - `File > Settings > Languages & Frameworks > Node.js`
   - Node interpreter: Node.js 18+

3. **ESLint beállítása:**
   - `File > Settings > Languages & Frameworks > JavaScript > Code Quality Tools > ESLint`

## További Információk

- [Frontend Architektúra](./architecture.md)
- [Coding Standards](./coding-standards.md)
- [Testing](./testing.md)
- [Vite dokumentáció](https://vitejs.dev/)
- [React dokumentáció](https://react.dev/)

## Troubleshooting

### Node modules hiba

**Hiba:** `Cannot find module`

**Megoldás:**
```bash
rm -rf node_modules package-lock.json
npm install
```

### Port már használatban

**Hiba:** `Port 5173 is already in use`

**Megoldás:**
```bash
# Vite config-ban port változtatása
# vite.config.ts
export default defineConfig({
  server: {
    port: 3000
  }
})
```

### TypeScript hiba

**Hiba:** `Cannot find name 'React'`

**Megoldás:**
- React 17+ nem igényli a React importot JSX-hez
- `tsconfig.json`-ban: `"jsx": "react-jsx"`

