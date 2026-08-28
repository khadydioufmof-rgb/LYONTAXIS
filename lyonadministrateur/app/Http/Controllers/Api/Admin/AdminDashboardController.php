<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\Driver;
use App\Models\Trip;
use App\Models\User;
use Illuminate\Http\JsonResponse;

class AdminDashboardController
{
    public function stats(): JsonResponse
    {
        $stats = [
            'customers' => User::count(),
            'drivers' => Driver::count(),
            'trips_total' => Trip::count(),
            'trips_active' => Trip::whereIn('status', ['pending', 'confirmed', 'driver_arriving', 'in_progress'])->count(),
            'trips_completed' => Trip::where('status', 'completed')->count(),
            'revenue' => (float) Trip::whereNotNull('fare')->sum('fare'),
        ];

        $recentTrips = Trip::with(['user', 'driver'])
            ->latest()
            ->take(10)
            ->get();

        return response()->json([
            'success' => true,
            'stats' => $stats,
            'recent_trips' => $recentTrips,
        ]);
    }
}
