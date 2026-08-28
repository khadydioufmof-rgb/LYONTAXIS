<?php

namespace Database\Seeders;

use App\Models\Location;
use App\Models\User;
use Illuminate\Database\Seeder;

class LocationsTableSeeder extends Seeder
{
    public function run(): void
    {
        $user = User::where('email', 'alice@example.com')->firstOrFail();
        $locations = [
            ['title' => 'Maison', 'address' => '12 rue de la République, Lyon', 'latitude' => 45.7640, 'longitude' => 4.8357, 'distance_km' => 0, 'is_favorite' => true, 'is_popular' => false],
            ['title' => 'Travail', 'address' => '3 rue de la Soie, Lyon Part-Dieu', 'latitude' => 45.7606, 'longitude' => 4.8594, 'distance_km' => 2.8, 'is_favorite' => true, 'is_popular' => false],
            ['title' => 'Parents', 'address' => '45 avenue Henri Barbusse, Lyon', 'latitude' => 45.7481, 'longitude' => 4.8500, 'distance_km' => 4.1, 'is_favorite' => false, 'is_popular' => false],
        ];

        foreach ($locations as $location) {
            Location::updateOrCreate(['user_id' => $user->id, 'title' => $location['title']], $location);
        }
    }
}
