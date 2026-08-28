@extends('layouts.app')

@section('title', 'Chauffeurs - ERP LyonTaxis')

@section('content')
<div class="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
    @if (session('success'))
        <div class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-bold text-emerald-700">{{ session('success') }}</div>
    @endif

    <div class="mb-8 flex flex-col justify-between gap-5 md:flex-row md:items-end">
        <div>
            <p class="text-xs font-black uppercase tracking-[0.22em] text-sky-600">Gestion de flotte</p>
            <h1 class="mt-2 text-4xl font-black tracking-tight text-slate-950">Chauffeurs</h1>
            <p class="mt-2 max-w-xl text-slate-500">Suivez la disponibilité, la qualité de service et les véhicules de votre réseau.</p>
        </div>
        <button type="button" onclick="toggleDriverForm(true)" class="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-black text-white shadow-lg shadow-slate-900/20 transition hover:-translate-y-0.5 hover:bg-sky-700">+ Ajouter un chauffeur</button>
    </div>

    <div class="mb-8 grid gap-4 sm:grid-cols-3">
        <div class="rounded-3xl bg-slate-950 p-5 text-white shadow-xl shadow-slate-900/10"><div class="text-3xl font-black">{{ $stats['total'] }}</div><div class="mt-1 text-sm text-slate-400">Chauffeurs inscrits</div></div>
        <div class="rounded-3xl bg-emerald-500 p-5 text-white shadow-xl shadow-emerald-500/20"><div class="text-3xl font-black">{{ $stats['available'] }}</div><div class="mt-1 text-sm text-emerald-50">Disponibles maintenant</div></div>
        <div class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"><div class="text-3xl font-black text-slate-950">{{ number_format($stats['rating'], 1) }} <span class="text-amber-400">*</span></div><div class="mt-1 text-sm text-slate-500">Note moyenne réseau</div></div>
    </div>

    <div class="mb-6 flex flex-col gap-3 rounded-3xl border border-slate-200 bg-white p-4 shadow-sm sm:flex-row">
        <label class="flex min-w-0 flex-1 items-center gap-3 rounded-2xl bg-slate-50 px-4 py-3">
            <span class="text-slate-400">⌕</span>
            <input id="driverSearch" type="search" placeholder="Rechercher un nom, véhicule ou plaque" class="w-full bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400">
        </label>
        <select id="statusFilter" class="rounded-2xl border-0 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700 outline-none">
            <option value="all">Tous les statuts</option>
            <option value="available">Disponibles</option>
            <option value="offline">Indisponibles</option>
        </select>
        <select id="categoryFilter" class="rounded-2xl border-0 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700 outline-none">
            <option value="all">Toutes les catégories</option>
            @foreach ($categories as $category)<option value="{{ strtolower($category) }}">{{ $category }}</option>@endforeach
        </select>
    </div>

    <div id="driverGrid" class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        @forelse ($drivers as $driver)
            <article class="driver-card group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:border-sky-200 hover:shadow-xl hover:shadow-sky-900/10" data-name="{{ strtolower($driver['name'].' '.$driver['vehicle'].' '.$driver['plate']) }}" data-status="{{ $driver['available'] ? 'available' : 'offline' }}" data-category="{{ strtolower($driver['category']) }}">
                <div class="flex items-start justify-between gap-3">
                    <div class="flex items-center gap-3"><div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-sky-100 text-sm font-black text-sky-700">{{ $driver['avatar'] }}</div><div><h2 class="font-black text-slate-950">{{ $driver['name'] }}</h2><p class="text-xs font-semibold text-slate-400">{{ $driver['phone'] ?: 'Téléphone non renseigné' }}</p></div></div>
                    <span class="rounded-full px-3 py-1 text-[11px] font-black {{ $driver['available'] ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500' }}">{{ $driver['available'] ? 'Disponible' : 'Hors ligne' }}</span>
                </div>
                <div class="mt-5 rounded-2xl bg-slate-50 p-4"><div class="flex items-center justify-between"><span class="text-xs font-black uppercase tracking-[0.15em] text-slate-400">Véhicule</span><span class="rounded-full bg-white px-2 py-1 text-xs font-black text-sky-700">{{ $driver['category'] }}</span></div><div class="mt-2 text-lg font-black text-slate-900">{{ $driver['vehicle'] }}</div><div class="mt-1 text-xs font-bold uppercase tracking-wider text-slate-400">{{ $driver['plate'] }} · {{ $driver['color'] }}</div></div>
                <div class="mt-5 grid grid-cols-2 gap-3"><div><div class="text-xl font-black text-slate-950">{{ number_format($driver['rating'], 1) }} <span class="text-amber-400">*</span></div><div class="text-xs font-semibold text-slate-400">Note client</div></div><div><div class="text-xl font-black text-slate-950">{{ number_format($driver['trips']) }}</div><div class="text-xs font-semibold text-slate-400">Courses réalisées</div></div></div>
            </article>
        @empty
            <div class="col-span-full rounded-3xl border border-dashed border-slate-300 bg-white p-12 text-center"><div class="text-lg font-black text-slate-900">Aucun chauffeur trouvé</div><p class="mt-2 text-sm text-slate-500">Ajoutez votre premier chauffeur pour commencer à suivre la flotte.</p></div>
        @endforelse
    </div>
    <p id="emptyFilter" class="mt-8 hidden text-center text-sm font-bold text-slate-500">Aucun résultat pour ces filtres.</p>
</div>

<div id="driverForm" class="fixed inset-0 z-50 hidden items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" aria-labelledby="driverFormTitle">
    <form method="POST" action="{{ route('admin.drivers.save') }}" class="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-3xl bg-white p-6 shadow-2xl sm:p-8">
        @csrf
        <div class="flex items-start justify-between gap-4"><div><p class="text-xs font-black uppercase tracking-[0.2em] text-sky-600">Nouvelle fiche</p><h2 id="driverFormTitle" class="mt-2 text-2xl font-black text-slate-950">Ajouter un chauffeur</h2></div><button type="button" onclick="toggleDriverForm(false)" class="text-2xl leading-none text-slate-400 hover:text-slate-900" aria-label="Fermer">&times;</button></div>
        <div class="mt-6 grid gap-4 sm:grid-cols-2">
            <label class="sm:col-span-2"><span class="mb-2 block text-sm font-bold text-slate-700">Nom complet</span><input name="name" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"></label>
            <label><span class="mb-2 block text-sm font-bold text-slate-700">Téléphone</span><input name="phone_number" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"></label>
            <label><span class="mb-2 block text-sm font-bold text-slate-700">Catégorie</span><select name="vehicle_category" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"><option>Eco</option><option>Sedan</option><option>Premium</option><option>Van</option></select></label>
            <label><span class="mb-2 block text-sm font-bold text-slate-700">Véhicule</span><input name="car_model" required placeholder="Toyota Prius" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"></label>
            <label><span class="mb-2 block text-sm font-bold text-slate-700">Immatriculation</span><input name="license_plate" required placeholder="AB-123-CD" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"></label>
            <label class="sm:col-span-2"><span class="mb-2 block text-sm font-bold text-slate-700">Couleur</span><input name="car_color" required placeholder="Noir" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-sky-400"></label>
        </div>
        <div class="mt-7 flex justify-end gap-3"><button type="button" onclick="toggleDriverForm(false)" class="rounded-2xl px-4 py-3 text-sm font-bold text-slate-500 hover:bg-slate-100">Annuler</button><button type="submit" class="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-black text-white hover:bg-sky-700">Créer la fiche</button></div>
    </form>
</div>

<script>
    const driverCards = [...document.querySelectorAll('.driver-card')];
    const updateDriverGrid = () => {
        const query = document.getElementById('driverSearch').value.toLowerCase().trim();
        const status = document.getElementById('statusFilter').value;
        const category = document.getElementById('categoryFilter').value;
        let visible = 0;
        driverCards.forEach((card) => {
            const matches = card.dataset.name.includes(query) && (status === 'all' || card.dataset.status === status) && (category === 'all' || card.dataset.category === category);
            card.classList.toggle('hidden', !matches);
            if (matches) visible++;
        });
        document.getElementById('emptyFilter').classList.toggle('hidden', visible !== 0);
    };
    ['driverSearch', 'statusFilter', 'categoryFilter'].forEach((id) => document.getElementById(id).addEventListener('input', updateDriverGrid));
    function toggleDriverForm(open) { document.getElementById('driverForm').classList.toggle('hidden', !open); document.getElementById('driverForm').classList.toggle('flex', open); }
</script>
@endsection
