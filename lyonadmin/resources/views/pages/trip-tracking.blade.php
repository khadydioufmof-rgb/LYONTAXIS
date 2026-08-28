@extends('layouts.app')

@section('title', 'Suivi de course - LyonTaxis')

@section('content')
<div class="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
    <div class="mb-6">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-sky-600">Suivi de course</p>
        <h1 class="mt-2 text-3xl font-black text-slate-900">Course en temps réel</h1>
    </div>

    <div class="grid gap-8 lg:grid-cols-[1.2fr_0.8fr]">
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div class="flex items-center justify-between">
                <div>
                    <div class="text-sm text-slate-500">Trajet en cours</div>
                    <div class="mt-1 text-2xl font-black text-slate-900">{{ $trip['from'] }} → {{ $trip['to'] }}</div>
                </div>
                <div class="rounded-full bg-emerald-100 px-3 py-1 text-sm font-semibold text-emerald-700">{{ $trip['status'] }}</div>
            </div>

            <div class="mt-6 rounded-2xl bg-slate-50 p-4">
                <div class="mb-2 flex items-center justify-between text-sm text-slate-600">
                    <span>Progression</span>
                    <span class="font-semibold text-slate-900"><span id="progressValue">{{ $trip['progress'] }}</span>%</span>
                </div>
                <div class="h-3 overflow-hidden rounded-full bg-slate-200">
                    <div id="progressBar" class="h-full rounded-full bg-gradient-to-r from-sky-500 to-emerald-500" style="width: {{ $trip['progress'] }}%"></div>
                </div>
            </div>

            <div class="mt-8 space-y-5">
                @foreach ($timeline as $step)
                    <div class="flex items-start gap-4">
                        <div class="relative flex flex-col items-center">
                            <div class="flex h-8 w-8 items-center justify-center rounded-full {{ $step['done'] ? 'bg-sky-600 text-white' : 'bg-slate-200 text-slate-500' }} font-bold shadow-sm">
                                {{ $loop->iteration }}
                            </div>
                            @if (! $loop->last)
                                <div class="mt-2 h-10 w-px bg-slate-200"></div>
                            @endif
                        </div>

                        <div class="flex-1 rounded-2xl {{ $step['done'] ? 'bg-sky-50 border border-sky-100' : 'bg-slate-50 border border-slate-200' }} p-3">
                            <div class="flex items-center justify-between gap-3">
                                <div class="font-semibold text-slate-900">{{ $step['label'] }}</div>
                                <div class="text-xs text-slate-500">{{ $step['time'] }}</div>
                            </div>
                        </div>
                    </div>
                @endforeach
            </div>
        </div>

        <aside class="space-y-5">
            <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Chauffeur</div>
                <div class="mt-4 flex items-center gap-4">
                    <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-sky-100 text-2xl">🚕</div>
                    <div>
                        <div class="text-xl font-bold text-slate-900">{{ $trip['driver'] }}</div>
                        <div class="text-sm text-slate-500">{{ $trip['vehicle'] }} • {{ $trip['eta'] }}</div>
                    </div>
                </div>
                <div class="mt-5 rounded-2xl bg-slate-50 p-4">
                    <div class="text-sm text-slate-500">Distance</div>
                    <div class="mt-1 text-2xl font-black text-slate-900">{{ $trip['distance'] }}</div>
                </div>
            </div>

            <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Montant</div>
                <div class="mt-4 text-4xl font-black text-slate-900">{{ $trip['price'] }}</div>
                <button class="mt-5 w-full rounded-2xl bg-sky-600 px-4 py-3 font-semibold text-white hover:bg-sky-700">
                    Contacter le chauffeur
                </button>
            </div>
        </aside>
    </div>
</div>

<script>
    const progressBar = document.getElementById('progressBar');
    const progressValue = document.getElementById('progressValue');
    let progress = {{ $trip['progress'] }};

    setInterval(() => {
        if (progress >= 100) {
            progress = 100;
        } else {
            progress += 5;
        }

        progressBar.style.width = progress + '%';
        progressValue.textContent = progress;
    }, 3000);
</script>
@endsection