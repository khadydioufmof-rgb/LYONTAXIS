<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\Trip;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminTripController
{
    public function index(Request $request): JsonResponse
    {
        $query = Trip::with(['user', 'driver']);

        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        if ($request->filled('driver_id')) {
            $query->where('driver_id', $request->driver_id);
        }

        if ($request->filled('user_id')) {
            $query->where('user_id', $request->user_id);
        }

        $trips = $query->latest()->paginate(20);

        return response()->json([
            'success' => true,
            'trips' => $trips->items(),
            'pagination' => [
                'current_page' => $trips->currentPage(),
                'per_page' => $trips->perPage(),
                'total' => $trips->total(),
                'last_page' => $trips->lastPage(),
            ],
        ]);
    }

    public function show(Trip $trip): JsonResponse
    {
        return response()->json([
            'success' => true,
            'trip' => $trip->load(['user', 'driver']),
        ]);
    }

    public function updateStatus(Request $request, Trip $trip): JsonResponse
    {
        $validated = $request->validate([
            'status' => 'required|string|in:pending,confirmed,driver_arriving,in_progress,completed,cancelled',
        ]);

        $trip->update(['status' => $validated['status']]);

        return response()->json([
            'success' => true,
            'message' => 'Statut de la course mis à jour',
            'trip' => $trip->fresh()->load(['user', 'driver']),
        ]);
    }
}
