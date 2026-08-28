<?php

namespace App\Http\Controllers\Api;

use App\Models\Trip;
use App\Models\Driver;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class TripController
{
    /**
     * List user trips
     */
    public function index(Request $request): JsonResponse
    {
        $trips = $request->user()->trips()
            ->with(['driver'])
            ->latest()
            ->paginate(10);

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

    /**
     * Create new trip
     */
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'vehicle_category' => 'required|string|in:Eco,Sedan,Premium,Van',
            'pickup_latitude' => 'required|numeric',
            'pickup_longitude' => 'required|numeric',
            'dropoff_latitude' => 'required|numeric',
            'dropoff_longitude' => 'required|numeric',
            'pickup_address' => 'required|string',
            'dropoff_address' => 'required|string',
            'scheduled_for' => 'nullable|date|after:now',
            'passenger_count' => 'required|integer|min:1|max:8',
            'special_requests' => 'nullable|string',
        ]);

        // Find available driver (simple implementation)
        $driver = Driver::where('is_available', true)
            ->inRandomOrder()
            ->first();

        if (!$driver) {
            return response()->json([
                'success' => false,
                'message' => 'Aucun chauffeur disponible',
            ], 409);
        }

        // Calculate estimated fare (simplified)
        $estimatedFare = $this->calculateFare(
            $validated['pickup_latitude'],
            $validated['pickup_longitude'],
            $validated['dropoff_latitude'],
            $validated['dropoff_longitude'],
            $validated['vehicle_category']
        );

        $trip = $request->user()->trips()->create([
            'driver_id' => $driver->id,
            'vehicle_category' => $validated['vehicle_category'],
            'pickup_location' => [
                'latitude' => $validated['pickup_latitude'],
                'longitude' => $validated['pickup_longitude'],
                'address' => $validated['pickup_address'],
            ],
            'dropoff_location' => [
                'latitude' => $validated['dropoff_latitude'],
                'longitude' => $validated['dropoff_longitude'],
                'address' => $validated['dropoff_address'],
            ],
            'fare' => $estimatedFare,
            'base_fare' => $estimatedFare,
            'distance_fare' => 0,
            'time_fare' => 0,
            'status' => $validated['scheduled_for'] ? 'pending' : 'confirmed',
            'distance_km' => 0,
            'duration_min' => 0,
            'payment_method' => 'cash',
            'preferences' => [
                'passengers' => $validated['passenger_count'],
                'notes' => $validated['special_requests'] ?? '',
            ],
            'is_scheduled' => (bool) $validated['scheduled_for'],
            'scheduled_at' => $validated['scheduled_for'] ?? null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Course créée',
            'trip' => $trip->load('driver'),
        ], 201);
    }

    /**
     * Get trip details
     */
    public function show(Request $request, Trip $trip): JsonResponse
    {
        if ($trip->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'message' => 'Non autorisé',
            ], 403);
        }

        return response()->json([
            'success' => true,
            'trip' => $trip->load('driver'),
        ]);
    }

    /**
     * Update trip
     */
    public function update(Request $request, Trip $trip): JsonResponse
    {
        if ($trip->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'message' => 'Non autorisé',
            ], 403);
        }

        $validated = $request->validate([
            'special_requests' => 'nullable|string',
            'passenger_count' => 'sometimes|integer|min:1|max:8',
        ]);

        $trip->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Course mise à jour',
            'trip' => $trip,
        ]);
    }

    /**
     * Cancel trip
     */
    public function destroy(Request $request, Trip $trip): JsonResponse
    {
        if ($trip->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'message' => 'Non autorisé',
            ], 403);
        }

        if (in_array($trip->status, ['completed', 'cancelled'])) {
            return response()->json([
                'success' => false,
                'message' => 'Cette course ne peut pas être annulée',
            ], 409);
        }

        $trip->update(['status' => 'cancelled']);

        return response()->json([
            'success' => true,
            'message' => 'Course annulée',
        ]);
    }

    /**
     * Get active trip
     */
    public function active(Request $request): JsonResponse
    {
        $trip = $request->user()->trips()
            ->whereIn('status', ['pending', 'confirmed', 'driver_arriving', 'in_progress'])
            ->latest()
            ->first();

        if (!$trip) {
            return response()->json([
                'success' => false,
                'message' => 'Aucune course active',
            ], 404);
        }

        return response()->json([
            'success' => true,
            'trip' => $trip->load('driver'),
        ]);
    }

    /**
     * Rate trip
     */
    public function rate(Request $request, Trip $trip): JsonResponse
    {
        if ($trip->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'message' => 'Non autorisé',
            ], 403);
        }

        $validated = $request->validate([
            'rating' => 'required|integer|min:1|max:5',
            'comment' => 'nullable|string|max:500',
        ]);

        $trip->update([
            'rating' => $validated['rating'],
            'review_comment' => $validated['comment'],
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Merci pour votre avis',
        ]);
    }

    /**
     * Add tip
     */
    public function addTip(Request $request, Trip $trip): JsonResponse
    {
        if ($trip->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'message' => 'Non autorisé',
            ], 403);
        }

        $validated = $request->validate([
            'tip_amount' => 'required|numeric|min:0|max:100',
        ]);

        $trip->update(['tip' => $validated['tip_amount']]);

        return response()->json([
            'success' => true,
            'message' => 'Pourboire ajouté',
            'tip_amount' => $validated['tip_amount'],
        ]);
    }

    /**
     * Calculate fare based on distance and category
     */
    private function calculateFare($lat1, $lon1, $lat2, $lon2, $category): float
    {
        // Haversine formula for distance
        $earthRadius = 6371;
        $lat1Rad = deg2rad($lat1);
        $lon1Rad = deg2rad($lon1);
        $lat2Rad = deg2rad($lat2);
        $lon2Rad = deg2rad($lon2);

        $dlat = $lat2Rad - $lat1Rad;
        $dlon = $lon2Rad - $lon1Rad;

        $a = sin($dlat / 2) * sin($dlat / 2) +
             cos($lat1Rad) * cos($lat2Rad) * sin($dlon / 2) * sin($dlon / 2);
        $c = 2 * asin(sqrt($a));
        $distance = $earthRadius * $c;

        // Indicative Lyon VTC pricing: base fare, per-km rate, and minimum fare.
        $tariff = match($category) {
            'Eco' => ['base' => 5.00, 'per_km' => 1.50, 'minimum' => 12.00],
            'Sedan' => ['base' => 6.00, 'per_km' => 1.80, 'minimum' => 15.00],
            'Premium' => ['base' => 8.00, 'per_km' => 2.50, 'minimum' => 20.00],
            'Van' => ['base' => 9.00, 'per_km' => 2.80, 'minimum' => 24.00],
        };

        return max($tariff['minimum'], round($tariff['base'] + ($distance * $tariff['per_km']), 2));
    }

}
