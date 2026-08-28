@extends('layouts.app')

@section('title', 'ERP Administrateur - LyonTaxis')

@section('content')
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
<style>
    #erp-map { height: 440px; }
    .leaflet-container { font: inherit; }
</style>

<div class="mx-auto w-full max-w-7xl px-3 py-6 sm:px-6 lg:px-8">
    <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
            <p class="text-xs font-black uppercase tracking-[0.2em] text-[#0d47a1]">Centre de contrôle</p>
            <h1 class="mt-1 text-3xl font-black tracking-tight text-[#10141d]">ERP LyonTaxis</h1>
            <p class="mt-2 text-sm text-slate-500">Pilotage unifié de Lyonadmin, LyonTaxis Pro et LyonTaxis Client.</p>
        </div>
        <div class="rounded-xl bg-[#10141d] px-4 py-3 text-sm font-bold text-white">Système opérationnel <span class="ml-2 inline-block h-2 w-2 rounded-full bg-emerald-400"></span></div>
    </div>

    <section class="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        @foreach ($metrics as $metric)
            <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <div class="text-3xl font-black {{ match ($metric['tone']) { 'yellow' => 'text-[#d28c00]', 'cyan' => 'text-[#168dcc]', 'green' => 'text-emerald-600', default => 'text-[#0d47a1]' } }}">{{ $metric['value'] }}</div>
                <div class="mt-2 text-sm font-semibold text-slate-500">{{ $metric['label'] }}</div>
            </div>
        @endforeach
    </section>

    <section class="mb-6 grid gap-4 md:grid-cols-3">
        @foreach ([['Lyonadmin', 'Console ERP & sécurité', 'bg-[#0d47a1]'], ['LyonTaxis Pro', 'Flotte & courses chauffeur', 'bg-[#10141d]'], ['LyonTaxis Client', 'Réservations & parcours client', 'bg-[#1e88e5]']] as $app)
            <div class="flex items-center gap-4 rounded-2xl p-5 text-white shadow-sm {{ $app[2] }}">
                <img src="{{ asset('images/lyontaxis-logo.jpg') }}" alt="" class="h-12 w-12 rounded-xl object-cover ring-1 ring-white/30">
                <div><div class="font-black">{{ $app[0] }}</div><div class="mt-1 text-sm text-white/70">{{ $app[1] }}</div></div>
            </div>
        @endforeach
    </section>

    <section class="mb-6 grid gap-5 lg:grid-cols-[1.35fr_0.65fr]">
        <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
            <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 p-5">
                <div><h2 class="text-xl font-black text-[#10141d]">Carte opérationnelle</h2><p class="mt-1 text-sm text-slate-500">Chauffeurs, positions et trajets en cours</p></div>
                <div class="flex gap-3 text-xs font-semibold text-slate-500"><span><i class="mr-1 inline-block h-2 w-2 rounded-full bg-emerald-500"></i>Disponible</span><span><i class="mr-1 inline-block h-2 w-2 rounded-full bg-slate-500"></i>Occupé</span></div>
            </div>
            <div id="erp-map"></div>
        </div>

        <div class="rounded-3xl bg-[#10141d] p-5 text-white shadow-sm">
            <div class="text-xs font-bold uppercase tracking-[0.18em] text-[#ffb300]">Sécurité</div>
            <h2 class="mt-2 text-xl font-black">Journal de contrôle</h2>
            <div class="mt-5 space-y-4">
                @foreach ($securityEvents as $event)
                    <div class="border-l-2 {{ $event['level'] === 'ok' ? 'border-emerald-400' : 'border-[#ffb300]' }} pl-3">
                        <div class="flex justify-between gap-3 text-sm font-bold"><span>{{ $event['label'] }}</span><span class="text-xs font-normal text-white/50">{{ $event['time'] }}</span></div>
                        <div class="mt-1 text-xs text-white/60">{{ $event['detail'] }}</div>
                    </div>
                @endforeach
            </div>
            <div class="mt-6 rounded-xl bg-white/10 p-3 text-xs text-white/70">Chaque action sensible doit être journalisée avant la mise en production.</div>
        </div>
    </section>

    <section class="grid gap-5 lg:grid-cols-2">
        <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-black text-[#10141d]">Transactions récentes</h2><span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-700">Contrôlées</span></div>
            <div class="space-y-3">
                @foreach ($transactions as $transaction)
                    <div class="flex items-center justify-between gap-3 rounded-xl bg-slate-50 p-3">
                        <div class="min-w-0"><div class="text-xs font-bold text-slate-400">{{ $transaction['id'] }}</div><div class="truncate text-sm font-bold text-slate-800">{{ $transaction['label'] }}</div></div>
                        <div class="shrink-0 text-right"><div class="font-black text-[#0d47a1]">{{ $transaction['amount'] }}</div><div class="text-xs text-emerald-600">{{ $transaction['status'] }}</div></div>
                    </div>
                @endforeach
            </div>
        </div>

        <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
            <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-black text-[#10141d]">Modules connectés</h2><span class="rounded-full bg-blue-100 px-3 py-1 text-xs font-bold text-blue-700">3 applications</span></div>
            <div class="space-y-3">
                <div class="flex items-center justify-between rounded-xl border border-slate-100 p-3"><span class="font-bold text-slate-700">Réservations client</span><span class="text-xs font-bold text-emerald-600">Synchronisé</span></div>
                <div class="flex items-center justify-between rounded-xl border border-slate-100 p-3"><span class="font-bold text-slate-700">Attribution chauffeur</span><span class="text-xs font-bold text-emerald-600">Actif</span></div>
                <div class="flex items-center justify-between rounded-xl border border-slate-100 p-3"><span class="font-bold text-slate-700">Paiements & remboursements</span><span class="text-xs font-bold text-[#d28c00]">Surveillance</span></div>
                <div class="flex items-center justify-between rounded-xl border border-slate-100 p-3"><span class="font-bold text-slate-700">Notifications</span><span class="text-xs font-bold text-emerald-600">Actif</span></div>
            </div>
        </div>
    </section>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
<script>
    const drivers = @json($mapDrivers);
    const trips = @json($mapTrips);
    const map = L.map('erp-map').setView([45.764, 4.8357], 13);
    const bounds = [];
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap contributors' }).addTo(map);

    drivers.forEach((driver) => {
        const color = driver.available ? '#10b981' : '#64748b';
        L.circleMarker([driver.lat, driver.lng], { radius: 9, color: '#fff', weight: 3, fillColor: color, fillOpacity: 1 }).addTo(map).bindPopup(`<strong>${driver.name}</strong><br>${driver.available ? 'Disponible' : 'Occupé'}`);
        bounds.push([driver.lat, driver.lng]);
    });

    trips.forEach((trip) => {
        const points = [[trip.fromLat, trip.fromLng], [trip.toLat, trip.toLng]];
        L.polyline(points, { color: trip.status === 'cancelled' ? '#94a3b8' : '#1e88e5', weight: 4, opacity: .75, dashArray: trip.status === 'cancelled' ? '6 8' : null }).addTo(map).bindPopup(`${trip.from} → ${trip.to}`);
        points.forEach((point) => bounds.push(point));
    });

    if (bounds.length) map.fitBounds(bounds, { padding: [28, 28] });
    setTimeout(() => map.invalidateSize(), 100);
</script>
@endsection
