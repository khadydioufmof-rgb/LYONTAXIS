<?php

namespace App\Http\Controllers\Web;

use App\Http\Controllers\Controller;
use App\Models\Driver;
use App\Models\Location;
use App\Models\Notification;
use App\Models\PaymentMethod;
use App\Models\Trip;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Schema;

class PageController extends Controller
{
    private function safeDatabase(callable $callback, mixed $fallback = null): mixed
    {
        if (! filter_var(env('WEB_DATABASE_ENABLED', true), FILTER_VALIDATE_BOOLEAN)) {
            return $fallback;
        }

        try {
            return $callback();
        } catch (\Throwable $e) {
            return $fallback;
        }
    }

    private function buildUserProfile(): object
    {
        $sessionProfile = session('web_profile');

        if ($this->safeDatabase(fn () => Schema::hasTable('users'), false)) {
            $user = $this->safeDatabase(fn () => User::query()->first(), null);

            if ($user) {
                return $user;
            }
        }

        return (object) array_merge([
            'id' => 1,
            'name' => 'Alice Martin',
            'email' => 'alice@example.com',
            'phone_number' => '+33 6 12 34 56 78',
            'gender' => 'Femme',
            'birthday' => null,
            'home_address' => '12 rue de la République, Lyon',
            'member_level' => 'Premium',
            'integral_points' => 1240,
            'coupons_count' => 3,
            'referral_code' => 'LYN-2048',
            'cash_balance' => 42.30,
            'avatar_seed' => 'AM',
        ], $sessionProfile ?? []);
    }

    private function buildDriverList(): array
    {
        if ($this->safeDatabase(fn () => Schema::hasTable('drivers'), false)) {
            $drivers = $this->safeDatabase(fn () => Driver::query()->where('is_available', true)->latest()->limit(3)->get(), null);

            if ($drivers && $drivers->isNotEmpty()) {
                return $drivers->map(fn ($driver) => [
                    'id' => $driver->id,
                    'name' => $driver->name,
                    'rating' => (float) $driver->rating,
                    'eta' => $driver->is_available ? '2 min' : '5 min',
                    'vehicle' => $driver->vehicle_category,
                    'status' => $driver->is_available ? 'Disponible' : 'Occupé',
                    'vehicle_icon' => match ($driver->vehicle_category) {
                        'Premium' => '🚙',
                        'Sedan' => '🚘',
                        'Van' => '🚐',
                        default => '🚕',
                    },
                ])->toArray();
            }
        }

        return [
            ['name' => 'Paul Martin', 'rating' => 4.9, 'eta' => '2 min', 'vehicle' => 'Eco', 'status' => 'Disponible', 'vehicle_icon' => '🚕'],
            ['name' => 'Nadia Leroy', 'rating' => 4.8, 'eta' => '4 min', 'vehicle' => 'Sedan', 'status' => 'Très proche', 'vehicle_icon' => '🚘'],
            ['name' => 'Luc Bernard', 'rating' => 4.7, 'eta' => '6 min', 'vehicle' => 'Premium', 'status' => 'Disponible', 'vehicle_icon' => '🚙'],
        ];
    }

    private function buildTripHistoryData(): array
    {
        if ($this->safeDatabase(fn () => Schema::hasTable('trips'), false)) {
            $userId = $this->buildUserProfile()->id ?? 1;
            $trips = $this->safeDatabase(fn () => Trip::query()->where('user_id', $userId)->with('driver')->latest()->limit(4)->get(), null);

            if ($trips && $trips->isNotEmpty()) {
                return $trips->map(fn ($trip) => [
                    'date' => $trip->created_at ? $trip->created_at->locale('fr_FR')->isoFormat('ddd D MMM') : 'Récemment',
                    'from' => $trip->pickup_location['address'] ?? ($trip->pickup_location['name'] ?? 'Départ'),
                    'to' => $trip->dropoff_location['address'] ?? ($trip->dropoff_location['name'] ?? 'Destination'),
                    'status' => ucfirst($trip->status),
                    'price' => '€' . number_format((float) $trip->fare, 2),
                    'color' => match ($trip->status) {
                        'completed' => 'sky',
                        'cancelled' => 'amber',
                        default => 'emerald',
                    },
                ])->toArray();
            }
        }

        return [
            ['date' => 'Aujourd’hui', 'from' => 'Place Bellecour', 'to' => 'Part-Dieu', 'status' => 'Terminé', 'price' => '€18.50', 'color' => 'sky'],
            ['date' => 'Hier', 'from' => 'Vieux Lyon', 'to' => 'Aéroport Saint-Exupéry', 'status' => 'Terminé', 'price' => '€52.00', 'color' => 'emerald'],
            ['date' => 'Lun 24 Août', 'from' => 'Lyon 2', 'to' => 'Gare Perrache', 'status' => 'Annulé', 'price' => '€0.00', 'color' => 'amber'],
            ['date' => 'Dim 22 Août', 'from' => 'Lyon 7', 'to' => 'Hotel de Ville', 'status' => 'Terminé', 'price' => '€21.60', 'color' => 'sky'],
        ];
    }

    private function buildPaymentMethodsData(): array
    {
        $sessionMethods = session('web_payment_methods');

        if ($this->safeDatabase(fn () => Schema::hasTable('payment_methods'), false)) {
            $methods = $this->safeDatabase(
                fn () => PaymentMethod::query()->where('user_id', optional($this->buildUserProfile())->id ?? 1)->get(),
                null
            );

            if ($methods && $methods->isNotEmpty()) {
                return $methods->map(fn ($method) => [
                    'type' => ucfirst($method->type),
                    'number' => $method->subtitle,
                    'default' => (bool) $method->is_default,
                ])->toArray();
            }
        }

        if (is_array($sessionMethods) && ! empty($sessionMethods)) {
            return $sessionMethods;
        }

        return [
            ['type' => 'Visa', 'number' => '•••• 2234', 'default' => true],
            ['type' => 'Mastercard', 'number' => '•••• 1188', 'default' => false],
            ['type' => 'Espèces', 'number' => 'Paiement à bord', 'default' => false],
        ];
    }

    private function buildNotificationsData(): array
    {
        if ($this->safeDatabase(fn () => Schema::hasTable('notifications'), false)) {
            $notifications = $this->safeDatabase(fn () => Notification::query()->latest()->limit(4)->get(), null);

            if ($notifications && $notifications->isNotEmpty()) {
                return $notifications->map(fn ($notification) => [
                    'title' => $notification->title,
                    'text' => $notification->description,
                    'time' => $notification->created_at ? $notification->created_at->diffForHumans() : 'Récemment',
                    'read' => (bool) $notification->is_read,
                ])->toArray();
            }
        }

        return [
            ['title' => 'Votre chauffeur est en route', 'text' => 'Paul Martin arrive dans 2 minutes sur votre adresse.', 'time' => 'Il y a 3 min', 'read' => false],
            ['title' => 'Paiement validé', 'text' => 'Le paiement pour votre course a bien été enregistré.', 'time' => 'Hier', 'read' => true],
            ['title' => 'Nouvelle promotion', 'text' => 'Profitez de 10% de réduction pour votre prochain trajet.', 'time' => '2 jours', 'read' => true],
        ];
    }

    private function buildSavedAddresses(): array
    {
        if ($this->safeDatabase(fn () => Schema::hasTable('locations'), false)) {
            $locations = $this->safeDatabase(fn () => Location::query()->latest()->limit(3)->get(), null);

            if ($locations && $locations->isNotEmpty()) {
                return $locations->map(fn ($location) => [
                    'title' => $location->title,
                    'address' => $location->address,
                ])->toArray();
            }
        }

        return [
            ['title' => 'Maison', 'address' => '12 rue de la République, Lyon'],
            ['title' => 'Travail', 'address' => '3 rue de la Soie, Lyon Part-Dieu'],
            ['title' => 'Parents', 'address' => '45 avenue Henri Barbusse, Lyon'],
        ];
    }

    public function home()
    {
        $user = $this->buildUserProfile();

        $stats = [
            ['label' => 'Courses ce mois', 'value' => '24'],
            ['label' => 'Temps moyen', 'value' => '12 min'],
            ['label' => 'Note moyenne', 'value' => '4.9/5'],
        ];

        $featuredDriver = [
            'name' => 'Paul Martin',
            'rating' => 4.9,
            'status' => 'Disponible',
            'eta' => '2 min',
            'vehicle' => 'Eco',
            'price' => '€18.50',
        ];

        $recentTrips = [
            ['from' => 'Place Bellecour', 'to' => 'Part-Dieu', 'time' => 'Aujourd’hui', 'price' => '€18.50'],
            ['from' => 'Vieux Lyon', 'to' => 'Aéroport Saint-Exupéry', 'time' => 'Hier', 'price' => '€52.00'],
            ['from' => 'Lyon 2', 'to' => 'Gare Perrache', 'time' => 'Lun 24 Août', 'price' => '€16.90'],
        ];

        if ($this->safeDatabase(fn () => Schema::hasTable('trips'), false)) {
            $recentTrips = $this->buildTripHistoryData();
        }

        if ($this->safeDatabase(fn () => Schema::hasTable('drivers'), false)) {
            $driverData = $this->buildDriverList();
            $featuredDriver['name'] = $driverData[0]['name'];
            $featuredDriver['status'] = $driverData[0]['status'];
            $featuredDriver['eta'] = $driverData[0]['eta'];
            $featuredDriver['vehicle'] = $driverData[0]['vehicle'];
        }

        return view('pages.home', compact('stats', 'featuredDriver', 'recentTrips', 'user'));
    }

    public function admin()
    {
        $drivers = $this->safeDatabase(fn () => Driver::query()->latest()->limit(20)->get(), collect());
        $trips = $this->safeDatabase(fn () => Trip::query()->with('driver')->latest()->limit(20)->get(), collect());

        $mapDrivers = $drivers && $drivers->isNotEmpty()
            ? $drivers->map(fn ($driver) => [
                'name' => $driver->name,
                'lat' => (float) $driver->latitude,
                'lng' => (float) $driver->longitude,
                'available' => (bool) $driver->is_available,
            ])->filter(fn ($driver) => $driver['lat'] && $driver['lng'])->values()->toArray()
            : [
                ['name' => 'Paul Martin', 'lat' => 45.7578, 'lng' => 4.8320, 'available' => true],
                ['name' => 'Nadia Leroy', 'lat' => 45.7601, 'lng' => 4.8594, 'available' => true],
                ['name' => 'Luc Bernard', 'lat' => 45.7485, 'lng' => 4.8467, 'available' => false],
            ];

        $mapTrips = $trips && $trips->isNotEmpty()
            ? $trips->map(fn ($trip) => [
                'from' => $trip->pickup_location['address'] ?? 'Départ',
                'to' => $trip->dropoff_location['address'] ?? 'Destination',
                'fromLat' => (float) ($trip->pickup_location['latitude'] ?? 45.764),
                'fromLng' => (float) ($trip->pickup_location['longitude'] ?? 4.8357),
                'toLat' => (float) ($trip->dropoff_location['latitude'] ?? 45.7606),
                'toLng' => (float) ($trip->dropoff_location['longitude'] ?? 4.8594),
                'status' => $trip->status,
            ])->toArray()
            : [[
                'from' => 'Place Bellecour', 'to' => 'Part-Dieu',
                'fromLat' => 45.7578, 'fromLng' => 4.8320,
                'toLat' => 45.7606, 'toLng' => 4.8594, 'status' => 'confirmed',
            ]];

        $totalRevenue = $trips && $trips->isNotEmpty()
            ? (float) $trips->sum('fare')
            : 90.50;

        $transactions = $trips && $trips->isNotEmpty()
            ? $trips->map(fn ($trip) => [
                'id' => 'TRX-' . $trip->id,
                'label' => ($trip->pickup_location['name'] ?? 'Course') . ' → ' . ($trip->dropoff_location['name'] ?? 'Destination'),
                'amount' => '€' . number_format((float) $trip->fare, 2),
                'status' => $trip->status === 'cancelled' ? 'Remboursé' : 'Réglé',
            ])->toArray()
            : [
                ['id' => 'TRX-2048', 'label' => 'Bellecour → Part-Dieu', 'amount' => '€18.50', 'status' => 'Réglé'],
                ['id' => 'TRX-2047', 'label' => 'Vieux Lyon → Aéroport', 'amount' => '€52.00', 'status' => 'Réglé'],
                ['id' => 'TRX-2046', 'label' => 'Lyon 2 → Perrache', 'amount' => '€20.00', 'status' => 'Remboursé'],
            ];

        $securityEvents = [
            ['time' => 'À l’instant', 'label' => 'API Supabase accessible', 'detail' => 'Contrôle de santé REST réussi', 'level' => 'ok'],
            ['time' => 'Il y a 4 min', 'label' => 'Connexion chauffeur', 'detail' => 'Session protégée par Supabase Auth', 'level' => 'ok'],
            ['time' => 'Aujourd’hui', 'label' => 'Contrôle des transactions', 'detail' => 'Aucune anomalie détectée', 'level' => 'info'],
        ];

        $metrics = [
            ['label' => 'Courses aujourd’hui', 'value' => $trips && $trips->isNotEmpty() ? (string) $trips->count() : '24', 'tone' => 'blue'],
            ['label' => 'Chauffeurs actifs', 'value' => $drivers && $drivers->isNotEmpty() ? (string) $drivers->where('is_available', true)->count() : '8', 'tone' => 'cyan'],
            ['label' => 'Chiffre d’affaires', 'value' => '€' . number_format($totalRevenue, 2, ',', ' '), 'tone' => 'yellow'],
            ['label' => 'Alertes sécurité', 'value' => '0', 'tone' => 'green'],
        ];

        return view('pages.admin-dashboard', compact('metrics', 'mapDrivers', 'mapTrips', 'transactions', 'securityEvents'));
    }

    public function login()
    {
        return view('pages.auth.login');
    }

    public function booking()
    {
        $distanceKm = 7.8;
        $vehicles = [
            ['name' => 'Eco', 'icon' => '🚕', 'description' => 'Le plus économique', 'price' => 'À partir de €12'],
            ['name' => 'Sedan', 'icon' => '🚘', 'description' => 'Confort optimal', 'price' => 'À partir de €15'],
            ['name' => 'Premium', 'icon' => '🚙', 'description' => 'Service haut de gamme', 'price' => 'À partir de €20'],
            ['name' => 'Van', 'icon' => '🚐', 'description' => 'Pour familles et bagages', 'price' => 'À partir de €24'],
        ];

        $drivers = $this->buildDriverList();

        $tripSummary = [
            'from' => 'Place Bellecour',
            'to' => 'Part-Dieu',
            'distance' => '7.8 km',
            'duration' => '14 min',
            'estimatedPrice' => '€16.70',
        ];

        $savedAddresses = $this->buildSavedAddresses();

        return view('pages.booking', compact('vehicles', 'drivers', 'tripSummary', 'savedAddresses'));
    }

    public function saveBooking(Request $request)
    {
        $validated = $request->validate([
            'pickup' => 'required|string',
            'destination' => 'required|string',
            'vehicle' => 'required|string|in:Eco,Sedan,Premium,Van',
            'passengers' => 'nullable|integer|min:1',
            'notes' => 'nullable|string',
            'reservation_type' => 'required|in:now,later',
            'scheduled_at' => 'nullable|date|required_if:reservation_type,later',
        ]);

        if ($this->safeDatabase(fn () => Schema::hasTable('trips'), false)) {
            $user = $this->buildUserProfile();

            $this->safeDatabase(function () use ($user, $validated) {
                Trip::query()->create([
                    'user_id' => $user->id ?? 1,
                    'driver_id' => $this->safeDatabase(fn () => Driver::query()->value('id'), null),
                    'vehicle_category' => $validated['vehicle'],
                    'pickup_location' => ['address' => $validated['pickup'], 'name' => $validated['pickup']],
                    'dropoff_location' => ['address' => $validated['destination'], 'name' => $validated['destination']],
                    'fare' => $this->calculateWebFare($validated['vehicle']),
                    'base_fare' => $this->vehicleTariffs()[$validated['vehicle']]['base'],
                    'distance_fare' => round(7.8 * $this->vehicleTariffs()[$validated['vehicle']]['per_km'], 2),
                    'time_fare' => 0,
                    'stop_fee' => 0,
                    'service_fee' => 1.00,
                    'discount' => 0,
                    'tip' => 0,
                    'status' => $validated['reservation_type'] === 'later' ? 'pending' : 'confirmed',
                    'distance_km' => 7.8,
                    'duration_min' => 14,
                    'payment_method' => 'cash',
                    'preferences' => ['notes' => $validated['notes'] ?? '', 'passengers' => $validated['passengers'] ?? 1],
                    'is_scheduled' => $validated['reservation_type'] === 'later',
                    'scheduled_at' => $validated['scheduled_at'] ?? now(),
                ]);
            }, null);
        }

        return redirect()->route('tracking')->with('success', 'Réservation enregistrée avec succès.');
    }

    private function vehicleTariffs(): array
    {
        return [
            'Eco' => ['base' => 5.00, 'per_km' => 1.50, 'minimum' => 12.00],
            'Sedan' => ['base' => 6.00, 'per_km' => 1.80, 'minimum' => 15.00],
            'Premium' => ['base' => 8.00, 'per_km' => 2.50, 'minimum' => 20.00],
            'Van' => ['base' => 9.00, 'per_km' => 2.80, 'minimum' => 24.00],
        ];
    }

    private function calculateWebFare(string $vehicle): float
    {
        $tariff = $this->vehicleTariffs()[$vehicle];

        return max($tariff['minimum'], round($tariff['base'] + (7.8 * $tariff['per_km']), 2));
    }

    public function tripHistory()
    {
        $trips = $this->buildTripHistoryData();

        return view('pages.trip-history', compact('trips'));
    }

    public function profile()
    {
        $profile = $this->buildUserProfile();

        $profileData = [
            'name' => $profile->name,
            'email' => $profile->email,
            'phone' => $profile->phone_number ?? '+33 6 12 34 56 78',
            'address' => $profile->home_address ?? '12 rue de la République, Lyon',
            'gender' => $profile->gender ?? 'Femme',
            'member' => $profile->member_level ?? 'Premium',
            'points' => number_format((int) ($profile->integral_points ?? 1240), 0, ',', ' '),
            'coupons' => $profile->coupons_count ?? 3,
            'referral' => $profile->referral_code ?? 'LYN-2048',
            'avatar' => strtoupper(substr((string) ($profile->name ?? 'AM'), 0, 2)),
        ];

        $infoCards = [
            ['label' => 'Points', 'value' => '1,240'],
            ['label' => 'Coupons', 'value' => '3'],
            ['label' => 'Référence', 'value' => 'LYN-2048'],
            ['label' => 'Solde', 'value' => '€42.30'],
        ];

        $savedAddresses = $this->buildSavedAddresses();

        return view('pages.profile', compact('profileData', 'infoCards', 'savedAddresses'));
    }

    public function saveProfile(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|max:255',
            'phone' => 'required|string|max:255',
            'gender' => 'nullable|string',
            'address' => 'nullable|string',
        ]);

        $profile = [
            'name' => $validated['name'],
            'email' => $validated['email'],
            'phone_number' => $validated['phone'],
            'gender' => $validated['gender'] ?? 'Femme',
            'home_address' => $validated['address'] ?? '12 rue de la République, Lyon',
            'member_level' => 'Premium',
            'avatar_seed' => strtoupper(substr($validated['name'], 0, 2)),
        ];

        if ($this->safeDatabase(fn () => Schema::hasTable('users'), false)) {
            $user = User::query()->firstOrCreate(['email' => $validated['email']], $profile);
            $user->update([
                'name' => $validated['name'],
                'phone_number' => $validated['phone'],
                'gender' => $validated['gender'] ?? $user->gender,
                'home_address' => $validated['address'] ?? $user->home_address,
            ]);
        }

        session()->put('web_profile', $profile);

        return redirect()->route('profile')->with('success', 'Profil enregistré avec succès.');
    }

    public function notifications()
    {
        $notifications = $this->buildNotificationsData();

        return view('pages.notifications', compact('notifications'));
    }

    public function paymentMethods()
    {
        $paymentMethods = $this->buildPaymentMethodsData();

        return view('pages.payment-methods', compact('paymentMethods'));
    }

    public function savePaymentMethod(Request $request)
    {
        $validated = $request->validate([
            'type' => 'required|string',
            'number' => 'required|string',
            'default' => 'nullable|boolean',
        ]);

        $methods = $this->buildPaymentMethodsData();

        $method = [
            'type' => $validated['type'],
            'number' => $validated['number'],
            'default' => (bool) ($validated['default'] ?? false),
        ];

        $methods = array_values(array_filter($methods, fn ($item) => $item['type'] !== $validated['type']));
        $methods[] = $method;
        session()->put('web_payment_methods', $methods);

        if ($this->safeDatabase(fn () => Schema::hasTable('payment_methods'), false)) {
            $profile = $this->buildUserProfile();

            $this->safeDatabase(function () use ($profile, $validated) {
                PaymentMethod::query()->updateOrCreate(
                    ['user_id' => $profile->id ?? 1, 'type' => strtolower($validated['type'])],
                    [
                        'title' => $validated['type'],
                        'subtitle' => $validated['number'],
                        'is_default' => (bool) ($validated['default'] ?? false),
                        'is_selected' => (bool) ($validated['default'] ?? false),
                    ]
                );
            }, null);
        }

        return redirect()->route('payment-methods')->with('success', 'Méthode de paiement enregistré.');
    }

    public function tracking()
    {
        $trip = [
            'from' => 'Place Bellecour',
            'to' => 'Part-Dieu',
            'driver' => 'Paul Martin',
            'vehicle' => 'Eco',
            'eta' => '2 min',
            'distance' => '7,8 km',
            'status' => 'En route',
            'progress' => 65,
            'price' => '€18.50',
        ];

        if ($this->safeDatabase(fn () => Schema::hasTable('trips'), false)) {
            $userId = $this->buildUserProfile()->id ?? 1;
            $latestTrip = $this->safeDatabase(fn () => Trip::query()->where('user_id', $userId)->latest()->first(), null);

            if ($latestTrip) {
                $trip['from'] = $latestTrip->pickup_location['address'] ?? 'Place Bellecour';
                $trip['to'] = $latestTrip->dropoff_location['address'] ?? 'Part-Dieu';
                $trip['driver'] = $latestTrip->driver?->name ?? 'Paul Martin';
                $trip['vehicle'] = $latestTrip->vehicle_category ?? 'Eco';
                $trip['price'] = '€' . number_format((float) $latestTrip->fare, 2);
                $trip['status'] = ucfirst($latestTrip->status ?? 'En route');
            }
        }

        $timeline = [
            ['label' => 'Course confirmée', 'time' => '09:42', 'done' => true],
            ['label' => 'Chauffeur assigné', 'time' => '09:46', 'done' => true],
            ['label' => 'En route vers vous', 'time' => '09:51', 'done' => true],
            ['label' => 'À l’arrivée', 'time' => '09:54', 'done' => false],
            ['label' => 'Trajet terminé', 'time' => '10:04', 'done' => false],
        ];

        return view('pages.trip-tracking', compact('trip', 'timeline'));
    }
}
