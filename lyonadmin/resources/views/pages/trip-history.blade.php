@extends('layouts.app')

@section('title', 'Historique - LyonTaxis')

@section('content')
<div class="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
    <div class="mb-6 flex items-center justify-between">
        <div>
            <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Historique</p>
            <h1 class="mt-2 text-3xl font-black text-slate-900">Mes courses</h1>
        </div>
        <button class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Filtrer</button>
    </div>

    <div class="space-y-4">
        @foreach ($trips as $trip)
            <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <div class="text-sm text-slate-500">{{ $trip['date'] }}</div>
                        <div class="mt-2 text-lg font-bold text-slate-900">{{ $trip['from'] }} → {{ $trip['to'] }}</div>
                    </div>
                    <div class="flex items-center gap-3">
                        <div class="rounded-full px-3 py-1 text-sm font-medium
                            @if ($trip['color'] === 'sky') bg-sky-100 text-sky-700
                            @elseif ($trip['color'] === 'emerald') bg-emerald-100 text-emerald-700
                            @else bg-amber-100 text-amber-700
                            @endif">
                            {{ $trip['status'] }}
                        </div>
                        <div class="text-2xl font-black text-slate-900">{{ $trip['price'] }}</div>
                    </div>
                </div>
            </div>
        @endforeach
    </div>
</div>
@endsection
