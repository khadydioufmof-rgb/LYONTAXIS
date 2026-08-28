<?php

namespace Database\Seeders;

use App\Models\Notification;
use App\Models\User;
use Illuminate\Database\Seeder;

class NotificationsTableSeeder extends Seeder
{
    public function run(): void
    {
        $user = User::where('email', 'alice@example.com')->firstOrFail();
        $notifications = [
            ['type' => 'trip', 'title' => 'Votre chauffeur est en route', 'description' => 'Paul Martin arrive dans 2 minutes sur votre adresse.', 'is_read' => false],
            ['type' => 'payment', 'title' => 'Paiement validé', 'description' => 'Le paiement pour votre course a bien été enregistré.', 'is_read' => true],
            ['type' => 'promotion', 'title' => 'Nouvelle promotion', 'description' => 'Profitez de 10% de réduction pour votre prochain trajet.', 'is_read' => true],
        ];

        foreach ($notifications as $notification) {
            Notification::updateOrCreate(
                ['user_id' => $user->id, 'title' => $notification['title']],
                $notification
            );
        }
    }
}
