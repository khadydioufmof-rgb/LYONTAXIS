🚕 Lyontaxis — Plateforme VTC Mobile & WebDécouvrir les fonctionnalités • Consulter l'architecture • Guide d'installation📋 Table des matières🔍 Architecture Système✨ Fonctionnalités Clefs🛠 Stack Technique⚙️ Configuration Requise🚀 Installation & Démarrage📂 Structure du Projet📄 Licence🔍 Architecture Système[!NOTE]La plateforme repose sur une architecture modulaire communiquant via des APIs REST et des WebSockets pour la géolocalisation haute fréquence.Plaintext┌─────────────────────────┐       ┌─────────────────────────┐
│   Application Mobile    │       │     Plateforme Web      │
│   (Client & Chauffeur)  │       │     (Admin & Client)    │
└────────────┬────────────┘       └────────────┬────────────┘
             │                                 │
             └────────────────┬────────────────┘
                              │ (HTTPS / WS)
                              ▼
                   ┌─────────────────────┐
                   │  API Node.js Server │
                   └──────────┬──────────┘
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
    ┌──────────────────┐           ┌──────────────────┐
    │  PostgreSQL/Redis│           │ Services Tiers   │
    │  (Données/Cache) │           │ (Stripe, Maps)   │
    └──────────────────┘           └──────────────────┘
✨ Fonctionnalités Clefs📱 Applications Mobiles (Client & Chauffeur)Client :📍 Réservation immédiate ou planifiée (Gares, Aéroports Lyon-Saint Exupéry).💰 Estimation automatique du tarif avant validation.🛰️ Suivi en temps réel du véhicule sur carte interactive.💳 Paiement sécurisé (Stripe, Apple Pay, Google Pay).📄 Génération automatique de factures PDF.Chauffeur :🟢 Commutation de statut (En ligne / Hors ligne).🔔 Prise en charge rapide des demandes à proximité.🗺️ Navigation GPS intégrée.📊 Suivi quotidien des courses et revenus.💻 Interface Web (Client & Administration)Client Web : Réservation directe sans téléchargement d'application.Supervision Admin : Dispatching des courses, modération des comptes chauffeurs, gestion des grilles tarifaires métropolitaines.🛠 Stack TechniqueDomaineTechnologies UtiliséesMobileReact Native, React Navigation, Redux ToolkitWeb FrontNext.js, React, Tailwind CSSBack-EndNode.js, Express, Socket.ioBase de donnéesPostgreSQL, Prisma ORM, RedisCartographieGoogle Maps API / Mapbox GLPaiementsStripe API⚙️ Configuration RequiseNode.js v18.0.0 ou supérieurnpm v9+ ou yarn / pnpmPostgreSQL v14+Xcode (pour le build iOS sur macOS)Android Studio (pour le build Android)🚀 Installation & Démarrage1. Clonage du dépôtBashgit clone https://github.com/votre-compte/lyontaxis.git
cd lyontaxis
2. Variables d'environnement[!WARNING]Ne poussez jamais vos clés privées sur GitHub. Assurez-vous que vos fichiers .env sont bien ignorés par Git.Copiez les modèles d'environnement dans chaque module :Bashcp backend/.env.example backend/.env
cp mobile/.env.example mobile/.env
cp web/.env.example web/.env
3. Exécution des modulesBack-End APIBashcd backend
npm install
npm run dev
Interface WebBashcd web
npm install
npm run dev
# Accessible sur http://localhost:3000
Application MobileBashcd mobile
npm install

# Pour iOS (macOS uniquement)
cd ios && pod install && cd ..
npx react-native run-ios

# Pour Android
npx react-native run-android
📂 Structure du ProjetPlaintextlyontaxis/
├── 📁 backend/       # API Node.js, WebSockets & ORM
├── 📁 mobile/        # Application React Native (iOS/Android)
├── 📁 web/           # Application Web Next.js (Dashboard & Booking)
├── 📁 docs/          # Schemas d'architecture et doc API
└── 📄 README.md      # Documentation principale
📄 LicenceProjet distribué sous la licence MIT. Consulter le fichier LICENSE pour plus d'informations.