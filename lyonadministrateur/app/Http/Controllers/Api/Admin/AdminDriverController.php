<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\Driver;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminDriverController
{
    public function index(Request $request): JsonResponse
    {
        $query = Driver::query();

        if ($request->filled('status')) {
            $query->where('is_available', $request->boolean('status'));
        }

        if ($request->filled('vehicle_category')) {
            $query->where('vehicle_category', $request->vehicle_category);
        }

        $drivers = $query->latest()->paginate(20);

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

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'phone_number' => 'required|string|unique:drivers,phone_number',
            'car_model' => 'required|string',
            'license_plate' => 'required|string|unique:drivers,license_plate',
            'car_color' => 'nullable|string',
            'vehicle_category' => 'required|string',
            'is_available' => 'boolean',
            'latitude' => 'nullable|numeric',
            'longitude' => 'nullable|numeric',
        ]);

        $driver = Driver::create($validated);

        return response()->json([
            'success' => true,
            'message' => 'Chauffeur créé avec succès',
            'driver' => $driver,
        ], 201);
    }

    public function show(Driver $driver): JsonResponse
    {
        return response()->json([
            'success' => true,
            'driver' => $driver->load('trips'),
        ]);
    }

    public function update(Request $request, Driver $driver): JsonResponse
    {
        $validated = $request->validate([
            'name' => 'sometimes|string|max:255',
            'phone_number' => 'sometimes|string|unique:drivers,phone_number,' . $driver->id,
            'car_model' => 'sometimes|string',
            'license_plate' => 'sometimes|string|unique:drivers,license_plate,' . $driver->id,
            'car_color' => 'nullable|string',
            'vehicle_category' => 'sometimes|string',
            'is_available' => 'sometimes|boolean',
            'latitude' => 'nullable|numeric',
            'longitude' => 'nullable|numeric',
        ]);

        $driver->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Chauffeur mis à jour',
            'driver' => $driver,
        ]);
    }

    public function destroy(Driver $driver): JsonResponse
    {
        $driver->delete();

        return response()->json([
            'success' => true,
            'message' => 'Chauffeur supprimé',
        ]);
    }
}
