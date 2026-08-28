@extends('layouts.app')

@section('title', 'Connexion - LyonTaxis')

@section('content')
<div class="mx-auto max-w-6xl px-4 py-16 sm:px-6 lg:px-8">
    <div class="grid items-center gap-10 lg:grid-cols-2">
        <div class="space-y-6">
            <div class="inline-flex items-center rounded-full bg-sky-100 px-3 py-1 text-sm font-medium text-sky-700">
                Accès rapide
            </div>
            <h1 class="text-4xl font-black tracking-tight text-slate-900 sm:text-5xl">
                Bienvenue chez <span class="text-sky-600">LyonTaxis</span>
            </h1>
            <p class="max-w-xl text-lg text-slate-600">
                Connectez-vous pour réserver une course, suivre votre chauffeur et gérer vos paiements en quelques secondes.
            </p>

            <div class="grid gap-4 sm:grid-cols-2">
                <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                    <div class="text-2xl font-bold text-sky-600">4.9/5</div>
                    <div class="mt-1 text-sm text-slate-500">Avis clients</div>
                </div>
                <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                    <div class="text-2xl font-bold text-sky-600">2 min</div>
                    <div class="mt-1 text-sm text-slate-500">Temps moyen d’arrivée</div>
                </div>
            </div>
        </div>

        <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60">
            <div class="mb-6">
                <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Connexion</p>
                <h2 class="mt-2 text-2xl font-bold text-slate-900">Saisissez votre numéro</h2>
            </div>

            <form class="space-y-5">
                <div>
                    <label class="mb-2 block text-sm font-medium text-slate-700">Téléphone ou e-mail</label>
                    <input type="text" value="+33 6 12 34 56 78" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-800 outline-none ring-0 transition focus:border-sky-400 focus:bg-white" />
                </div>

                <div>
                    <label class="mb-2 block text-sm font-medium text-slate-700">Code OTP</label>
                    <div class="grid grid-cols-4 gap-2">
                        <input class="h-14 rounded-xl border border-slate-200 bg-slate-50 text-center text-xl font-bold text-slate-800" value="1" />
                        <input class="h-14 rounded-xl border border-slate-200 bg-slate-50 text-center text-xl font-bold text-slate-800" value="2" />
                        <input class="h-14 rounded-xl border border-slate-200 bg-slate-50 text-center text-xl font-bold text-slate-800" value="3" />
                        <input class="h-14 rounded-xl border border-slate-200 bg-slate-50 text-center text-xl font-bold text-slate-800" value="4" />
                    </div>
                </div>

                <button type="button" class="w-full rounded-2xl bg-sky-600 px-4 py-3 text-base font-semibold text-white shadow-lg shadow-sky-600/20 hover:bg-sky-700">
                    Se connecter
                </button>

                <div class="flex items-center justify-between text-sm text-slate-500">
                    <span>Pas encore inscrit ?</span>
                    <a href="/booking" class="font-medium text-sky-600 hover:text-sky-700">Essayer sans compte</a>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
