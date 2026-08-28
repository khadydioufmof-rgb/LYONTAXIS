<?php

namespace Database\Seeders;

use App\Models\Driver;
use Illuminate\Database\Seeder;

class DriversTableSeeder extends Seeder
{
    public function run(): void
    {
        $drivers = [
            ['phone_number' => '+33 6 11 22 33 44', 'name' => 'Paul Martin', 'rating' => 4.9, 'total_trips' => 482, 'car_model' => 'Toyota Prius', 'license_plate' => 'AB-123-CD', 'car_color' => 'Noir', 'vehicle_category' => 'Eco', 'is_available' => true, 'latitude' => 45.7578, 'longitude' => 4.8320, 'avatar_seed' => 'PM'],
            ['phone_number' => '+33 6 22 33 44 55', 'name' => 'Nadia Leroy', 'rating' => 4.8, 'total_trips' => 367, 'car_model' => 'Peugeot 508', 'license_plate' => 'EF-456-GH', 'car_color' => 'Gris', 'vehicle_category' => 'Sedan', 'is_available' => true, 'latitude' => 45.7601, 'longitude' => 4.8357, 'avatar_seed' => 'NL'],
            ['phone_number' => '+33 6 33 44 55 66', 'name' => 'Luc Bernard', 'rating' => 4.7, 'total_trips' => 295, 'car_model' => 'Mercedes Classe E', 'license_plate' => 'IJ-789-KL', 'car_color' => 'Bleu nuit', 'vehicle_category' => 'Premium', 'is_available' => false, 'latitude' => 45.7485, 'longitude' => 4.8467, 'avatar_seed' => 'LB'],
        ];

        foreach ($drivers as $driver) {
            Driver::updateOrCreate(['phone_number' => $driver['phone_number']], $driver);
        }
    }
}
