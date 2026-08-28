@extends('layouts.app')

@section('title', 'Notifications - LyonTaxis')

@section('content')
<div class="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
    <div class="mb-6">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Notifications</p>
        <h1 class="mt-2 text-3xl font-black text-slate-900">Centre de notifications</h1>
    </div>

    <div class="space-y-4">
        @foreach([
            ['title' => 'Votre chauffeur est en route', 'text' => 'Paul Martin arrive dans 2 minutes sur votre adresse.', 'time' => 'Il y a 3 min', 'read' => false],
            ['title' => 'Paiement validé', 'text' => 'Le paiement pour votre course a bien été enregistré.', 'time' => 'Hier', 'read' => true],
            ['title' => 'Nouvelle promotion', 'text' => 'Profitez de 10% de réduction pour votre prochain trajet.', 'time' => '2 jours', 'read' => true],
        ] as $notification)
            <div class="flex items-start gap-4 rounded-3xl border border-slate-200 bg-white p-5 shadow-sm {{ $notification['read'] ? 'opacity-80' : '' }}">
                <div class="mt-1 flex h-10 w-10 items-center justify-center rounded-2xl {{ $notification['read'] ? 'bg-slate-100 text-slate-600' : 'bg-sky-100 text-sky-700' }}">
                    {{ $notification['read'] ? '✓' : '•' }}
                </div>
                <div class="flex-1">
                    <div class="flex items-center justify-between gap-3">
                        <h3 class="text-lg font-bold text-slate-900">{{ $notification['title'] }}</h3>
                        <span class="text-xs text-slate-500">{{ $notification['time'] }}</span>
                    </div>
                    <p class="mt-2 text-sm text-slate-600">{{ $notification['text'] }}</p>
                </div>
            </div>
        @endforeach
    </div>
</div>
@endsection
