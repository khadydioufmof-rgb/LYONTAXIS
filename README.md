# LyonTaxis

Plateforme de transport composee de trois applications clientes et d'un backend API centralise.

## Architecture

| Dossier | Role | Technologie |
| --- | --- | --- |
| `lyonadministrateur` | Backend ERP, API et console web d'administration | Laravel 13, PHP 8.5, Sanctum |
| `lyonadmin` | Application web historique et interface frontend | Laravel, Vite, Tailwind CSS |
| `lyontaxisclient` | Application mobile passager | Android, Kotlin, Jetpack Compose |
| `lyontaxischauffeur` | Application mobile chauffeur | Android, Kotlin, Jetpack Compose |

Le backend central expose les plateformes suivantes :

- `admin` : supervision, chauffeurs, courses et clients
- `driver` : authentification chauffeur et courses affectees
- `client` : reservation, profil, historique et notifications

## Backend ERP

Le service principal est `lyonadministrateur`.

### Demarrage local

Depuis PowerShell :

```powershell
Set-Location C:\laragon\www\LYONTAXIS\lyonadministrateur
& C:\laragon\bin\php\php-8.5.4-nts-Win32-vs17-x64\php.exe artisan serve --host=127.0.0.1 --port=8001
```

URLs principales :

- Console ERP : `http://127.0.0.1:8001/admin`
- Login admin : `http://127.0.0.1:8001/admin/login`
- Healthcheck : `http://127.0.0.1:8001/api/v1/shared/health`

### Identifiants locaux

Le compte de demonstration est cree par le seeder `AdminUserSeeder`. Les valeurs sont configurees dans `.env` :

```dotenv
ERP_ADMIN_EMAIL=admin@lyontaxis.local
ERP_ADMIN_PASSWORD=ChangeMe123!
```

Changez ce mot de passe avant toute utilisation hors environnement local.

### Installation et base de donnees

```powershell
composer install
Copy-Item .env.example .env
php artisan key:generate
php artisan migrate --force
php artisan db:seed --class=AdminUserSeeder
```

Le backend utilise PostgreSQL. Les variables obligatoires sont `DB_CONNECTION`, `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME` et `DB_PASSWORD`.

## API v1

Toutes les routes API sont prefixees par `/api/v1`.

### Authentification

- Client : `POST /client/auth/request-otp`, `POST /client/auth/verify-otp`
- Chauffeur : `POST /driver/auth/login`
- Admin API : `POST /admin/auth/request-otp`, `POST /admin/auth/verify-otp`

Les routes protegees utilisent un token Laravel Sanctum :

```http
Authorization: Bearer <token>
Accept: application/json
```

Les roles sont controles par plateforme : `admin`, `driver` et `client`.

### Gestion admin

- `GET /admin/dashboard/stats`
- `GET|POST|PUT|DELETE /admin/drivers`
- `GET /admin/trips`
- `PATCH /admin/trips/{trip}/status`
- `GET|PUT /admin/users`

## Applications Android

Les deux applications Android utilisent l'URL configuree par `LYONTAXIS_API_URL`.

Pour un emulateur Android local :

```dotenv
LYONTAXIS_API_URL=http://10.0.2.2:8001/api/v1
```

Pour la production, utilisez une URL HTTPS publique :

```dotenv
LYONTAXIS_API_URL=https://api.example.com/api/v1
```

Build client passager :

```powershell
Set-Location lyontaxisclient
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

Build application chauffeur :

```powershell
Set-Location lyontaxischauffeur
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

## Frontend web

```powershell
Set-Location lyonadmin
npm ci
npm run build
```

Le frontend peut cibler l'ERP avec :

```dotenv
VITE_LYONTAXIS_API_URL=http://localhost:8001/api/v1
```

## CI/CD

Le workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) est declenche sur les push et pull requests vers `main` ou `master`.

Il verifie :

- syntaxe PHP, migrations, routes et vues Laravel
- tests Laravel avec PostgreSQL
- installation propre et build Vite
- tests et compilation Android client
- tests et compilation Android chauffeur
- publication des APK debug comme artefacts GitHub

## Production

Avant de publier :

1. Configurer un hebergement HTTPS et un domaine API.
2. Utiliser `APP_ENV=production` et `APP_DEBUG=false`.
3. Generer un nouvel `APP_KEY` et remplacer tous les secrets locaux.
4. Configurer un vrai fournisseur SMS ou e-mail pour l'OTP.
5. Remplacer le mot de passe PostgreSQL expose ou utilise en local.
6. Configurer les secrets GitHub pour les builds release Android.
7. Ajouter Firebase, Google Maps et Stripe selon les fonctionnalites activees.
8. Mettre en place sauvegardes, monitoring, rate limiting et journalisation.

Les fichiers `.env` et les cles privees ne doivent jamais etre commites.

## Etat du projet

Le backend API, la console ERP, l'authentification Sanctum et les builds Android sont operationnels en local. L'envoi OTP reel, le deploiement serveur et les signatures release Android doivent encore etre configures pour une mise en production complete.
