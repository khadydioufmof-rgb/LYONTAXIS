<?php

namespace Database\Seeders;

use App\Models\Driver;
use App\Models\Trip;
use App\Models\User;
use Illuminate\Database\Seeder;

class TripsTableSeeder extends Seeder
{
    public function run(): void
    {
        $user = User::where('email', 'alice@example.com')->firstOrFail();
        $drivers = Driver::orderBy('id')->get();

        $trips = [
            ['driver_id' => $drivers->get(0)?->id, 'vehicle_category' => 'Eco', 'pickup_location' => ['name' => 'Place Bellecour', 'address' => 'Place Bellecour, Lyon'], 'dropoff_location' => ['name' => 'Part-Dieu', 'address' => 'Gare Part-Dieu, Lyon'], 'fare' => 18.50, 'base_fare' => 12, 'distance_fare' => 4.50, 'time_fare' => 2, 'status' => 'completed', 'distance_km' => 7.8, 'duration_min' => 14, 'payment_method' => 'visa', 'rating' => 5, 'preferences' => ['passengers' => 1]],
            ['driver_id' => $drivers->get(1)?->id, 'vehicle_category' => 'Sedan', 'pickup_location' => ['name' => 'Vieux Lyon', 'address' => 'Vieux Lyon, Lyon'], 'dropoff_location' => ['name' => 'Aéroport Saint-Exupéry', 'address' => 'Aéroport Lyon-Saint-Exupéry'], 'fare' => 52, 'base_fare' => 20, 'distance_fare' => 25, 'time_fare' => 7, 'status' => 'completed', 'distance_km' => 28.4, 'duration_min' => 32, 'payment_method' => 'cash', 'rating' => 5, 'preferences' => ['passengers' => 2]],
            ['driver_id' => null, 'vehicle_category' => 'Premium', 'pickup_location' => ['name' => 'Lyon 2', 'address' => 'Lyon 2e'], 'dropoff_location' => ['name' => 'Gare Perrache', 'address' => 'Gare de Lyon-Perrache'], 'fare' => 0, 'base_fare' => 12, 'distance_fare' => 4.90, 'time_fare' => 0, 'status' => 'cancelled', 'distance_km' => 5.2, 'duration_min' => 12, 'payment_method' => 'cash', 'preferences' => ['passengers' => 1]],
        ];

        foreach ($trips as $trip) {
            Trip::updateOrCreate(
                ['user_id' => $user->id, 'pickup_location->address' => $trip['pickup_location']['address'], 'dropoff_location->address' => $trip['dropoff_location']['address']],
                array_merge($trip, ['user_id' => $user->id])
            );
        }
    }
}
