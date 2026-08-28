@extends('layouts.app')

@section('title', 'Profil - LyonTaxis')

@section('content')
<div class="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
    @if (session('success'))
        <div class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {{ session('success') }}
        </div>
    @endif

    <div class="mb-6">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Profil</p>
        <h1 class="mt-2 text-3xl font-black text-slate-900">Mon compte</h1>
    </div>

    <div class="grid gap-8 lg:grid-cols-[0.8fr_1.2fr]">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div class="flex flex-col items-center text-center">
                <div class="flex h-20 w-20 items-center justify-center rounded-full bg-sky-600 text-2xl font-black text-white">{{ $profileData['avatar'] }}</div>
                <div class="mt-4 text-2xl font-bold text-slate-900">{{ $profileData['name'] }}</div>
                <div class="text-sm text-slate-500">Membre {{ $profileData['member'] }}</div>
            </div>

            <div class="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
                @foreach ($infoCards as $card)
                    <div class="rounded-2xl bg-slate-50 p-3">
                        <div class="text-xs uppercase tracking-[0.18em] text-slate-500">{{ $card['label'] }}</div>
                        <div class="mt-1 text-lg font-bold text-slate-900">{{ $card['value'] }}</div>
                    </div>
                @endforeach
            </div>
        </div>

        <div class="space-y-6">
            <form method="POST" action="{{ route('profile.save') }}" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                @csrf
                <div class="grid gap-5 sm:grid-cols-2">
                    <div>
                        <label class="mb-2 block text-sm font-medium text-slate-700">Nom</label>
                        <input name="name" value="{{ $profileData['name'] }}" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400" />
                    </div>
                    <div>
                        <label class="mb-2 block text-sm font-medium text-slate-700">Téléphone</label>
                        <input name="phone" value="{{ $profileData['phone'] }}" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400" />
                    </div>
                    <div>
                        <label class="mb-2 block text-sm font-medium text-slate-700">Email</label>
                        <input name="email" value="{{ $profileData['email'] }}" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400" />
                    </div>
                    <div>
                        <label class="mb-2 block text-sm font-medium text-slate-700">Genre</label>
                        <input name="gender" value="{{ $profileData['gender'] }}" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400" />
                    </div>
                    <div class="sm:col-span-2">
                        <label class="mb-2 block text-sm font-medium text-slate-700">Adresse domicile</label>
                        <input name="address" value="{{ $profileData['address'] }}" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400" />
                    </div>
                </div>

                <button type="submit" class="mt-6 rounded-2xl bg-sky-600 px-5 py-3 font-semibold text-white hover:bg-sky-700">
                    Enregistrer les modifications
                </button>
            </form>

            <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div class="mb-4 text-xl font-bold text-slate-900">Adresses enregistrées</div>
                <div class="space-y-3">
                    @foreach ($savedAddresses as $address)
                        <div class="flex items-center justify-between rounded-2xl bg-slate-50 p-3">
                            <div>
                                <div class="font-semibold text-slate-900">{{ $address['title'] }}</div>
                                <div class="text-sm text-slate-600">{{ $address['address'] }}</div>
                            </div>
                            <button class="text-sm font-medium text-sky-600">Modifier</button>
                        </div>
                    @endforeach
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
