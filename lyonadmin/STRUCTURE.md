# LyonTaxis Web Admin - Version Web de lyontaxisclient

Cette application est une version web Laravel de l'application mobile Android `lyontaxisclient`.

## Structure du Projet

### Base de Données

#### Models & Relations
- **User** - Utilisateurs avec profil enrichi (phone, birthday, address, etc.)
  - HasMany: Trips, Locations, PaymentMethods, Notifications
- **Trip** - Courses/Réservations
  - BelongsTo: User, Driver
- **Driver** - Chauffeurs disponibles
  - HasMany: Trips
- **Location** - Adresses/Lieux sauvegardés
  - BelongsTo: User
- **PaymentMethod** - Moyens de paiement (cash, carte, etc.)
  - BelongsTo: User
- **Notification** - Notifications utilisateur
  - BelongsTo: User

### API Routes

#### Auth (Public)
- `POST /api/auth/request-otp` - Demander un code OTP
- `POST /api/auth/verify-otp` - Vérifier et créer session
- `POST /api/auth/logout` - Déconnexion
- `GET /api/auth/user` - Profil utilisateur

#### Trips (Protected)
- `GET /api/trips` - Lister les courses
- `POST /api/trips` - Créer une course
- `GET /api/trips/{id}` - Détails d'une course
- `PATCH /api/trips/{id}` - Modifier une course
- `DELETE /api/trips/{id}` - Annuler une course
- `GET /api/trips/{id}/active` - Course active
- `POST /api/trips/{id}/rate` - Noter une course
- `POST /api/trips/{id}/tip` - Ajouter pourboire

#### Drivers (Protected)
- `GET /api/drivers` - Chauffeurs disponibles
- `GET /api/drivers/{id}` - Profil chauffeur

#### Locations (Protected)
- `GET /api/locations` - Lieux populaires
- `POST /api/locations` - Sauvegarder adresse
- `GET /api/locations/popular` - Lieux populaires
- `GET /api/locations/search` - Rechercher adresses

#### User Profile (Protected)
- `GET /api/user/profile` - Profil utilisateur
- `PATCH /api/user/profile` - Mettre à jour profil
- `GET /api/user/payment-methods` - Moyens de paiement
- `POST /api/user/payment-methods` - Ajouter moyen paiement
- `DELETE /api/user/payment-methods/{id}` - Supprimer moyen paiement
- `GET /api/user/notifications` - Notifications
- `PATCH /api/user/notifications/{id}/read` - Marquer comme lu

## Configuration

### Environnement
Le fichier `.env` doit contenir:

```
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

### Authentification
- Utilise Laravel Sanctum pour les tokens API
- Intégration possible avec Supabase pour l'authentification OTP

## Fonctionnalités

### Parity avec l'app Android
✅ Authentification par OTP
✅ Réservation de courses
✅ Historique des courses
✅ Profil utilisateur enrichi
✅ Moyens de paiement multiples
✅ Notifications
✅ Adresses sauvegardées
✅ Notation des chauffeurs
✅ Pourboires
✅ Code de parrainage

## Installation

```bash
# Migrations
php artisan migrate

# Lancer le serveur
php artisan serve

# URL: http://localhost:8000
```

## Écrans/Pages Prévus (Frontend)

- Login/Signup par OTP
- Dashboard/Accueil (recherche de taxi)
- Selection du type de taxi
- Suivi de la course en temps réel
- Chat avec chauffeur
- Historique des courses
- Profil utilisateur
- Portefeuille/Paiements
- Notifications
- Factures/Invoices
- Paramètres

## Prochaines Étapes

1. Implémenter les contrôleurs API avec la logique complète
2. Créer des services (OTP, Pricing, Tracking)
3. Seeders pour données de test (chauffeurs, lieux populaires)
4. Frontend Vue.js/Inertia ou React
5. Tests unitaires et d'intégration
6. WebSockets pour le suivi en temps réel
7. Intégration Supabase pour l'authentification OTP
