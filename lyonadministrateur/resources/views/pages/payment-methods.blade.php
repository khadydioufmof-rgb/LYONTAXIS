@extends('layouts.app')

@section('title', 'Paiements - LyonTaxis')

@section('content')
<div class="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
    @if (session('success'))
        <div class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {{ session('success') }}
        </div>
    @endif

    <div class="mb-6 flex items-center justify-between">
        <div>
            <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Paiement</p>
            <h1 class="mt-2 text-3xl font-black text-slate-900">Méthodes de paiement</h1>
        </div>
        <form method="POST" action="{{ route('payment-methods.save') }}" class="flex gap-3">
            @csrf
            <input type="hidden" name="type" value="Visa" />
            <input type="hidden" name="number" value="•••• 9999" />
            <input type="hidden" name="default" value="0" />
            <button type="submit" class="rounded-full bg-sky-600 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-700">Ajouter</button>
        </form>
    </div>

    <div class="space-y-4">
        @foreach([
            ['type' => 'Visa', 'number' => '•••• 2234', 'default' => true],
            ['type' => 'Mastercard', 'number' => '•••• 1188', 'default' => false],
            ['type' => 'Espèces', 'number' => 'Paiement à bord', 'default' => false],
        ] as $method)
            <div class="flex items-center justify-between rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                <div class="flex items-center gap-4">
                    <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-sky-100 text-xl">💳</div>
                    <div>
                        <div class="text-lg font-bold text-slate-900">{{ $method['type'] }}</div>
                        <div class="text-sm text-slate-500">{{ $method['number'] }}</div>
                    </div>
                </div>
                <div class="flex items-center gap-3">
                    @if($method['default'])
                        <span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-medium text-emerald-700">Par défaut</span>
                    @endif
                    <button class="rounded-full border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Modifier</button>
                </div>
            </div>
        @endforeach
    </div>
</div>
@endsection
