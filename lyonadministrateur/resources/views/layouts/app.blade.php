<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'LyonTaxis')</title>
    <link rel="icon" type="image/jpeg" href="{{ asset('images/lyontaxis-logo.jpg') }}">
    <link rel="apple-touch-icon" href="{{ asset('images/lyontaxis-logo.jpg') }}">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        :root { --lyon-blue: #1e88e5; --lyon-deep-blue: #0d47a1; --lyon-cyan: #29b6f6; --lyon-yellow: #ffb300; --lyon-ink: #10141d; --lyon-muted: #747474; }
        html, body { max-width: 100%; overflow-x: hidden; }
        body { background: #f4f6f9; }
        .glass { backdrop-filter: blur(16px); }
        .brand-glow { box-shadow: 0 8px 20px rgba(0, 0, 0, .24); }
    </style>
</head>
<body class="min-h-screen text-slate-800">
    <div class="min-h-screen max-w-full overflow-x-hidden bg-[#f4f6f9]">
        <header class="sticky top-0 z-20 border-b border-black/10 bg-white/95 text-[#10141d] shadow-sm glass">
            <div class="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
                <a href="/booking" class="flex items-center gap-3" aria-label="Réserver un taxi LyonTaxis">
                    <img src="{{ asset('images/lyontaxis-logo.jpg') }}" alt="Logo LyonTaxis" class="brand-glow h-12 w-12 rounded-2xl object-cover ring-1 ring-white/20">
                    <div>
                        <div class="text-lg font-black tracking-tight">Lyon<span class="text-[#1e88e5]">Taxis</span></div>
                        <div class="text-[11px] font-medium uppercase tracking-[0.22em] text-neutral-500">Votre trajet, simplement</div>
                    </div>
                </a>

                <nav class="hidden items-center gap-1 lg:flex" aria-label="Navigation ERP">
                    <a href="{{ route('admin.dashboard') }}" class="rounded-lg px-3 py-2 text-sm font-bold text-[#0d47a1] hover:bg-blue-50">Dashboard</a>
                    <a href="{{ route('admin.drivers') }}" class="rounded-lg px-3 py-2 text-sm font-semibold text-neutral-600 hover:bg-slate-100">Chauffeurs</a>
                    <a href="{{ route('trips') }}" class="rounded-lg px-3 py-2 text-sm font-semibold text-neutral-600 hover:bg-slate-100">Courses</a>
                    <a href="{{ route('profile') }}" class="rounded-lg px-3 py-2 text-sm font-semibold text-neutral-600 hover:bg-slate-100">Clients</a>
                    <a href="{{ route('notifications') }}" class="rounded-lg px-3 py-2 text-sm font-semibold text-neutral-600 hover:bg-slate-100">Alertes</a>
                </nav>

                <div class="flex items-center gap-3">
                    <span class="hidden text-xs font-semibold uppercase tracking-[0.18em] text-neutral-500 sm:block">ERP central</span>
                    <a href="{{ route('booking') }}" class="rounded-xl bg-[#ffb300] px-5 py-3 text-sm font-bold text-[#10141d] shadow-lg shadow-yellow-900/10 transition hover:-translate-y-0.5 hover:bg-[#ffc107]">Nouvelle course</a>
                    @auth
                        @if (auth()->user()->role === 'admin')
                            <form method="POST" action="{{ route('admin.logout') }}">@csrf<button type="submit" class="hidden rounded-xl border border-slate-200 px-3 py-3 text-sm font-bold text-slate-600 hover:bg-slate-100 sm:block">Déconnexion</button></form>
                        @endif
                    @endauth
                </div>
            </div>
        </header>

        <main>
            @yield('content')
        </main>
    </div>
</body>
</html>
