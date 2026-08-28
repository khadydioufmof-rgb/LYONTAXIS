@extends('layouts.app')

@section('title', 'Accueil - LyonTaxis')

@section('content')
<div class="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
    <section class="rounded-[2rem] bg-slate-900 px-6 py-8 text-white shadow-2xl shadow-sky-900/20 sm:px-8 lg:px-12">
        <div class="grid gap-8 lg:grid-cols-[1.3fr_0.7fr] lg:items-center">
            <div>
                <div class="mb-4 inline-flex rounded-full bg-white/10 px-3 py-1 text-sm font-medium text-sky-100">
                    Service taxi à Lyon
                </div>
                <h1 class="max-w-xl text-4xl font-black tracking-tight sm:text-5xl">
                    Déplacez-vous vite, sans stress.
                </h1>
                <p class="mt-4 max-w-xl text-lg text-slate-300">
                    Réservez une course depuis votre navigateur, suivez votre chauffeur et gérez vos paiements en un seul endroit.
                </p>

                <div class="mt-8 flex flex-wrap gap-3">
                    <a href="/booking" class="rounded-full bg-sky-500 px-6 py-3 font-semibold text-white hover:bg-sky-400">Réserver maintenant</a>
                    <a href="/login" class="rounded-full border border-white/20 bg-white/5 px-6 py-3 font-semibold text-white hover:bg-white/10">Connexion</a>
                </div>
            </div>

            <div class="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur-sm">
                <div class="space-y-4">
                    <div class="rounded-2xl bg-white/5 p-4">
                        <div class="text-xs uppercase tracking-[0.18em] text-slate-300">Trajet actuel</div>
                        <div class="mt-3 flex items-center justify-between">
                            <div>
                                <div class="text-sm text-slate-400">Départ</div>
                                <div class="font-semibold">{{ $featuredDriver['vehicle'] ?? 'Eco' }}</div>
                            </div>
                            <div class="text-sky-300">→</div>
                            <div>
                                <div class="text-sm text-slate-400">Arrivée</div>
                                <div class="font-semibold">Part-Dieu</div>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl bg-white/5 p-4">
                        <div class="flex items-center justify-between">
                            <div>
                                <div class="text-xs uppercase tracking-[0.18em] text-slate-300">Chauffeur</div>
                                <div class="mt-1 font-semibold">{{ $featuredDriver['name'] }}</div>
                            </div>
                            <div class="rounded-full bg-emerald-500/20 px-2 py-1 text-xs font-medium text-emerald-300">
                                {{ $featuredDriver['status'] }}
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl bg-sky-500 p-4 text-white">
                        <div class="text-sm text-sky-100">Prix estimé</div>
                        <div class="mt-1 text-3xl font-black">{{ $featuredDriver['price'] }}</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="mt-8 grid gap-4 md:grid-cols-3">
        @foreach ($stats as $stat)
            <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                <div class="text-3xl font-black text-slate-900">{{ $stat['value'] }}</div>
                <div class="mt-1 text-sm text-slate-500">{{ $stat['label'] }}</div>
            </div>
        @endforeach
    </section>

    <section class="mt-8 grid gap-5 md:grid-cols-3">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-sky-100 text-xl">📍</div>
            <h3 class="text-lg font-bold text-slate-900">Recherche rapide</h3>
            <p class="mt-2 text-sm text-slate-600">Saisissez votre point de départ et votre destination en quelques secondes.</p>
        </div>
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-100 text-xl">🚕</div>
            <h3 class="text-lg font-bold text-slate-900">Chauffeurs fiables</h3>
            <p class="mt-2 text-sm text-slate-600">Choisissez le véhicule adapté à votre trajet et votre budget.</p>
        </div>
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-100 text-xl">💳</div>
            <h3 class="text-lg font-bold text-slate-900">Paiement simple</h3>
            <p class="mt-2 text-sm text-slate-600">Payez en cash, par carte ou via votre portefeuille numérique.</p>
        </div>
    </section>

    <section class="mt-8 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="mb-4 flex items-center justify-between">
            <h3 class="text-xl font-bold text-slate-900">Trajets récents</h3>
            <a href="/trips" class="text-sm font-semibold text-sky-600">Voir tout</a>
        </div>

        <div class="space-y-3">
            @foreach ($recentTrips as $trip)
                <div class="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
                    <div>
                        <div class="text-sm text-slate-500">{{ $trip['time'] }}</div>
                        <div class="mt-1 font-semibold text-slate-900">{{ $trip['from'] }} → {{ $trip['to'] }}</div>
                    </div>
                    <div class="text-lg font-black text-slate-900">{{ $trip['price'] }}</div>
                </div>
            @endforeach
        </div>
    </section>
</div>
@endsection
