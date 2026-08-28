-- LyonTaxis Supabase schema
-- Run this script once in Supabase Dashboard > SQL Editor.
-- This schema uses auth.uid() and Row Level Security for tenant isolation.

create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = timezone('utc', now());
  return new;
end;
$$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  name text not null default '',
  email text,
  phone_number text,
  gender text,
  birthday date,
  emergency_contact text,
  home_address text,
  member_level text not null default 'Membre',
  cash_balance numeric(10, 2) not null default 0 check (cash_balance >= 0),
  integral_points integer not null default 0 check (integral_points >= 0),
  coupons_count integer not null default 0 check (coupons_count >= 0),
  referral_code text unique,
  avatar_seed text,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.saved_locations (
  id text not null,
  user_id uuid not null references auth.users(id) on delete cascade,
  title text not null,
  address text not null,
  latitude double precision not null,
  longitude double precision not null,
  distance_km double precision not null default 0 check (distance_km >= 0),
  is_favorite boolean not null default false,
  created_at timestamptz not null default timezone('utc', now()),
  primary key (user_id, id)
);

create table if not exists public.payment_methods (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  provider text not null default 'stripe',
  provider_payment_method_id text not null,
  type text not null check (type in ('cash', 'visa', 'paypal', 'mastercard')),
  title text not null,
  subtitle text not null default '',
  is_default boolean not null default false,
  created_at timestamptz not null default timezone('utc', now()),
  unique (user_id, provider, provider_payment_method_id)
);

create unique index if not exists one_default_payment_method_per_user
on public.payment_methods(user_id)
where is_default;

create table if not exists public.rides (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  pickup_location jsonb not null,
  dropoff_location jsonb not null,
  vehicle text not null,
  driver jsonb,
  status text not null default 'pending' check (status in ('pending', 'confirmed', 'driver_arriving', 'in_progress', 'completed', 'cancelled')),
  fare numeric(10, 2) not null check (fare >= 0),
  base_fare numeric(10, 2) not null default 0,
  distance_fare numeric(10, 2) not null default 0,
  time_fare numeric(10, 2) not null default 0,
  stop_fee numeric(10, 2) not null default 0,
  service_fee numeric(10, 2) not null default 0,
  discount numeric(10, 2) not null default 0,
  tip numeric(10, 2) not null default 0,
  distance_km double precision not null default 0,
  duration_min integer not null default 0,
  payment_method_id uuid references public.payment_methods(id) on delete set null,
  payment_method_title text not null default 'Espèces LyonTaxis',
  preferences jsonb not null default '{}'::jsonb,
  scheduled_for timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.ride_stops (
  id uuid primary key default gen_random_uuid(),
  ride_id uuid not null references public.rides(id) on delete cascade,
  position integer not null check (position between 0 and 3),
  location jsonb not null,
  unique (ride_id, position)
);

create table if not exists public.scheduled_rides (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  pickup_location jsonb not null,
  dropoff_location jsonb not null,
  vehicle text not null,
  scheduled_for timestamptz not null,
  estimated_fare numeric(10, 2) not null check (estimated_fare >= 0),
  payment_method_id uuid references public.payment_methods(id) on delete set null,
  payment_method_title text not null default 'Espèces LyonTaxis',
  preferences jsonb not null default '{}'::jsonb,
  special_instructions text not null default '',
  status text not null default 'confirmed' check (status in ('confirmed', 'reminder_enabled', 'cancelled')),
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.scheduled_ride_stops (
  id uuid primary key default gen_random_uuid(),
  scheduled_ride_id uuid not null references public.scheduled_rides(id) on delete cascade,
  position integer not null check (position between 0 and 3),
  location jsonb not null,
  unique (scheduled_ride_id, position)
);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  type text not null,
  title text not null,
  description text not null,
  is_read boolean not null default false,
  created_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.contacts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  phone text not null,
  is_invited boolean not null default false,
  unique (user_id, phone)
);

create table if not exists public.chat_messages (
  id uuid primary key default gen_random_uuid(),
  ride_id uuid not null references public.rides(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  is_from_user boolean not null,
  text text not null default '',
  message_type text not null default 'text' check (message_type in ('text', 'audio', 'location', 'system_status')),
  location_title text,
  audio_duration_sec integer not null default 0 check (audio_duration_sec >= 0),
  status text not null default 'sent' check (status in ('sending', 'sent', 'delivered', 'read')),
  created_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.user_roles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  role text not null check (role in ('passenger', 'driver', 'dispatcher', 'admin')),
  created_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.drivers (
  user_id uuid primary key references auth.users(id) on delete cascade,
  name text not null,
  phone_number text,
  car_model text not null,
  license_plate text not null unique,
  rating numeric(2, 1) not null default 5.0 check (rating between 0 and 5),
  is_available boolean not null default false,
  latitude double precision,
  longitude double precision,
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.payment_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  ride_id uuid references public.rides(id) on delete set null,
  provider text not null default 'stripe',
  provider_payment_intent_id text unique,
  amount numeric(10, 2) not null check (amount >= 0),
  currency text not null default 'eur',
  status text not null check (status in ('pending', 'requires_action', 'succeeded', 'failed', 'refunded')),
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.device_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  token text not null unique,
  platform text not null default 'android' check (platform in ('android', 'ios', 'web')),
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create index if not exists rides_user_created_idx on public.rides(user_id, created_at desc);
create index if not exists scheduled_rides_user_date_idx on public.scheduled_rides(user_id, scheduled_for);
create index if not exists notifications_user_created_idx on public.notifications(user_id, created_at desc);
create index if not exists chat_messages_ride_created_idx on public.chat_messages(ride_id, created_at);
create index if not exists drivers_available_idx on public.drivers(is_available);
create index if not exists payment_transactions_user_idx on public.payment_transactions(user_id, created_at desc);
create index if not exists device_tokens_user_idx on public.device_tokens(user_id);

do $$
begin
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'rides') then
    alter publication supabase_realtime add table public.rides;
  end if;
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'chat_messages') then
    alter publication supabase_realtime add table public.chat_messages;
  end if;
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'notifications') then
    alter publication supabase_realtime add table public.notifications;
  end if;
  if not exists (select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'drivers') then
    alter publication supabase_realtime add table public.drivers;
  end if;
end;
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.profiles (id, email, phone_number, name)
  values (new.id, new.email, new.phone, coalesce(new.raw_user_meta_data ->> 'name', ''))
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at before update on public.profiles
for each row execute procedure public.set_updated_at();

drop trigger if exists rides_set_updated_at on public.rides;
create trigger rides_set_updated_at before update on public.rides
for each row execute procedure public.set_updated_at();

drop trigger if exists scheduled_rides_set_updated_at on public.scheduled_rides;
create trigger scheduled_rides_set_updated_at before update on public.scheduled_rides
for each row execute procedure public.set_updated_at();

create or replace function public.protect_ride_fields()
returns trigger
language plpgsql
as $$
begin
  if auth.uid() is not null and (
    new.user_id <> old.user_id or
    new.fare <> old.fare or
    new.base_fare <> old.base_fare or
    new.distance_fare <> old.distance_fare or
    new.time_fare <> old.time_fare or
    new.stop_fee <> old.stop_fee or
    new.service_fee <> old.service_fee or
    new.discount <> old.discount or
    new.driver is distinct from old.driver or
    (new.status <> old.status and new.status <> 'cancelled')
  ) then
    raise exception 'Protected ride fields can only be changed by the backend';
  end if;
  return new;
end;
$$;

drop trigger if exists protect_ride_fields on public.rides;
create trigger protect_ride_fields before update on public.rides
for each row execute procedure public.protect_ride_fields();

alter table public.profiles enable row level security;
alter table public.saved_locations enable row level security;
alter table public.payment_methods enable row level security;
alter table public.rides enable row level security;
alter table public.ride_stops enable row level security;
alter table public.scheduled_rides enable row level security;
alter table public.scheduled_ride_stops enable row level security;
alter table public.notifications enable row level security;
alter table public.contacts enable row level security;
alter table public.chat_messages enable row level security;
alter table public.user_roles enable row level security;
alter table public.drivers enable row level security;
alter table public.payment_transactions enable row level security;
alter table public.device_tokens enable row level security;

drop policy if exists profiles_owner on public.profiles;
drop policy if exists saved_locations_owner on public.saved_locations;
drop policy if exists payment_methods_owner on public.payment_methods;
drop policy if exists rides_owner on public.rides;
drop policy if exists ride_stops_owner on public.ride_stops;
drop policy if exists scheduled_rides_owner on public.scheduled_rides;
drop policy if exists scheduled_ride_stops_owner on public.scheduled_ride_stops;
drop policy if exists notifications_owner on public.notifications;
drop policy if exists contacts_owner on public.contacts;
drop policy if exists chat_messages_owner on public.chat_messages;
drop policy if exists user_roles_owner on public.user_roles;
drop policy if exists drivers_authenticated_read on public.drivers;
drop policy if exists payment_transactions_owner on public.payment_transactions;
drop policy if exists device_tokens_owner on public.device_tokens;
drop policy if exists rides_select_owner on public.rides;
drop policy if exists rides_insert_owner on public.rides;
drop policy if exists ride_stops_select_owner on public.ride_stops;
drop policy if exists ride_stops_insert_owner on public.ride_stops;

create policy profiles_owner on public.profiles for all using (id = auth.uid()) with check (id = auth.uid());
create policy saved_locations_owner on public.saved_locations for all using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy payment_methods_owner on public.payment_methods for all using (user_id = auth.uid()) with check (user_id = auth.uid());
drop policy if exists rides_owner on public.rides;
drop policy if exists ride_stops_owner on public.ride_stops;
create policy rides_select_owner on public.rides for select using (user_id = auth.uid());
create policy rides_insert_owner on public.rides for insert with check (user_id = auth.uid() and status in ('pending', 'confirmed'));
create policy ride_stops_select_owner on public.ride_stops for select using (exists (select 1 from public.rides r where r.id = ride_id and r.user_id = auth.uid()));
create policy ride_stops_insert_owner on public.ride_stops for insert with check (exists (select 1 from public.rides r where r.id = ride_id and r.user_id = auth.uid()));
create policy scheduled_rides_owner on public.scheduled_rides for all using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy scheduled_ride_stops_owner on public.scheduled_ride_stops for all using (exists (select 1 from public.scheduled_rides r where r.id = scheduled_ride_id and r.user_id = auth.uid())) with check (exists (select 1 from public.scheduled_rides r where r.id = scheduled_ride_id and r.user_id = auth.uid()));
create policy notifications_owner on public.notifications for all using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy contacts_owner on public.contacts for all using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy chat_messages_owner on public.chat_messages for all using (user_id = auth.uid() or exists (select 1 from public.rides r where r.id = ride_id and r.user_id = auth.uid())) with check (user_id = auth.uid());
create policy user_roles_owner on public.user_roles for select using (user_id = auth.uid());
create policy drivers_authenticated_read on public.drivers for select to authenticated using (true);
create policy payment_transactions_owner on public.payment_transactions for select using (user_id = auth.uid());
create policy device_tokens_owner on public.device_tokens for all using (user_id = auth.uid()) with check (user_id = auth.uid());
