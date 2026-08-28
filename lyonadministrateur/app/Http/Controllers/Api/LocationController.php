<?php

namespace App\Http\Controllers\Api;

use App\Models\Location;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class LocationController
{
    /**
     * Get user saved locations
     */
    public function index(Request $request): JsonResponse
    {
        $locations = $request->user()->locations()
            ->orderBy('is_favorite', 'desc')
            ->orderBy('updated_at', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'locations' => $locations,
        ]);
    }

    /**
     * Save new location
     */
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'address' => 'required|string',
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'is_favorite' => 'boolean',
        ]);

        $location = $request->user()->locations()->create($validated);

        return response()->json([
            'success' => true,
            'message' => 'Lieu sauvegardé',
            'location' => $location,
        ], 201);
    }

    /**
     * Get popular locations in Lyon
     */
    public function popular(Request $request): JsonResponse
    {
        // Get popular locations (is_popular = true)
        $locations = Location::where('is_popular', true)
            ->orderBy('distance_km', 'asc')
            ->limit(20)
            ->get();

        return response()->json([
            'success' => true,
            'popular_locations' => $locations,
        ]);
    }

    /**
     * Search locations by query (simple text search)
     */
    public function search(Request $request): JsonResponse
    {
        $query = $request->input('q', '');
        
        if (strlen($query) < 2) {
            return response()->json([
                'success' => false,
                'message' => 'Veuillez entrer au moins 2 caractères',
            ], 400);
        }

        // Search in popular locations first
        $results = Location::where('is_popular', true)
            ->where(function($q) use ($query) {
                $q->where('title', 'like', "%{$query}%")
                  ->orWhere('address', 'like', "%{$query}%");
            })
            ->limit(10)
            ->get();

        // If user is authenticated, add their saved locations
        if ($request->user()) {
            $userLocations = $request->user()->locations()
                ->where(function($q) use ($query) {
                    $q->where('title', 'like', "%{$query}%")
                      ->orWhere('address', 'like', "%{$query}%");
                })
                ->limit(5)
                ->get();

            $results = $results->concat($userLocations);
        }

        return response()->json([
            'success' => true,
            'results' => $results->unique('id')->values(),
        ]);
    }
}
