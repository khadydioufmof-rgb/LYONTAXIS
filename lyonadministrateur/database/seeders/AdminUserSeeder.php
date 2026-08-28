<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;

class AdminUserSeeder extends Seeder
{
    public function run(): void
    {
        $email = env('ERP_ADMIN_EMAIL', 'admin@lyontaxis.local');
        $password = env('ERP_ADMIN_PASSWORD', 'ChangeMe123!');

        User::updateOrCreate(
            ['email' => $email],
            [
                'name' => env('ERP_ADMIN_NAME', 'Administrateur LyonTaxis'),
                'password' => $password,
                'role' => 'admin',
                'email_verified_at' => now(),
            ],
        );
    }
}
