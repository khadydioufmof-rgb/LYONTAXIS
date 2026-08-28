<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\TripController;
use App\Http\Controllers\Api\DriverController;
use App\Http\Controllers\Api\LocationController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
| LyonTaxis Web API Routes - Parity with Android Client
*/

// Auth Routes (Public)
Route::prefix('auth')->group(function () {
    Route::post('request-otp', [AuthController::class, 'requestOtp']);
    Route::post('verify-otp', [AuthController::class, 'verifyOtp']);
    Route::post('logout', [AuthController::class, 'logout'])->middleware('auth:sanctum');
    Route::get('user', [AuthController::class, 'user'])->middleware('auth:sanctum');
});

// Protected Routes
Route::middleware('auth:sanctum')->group(function () {
    
    // Trips/Bookings
    Route::prefix('trips')->group(function () {
        Route::get('/', [TripController::class, 'index']); // Get user's trips
        Route::post('/', [TripController::class, 'store']); // Create new trip
        Route::get('{trip}', [TripController::class, 'show']); // Get trip details
        Route::patch('{trip}', [TripController::class, 'update']); // Update trip
        Route::delete('{trip}', [TripController::class, 'destroy']); // Cancel trip
        Route::get('{trip}/active', [TripController::class, 'active']); // Get active trip
        Route::post('{trip}/rate', [TripController::class, 'rate']); // Rate trip
        Route::post('{trip}/tip', [TripController::class, 'addTip']); // Add tip
    });

    // Drivers
    Route::prefix('drivers')->group(function () {
        Route::get('/', [DriverController::class, 'index']); // List available drivers
        Route::get('{driver}', [DriverController::class, 'show']); // Get driver details
    });

    // Locations/Addresses
    Route::prefix('locations')->group(function () {
        Route::get('/', [LocationController::class, 'index']); // Get popular locations
        Route::post('/', [LocationController::class, 'store']); // Save location
        Route::get('popular', [LocationController::class, 'popular']); // Get popular locations
        Route::get('search', [LocationController::class, 'search']); // Search locations
    });

    // Profile & Account
    Route::prefix('user')->group(function () {
        Route::get('profile', [AuthController::class, 'profile']);
        Route::patch('profile', [AuthController::class, 'updateProfile']);
        Route::get('payment-methods', [AuthController::class, 'paymentMethods']);
        Route::post('payment-methods', [AuthController::class, 'addPaymentMethod']);
        Route::delete('payment-methods/{id}', [AuthController::class, 'deletePaymentMethod']);
        Route::get('notifications', [AuthController::class, 'notifications']);
        Route::patch('notifications/{id}/read', [AuthController::class, 'markNotificationAsRead']);
    });
});
