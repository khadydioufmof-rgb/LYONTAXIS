<?php

namespace App\Http\Controllers\Api;

use App\Models\Driver;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class DriverController
{
    /**
     * Get available drivers
     */
    public function index(Request $request): JsonResponse
    {
        $query = Driver::where('is_available', true);

        // Filter by vehicle category
        if ($request->has('vehicle_category')) {
            $query->where('vehicle_category', $request->input('vehicle_category'));
        }

        // Filter by minimum rating
        if ($request->has('min_rating')) {
            $query->where('rating', '>=', $request->input('min_rating'));
        }

        $drivers = $query->paginate(20);

        return response()->json([
            'success' => true,
            'drivers' => $drivers->items(),
            'pagination' => [
                'current_page' => $drivers->currentPage(),
                'per_page' => $drivers->perPage(),
                'total' => $drivers->total(),
                'last_page' => $drivers->lastPage(),
            ],
        ]);
    }

    /**
     * Get driver details
     */
    public function show(Request $request, Driver $driver): JsonResponse
    {
        $tripCount = $driver->trips()->count();
        $avgRating = $driver->trips()
            ->whereNotNull('driver_rating')
            ->avg('driver_rating') ?? 0;

        return response()->json([
            'success' => true,
            'driver' => [
                'id' => $driver->id,
                'name' => $driver->name,
                'phone_number' => $driver->phone_number,
                'rating' => (float) $driver->rating,
                'total_trips' => $tripCount,
                'average_rating' => (float) round($avgRating, 1),
                'vehicle_brand' => $driver->vehicle_brand,
                'vehicle_model' => $driver->vehicle_model,
                'vehicle_color' => $driver->vehicle_color,
                'license_plate' => $driver->license_plate,
                'vehicle_category' => $driver->vehicle_category,
                'is_available' => $driver->is_available,
                'latitude' => (float) $driver->latitude,
                'longitude' => (float) $driver->longitude,
            ],
        ]);
    }
}
