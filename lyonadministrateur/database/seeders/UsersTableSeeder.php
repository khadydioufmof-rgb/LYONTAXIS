<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;

class UsersTableSeeder extends Seeder
{
    public function run(): void
    {
        User::updateOrCreate(
            ['email' => 'alice@example.com'],
            [
                'name' => 'Alice Martin',
                'phone_number' => '+33 6 12 34 56 78',
                'gender' => 'Femme',
                'birthday' => '1992-04-18',
                'emergency_contact' => '+33 6 98 76 54 32',
                'home_address' => '12 rue de la République, Lyon',
                'member_level' => 'Premium',
                'cash_balance' => 42.30,
                'integral_points' => 1240,
                'coupons_count' => 3,
                'referral_code' => 'LYN-2048',
                'avatar_seed' => 'AM',
                'email_verified_at' => now(),
                'password' => 'password',
            ]
        );
    }
}
