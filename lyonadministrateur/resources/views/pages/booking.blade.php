@extends('layouts.app')

@section('title', 'Réserver - LyonTaxis')

@section('content')
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
<style>
    #address-map { height: clamp(280px, 48vh, 480px); min-height: 280px; }
    .leaflet-container { font: inherit; }
    .booking-panel { box-shadow: 0 18px 50px rgba(13, 71, 161, .12); min-width: 0; }
    .leaflet-control { z-index: 500; }
</style>

<div class="mx-auto w-full max-w-7xl px-3 py-4 sm:px-6 sm:py-6 lg:px-8">
    <div class="mb-4 flex min-w-0 items-end justify-between gap-3 sm:mb-5 sm:gap-4">
        <div>
            <p class="text-[11px] font-bold uppercase tracking-[0.16em] text-neutral-500 sm:text-xs sm:tracking-[0.2em]">LyonTaxis</p>
            <h1 class="mt-1 text-2xl font-black tracking-tight text-[#10141d] sm:text-3xl">Où allez-vous ?</h1>
        </div>
        <div class="hidden max-w-[42%] rounded-full bg-white px-3 py-2 text-right text-xs font-semibold text-neutral-600 shadow-sm sm:block">Tarif VTC estimatif avant confirmation</div>
    </div>

    <div class="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
        <div class="booking-panel order-2 rounded-3xl bg-white p-4 sm:p-5 lg:order-1">
            <form method="POST" action="{{ route('booking.save') }}" class="space-y-5" autocomplete="on">
                @csrf
                <div>
                    <div class="mb-3 text-sm font-medium text-slate-700">Quand souhaitez-vous votre taxi ?</div>
                    <div class="grid gap-3 sm:grid-cols-2">
                        <label class="flex min-w-0 cursor-pointer items-center gap-3 rounded-2xl border border-[#ffb300] bg-[#fff8e1] p-3 sm:p-4">
                            <input type="radio" name="reservation_type" value="now" class="h-4 w-4 shrink-0 accent-[#ffb300]" checked />
                            <span><strong class="block text-slate-900">Maintenant</strong><span class="text-sm text-slate-500">Un chauffeur disponible</span></span>
                        </label>
                        <label class="flex min-w-0 cursor-pointer items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-3 sm:p-4">
                            <input type="radio" name="reservation_type" value="later" class="h-4 w-4 shrink-0 accent-[#ffb300]" />
                            <span><strong class="block text-slate-900">Plus tard</strong><span class="text-sm text-slate-500">Choisir une date et une heure</span></span>
                        </label>
                    </div>
                    <div id="schedule-fields" class="mt-3 hidden rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <label for="scheduled_at" class="mb-2 block text-sm font-medium text-slate-700">Date et heure</label>
                        <input id="scheduled_at" name="scheduled_at" type="datetime-local" min="{{ now()->format('Y-m-d\TH:i') }}" class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-slate-800 outline-none focus:border-sky-400" />
                    </div>
                </div>
                <div class="relative rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <label class="mb-2 block text-sm font-medium text-slate-700">Adresse de départ</label>
                    <input id="pickup" name="pickup" type="text" autocomplete="street-address" value="{{ $tripSummary['from'] }}" class="w-full bg-transparent text-base font-medium text-slate-800 outline-none" required />
                    <div id="pickup-suggestions" class="absolute left-4 right-4 top-[4.6rem] z-30 hidden overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl"></div>
                </div>

                <div class="relative rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <label class="mb-2 block text-sm font-medium text-slate-700">Destination</label>
                    <input id="destination" name="destination" type="text" autocomplete="street-address" value="{{ $tripSummary['to'] }}" class="w-full bg-transparent text-base font-medium text-slate-800 outline-none" required />
                    <div id="destination-suggestions" class="absolute left-4 right-4 top-[4.6rem] z-30 hidden overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl"></div>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                    <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <label class="mb-2 block text-sm font-medium text-slate-700">Type de véhicule</label>
                        <select name="vehicle" class="w-full bg-transparent font-medium text-slate-800 outline-none" required>
                            @foreach ($vehicles as $vehicle)
                                <option>{{ $vehicle['name'] }}</option>
                            @endforeach
                        </select>
                    </div>

                    <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <label class="mb-2 block text-sm font-medium text-slate-700">Passagers</label>
                        <select name="passengers" class="w-full bg-transparent font-medium text-slate-800 outline-none">
                            <option value="1">1 passager</option>
                            <option value="2">2 passagers</option>
                            <option value="3">3 passagers</option>
                            <option value="4">4 passagers</option>
                        </select>
                    </div>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                    @foreach ($vehicles as $vehicle)
                        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                            <div class="flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-xl shadow-sm">{{ $vehicle['icon'] }}</div>
                                <div>
                                    <div class="font-semibold text-slate-900">{{ $vehicle['name'] }}</div>
                                    <div class="text-xs text-slate-500">{{ $vehicle['description'] }}</div>
                                </div>
                            </div>
                            <div class="mt-3 text-sm font-semibold text-sky-700">{{ $vehicle['price'] }}</div>
                        </div>
                    @endforeach
                </div>

                <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <label class="mb-2 block text-sm font-medium text-slate-700">Demande spéciale</label>
                    <textarea name="notes" rows="3" class="w-full resize-none bg-transparent text-slate-800 outline-none">Sans fumer, porte-bagages</textarea>
                </div>

                <button type="submit" class="w-full rounded-2xl bg-[#ffc900] px-4 py-3 text-base font-black text-[#171717] shadow-lg shadow-yellow-900/10 transition hover:bg-[#ffd633]">
                    Confirmer la réservation
                </button>
            </form>
        </div>

        <aside class="order-1 space-y-5 lg:order-2">
            <div class="overflow-hidden rounded-3xl border border-neutral-200 bg-white shadow-sm">
                <div class="relative">
                    <div id="address-map"></div>
                    <div class="absolute left-4 top-4 z-[400] rounded-xl bg-white/95 px-3 py-2 text-xs font-bold text-neutral-700 shadow-md">Carte de Lyon</div>
                </div>
            </div>
            <div class="rounded-3xl bg-[#10141d] p-5 text-white shadow-lg sm:p-6">
                <div class="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">Tarif VTC estimatif</div>
                <div class="mt-4 text-4xl font-black text-[#ffb300]">{{ $tripSummary['estimatedPrice'] }}</div>
                <div class="mt-2 text-sm text-neutral-400">{{ $tripSummary['distance'] }} · {{ $tripSummary['duration'] }} · prix final confirmé avant départ</div>
            </div>

            <div class="rounded-3xl bg-white p-6 shadow-sm">
                <div class="mb-3 text-lg font-black text-[#171717]">Chauffeurs disponibles</div>
                <div class="space-y-3">
                    @foreach ($drivers as $driver)
                        <div class="flex items-center justify-between rounded-2xl bg-slate-50 p-3">
                            <div>
                                <div class="font-semibold">{{ $driver['name'] }}</div>
                                <div class="text-sm text-slate-500">{{ $driver['vehicle'] }} • {{ $driver['rating'] }} ★</div>
                            </div>
                            <div class="rounded-full {{ $driver['status'] === 'Disponible' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700' }} px-2 py-1 text-xs font-medium">
                                {{ $driver['eta'] }}
                            </div>
                        </div>
                    @endforeach
                </div>
            </div>
        </aside>
    </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
<script>
    const localAddresses = @json($savedAddresses);
    const map = L.map('address-map').setView([45.764, 4.8357], 13);
    const markers = { pickup: null, destination: null };

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    function updateMap() {
        const points = Object.values(markers).filter(Boolean);
        if (points.length) {
            map.fitBounds(L.latLngBounds(points), { padding: [30, 30] });
        }
    }

    function chooseAddress(field, result) {
        document.getElementById(field).value = result.address;
        document.getElementById(field + '-suggestions').classList.add('hidden');

        if (result.lat && result.lon) {
            if (markers[field]) {
                map.removeLayer(markers[field]);
            }

            markers[field] = L.marker([result.lat, result.lon]).addTo(map).bindPopup(result.address);
            updateMap();
        }
    }

    function renderSuggestions(field, results) {
        const container = document.getElementById(field + '-suggestions');
        container.innerHTML = '';

        results.forEach((result) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'block w-full border-b border-slate-100 px-4 py-3 text-left text-sm text-slate-700 last:border-0 hover:bg-sky-50';
            button.textContent = result.label || result.address;
            button.addEventListener('click', () => chooseAddress(field, result));
            container.appendChild(button);
        });

        container.classList.toggle('hidden', results.length === 0);
    }

    function setupAutocomplete(field) {
        const input = document.getElementById(field);
        let timer;

        input.addEventListener('input', () => {
            clearTimeout(timer);
            const query = input.value.trim();

            if (query.length < 3) {
                renderSuggestions(field, []);
                return;
            }

            timer = setTimeout(async () => {
                const localResults = localAddresses
                    .filter((item) => `${item.title} ${item.address}`.toLowerCase().includes(query.toLowerCase()))
                    .map((item) => ({ label: `${item.title} - ${item.address}`, address: item.address }));

                try {
                    const response = await fetch(`https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&countrycodes=fr&viewbox=4.5,45.9,5.1,45.5&bounded=1&q=${encodeURIComponent(query)}`);
                    const remoteResults = await response.json();
                    const results = remoteResults.map((item) => ({
                        label: item.display_name,
                        address: item.display_name,
                        lat: item.lat,
                        lon: item.lon
                    }));
                    renderSuggestions(field, [...localResults, ...results]);
                } catch {
                    renderSuggestions(field, localResults);
                }
            }, 450);
        });

        input.addEventListener('blur', () => {
            setTimeout(() => document.getElementById(field + '-suggestions').classList.add('hidden'), 180);
        });
    }

    setupAutocomplete('pickup');
    setupAutocomplete('destination');
    const scheduleFields = document.getElementById('schedule-fields');
    const scheduledAt = document.getElementById('scheduled_at');
    document.querySelectorAll('input[name="reservation_type"]').forEach((input) => {
        input.addEventListener('change', () => {
            const isLater = input.value === 'later' && input.checked;
            scheduleFields.classList.toggle('hidden', !isLater);
            scheduledAt.required = isLater;
        });
    });
    setTimeout(() => map.invalidateSize(), 100);
</script>
@endsection
