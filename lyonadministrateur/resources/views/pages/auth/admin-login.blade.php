<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion ERP LyonTaxis</title>
    <link rel="icon" type="image/jpeg" href="{{ asset('images/lyontaxis-logo.jpg') }}">
    <link rel="apple-touch-icon" href="{{ asset('images/lyontaxis-logo.jpg') }}">
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-slate-950 text-slate-900">
    <main class="flex min-h-screen items-center justify-center px-4 py-10">
        <div class="grid w-full max-w-5xl overflow-hidden rounded-[2rem] bg-white shadow-2xl shadow-sky-950/30 lg:grid-cols-[0.9fr_1.1fr]">
            <section class="flex flex-col justify-between bg-gradient-to-br from-sky-700 via-blue-800 to-slate-950 p-8 text-white sm:p-12">
                <div><img src="{{ asset('images/lyontaxis-logo.jpg') }}" alt="Logo LyonTaxis" class="mb-10 h-16 w-16 rounded-2xl object-cover shadow-xl ring-2 ring-white/30"><p class="text-xs font-black uppercase tracking-[0.22em] text-sky-200">Console centrale</p><h1 class="mt-4 text-4xl font-black tracking-tight sm:text-5xl">Pilotez LyonTaxis.</h1><p class="mt-5 max-w-sm text-sm leading-6 text-sky-100">Retrouvez la flotte, les courses et les comptes clients dans un espace réservé à l’administration.</p></div>
                <div class="mt-12 grid grid-cols-2 gap-3 text-sm"><div class="rounded-2xl bg-white/10 p-4"><div class="font-black">24/7</div><div class="mt-1 text-sky-200">Supervision</div></div><div class="rounded-2xl bg-white/10 p-4"><div class="font-black">3 plateformes</div><div class="mt-1 text-sky-200">Connectées</div></div></div>
            </section>
            <section class="p-8 sm:p-12"><div class="mb-10"><p class="text-xs font-black uppercase tracking-[0.22em] text-sky-600">Accès sécurisé</p><h2 class="mt-3 text-3xl font-black tracking-tight text-slate-950">Connexion administrateur</h2><p class="mt-2 text-sm text-slate-500">Utilisez votre adresse professionnelle et votre mot de passe.</p></div>
                @if ($errors->any())<div class="mb-5 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-bold text-rose-700">{{ $errors->first() }}</div>@endif
                <form method="POST" action="{{ route('admin.login.store') }}" class="space-y-5">
                    @csrf
                    <label class="block"><span class="mb-2 block text-sm font-bold text-slate-700">Adresse e-mail</span><input type="email" name="email" value="{{ old('email') }}" required autofocus autocomplete="email" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3.5 outline-none transition focus:border-sky-500 focus:bg-white focus:ring-4 focus:ring-sky-100"></label>
                    <label class="block"><span class="mb-2 block text-sm font-bold text-slate-700">Mot de passe</span><input type="password" name="password" required minlength="8" autocomplete="current-password" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3.5 outline-none transition focus:border-sky-500 focus:bg-white focus:ring-4 focus:ring-sky-100"></label>
                    <label class="flex items-center gap-3 text-sm font-semibold text-slate-500"><input type="checkbox" name="remember" value="1" class="h-4 w-4 rounded accent-sky-600"> Se souvenir de moi</label>
                    <button type="submit" class="w-full rounded-2xl bg-slate-950 px-5 py-4 text-sm font-black text-white shadow-lg shadow-slate-950/20 transition hover:-translate-y-0.5 hover:bg-sky-700">Ouvrir le dashboard</button>
                </form>
                <a href="{{ route('health') }}" class="mt-8 block text-center text-xs font-bold text-slate-400 hover:text-sky-600">Vérifier l’état du service</a>
            </section>
        </div>
    </main>
</body>
</html>
