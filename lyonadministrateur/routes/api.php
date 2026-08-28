<?php

use App\Http\Controllers\Api\Admin\AdminDashboardController;
use App\Http\Controllers\Api\Admin\AdminDriverController;
use App\Http\Controllers\Api\Admin\AdminTripController;
use App\Http\Controllers\Api\Admin\AdminUserController;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\DriverController;
use App\Http\Controllers\Api\LocationController;
use App\Http\Controllers\Api\TripController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API v1 - Multi-plateforme LyonTaxis
|--------------------------------------------------------------------------
| Ce backend sert :
| - admin
| - driver
| - client
|
| Chaque plateforme utilise le même backend avec un préfixe dédié.
*/

Route::prefix('v1')->group(function () {
    Route::prefix('admin')->middleware(['auth:sanctum', 'platform.role:admin'])->group(function () {
        Route::post('auth/logout', [AuthController::class, 'logout']);
        Route::get('auth/user', [AuthController::class, 'user']);

        Route::get('dashboard/stats', [AdminDashboardController::class, 'stats']);

        Route::get('drivers', [AdminDriverController::class, 'index']);
        Route::post('drivers', [AdminDriverController::class, 'store']);
        Route::get('drivers/{driver}', [AdminDriverController::class, 'show']);
        Route::put('drivers/{driver}', [AdminDriverController::class, 'update']);
        Route::delete('drivers/{driver}', [AdminDriverController::class, 'destroy']);

        Route::get('trips', [AdminTripController::class, 'index']);
        Route::get('trips/{trip}', [AdminTripController::class, 'show']);
        Route::patch('trips/{trip}/status', [AdminTripController::class, 'updateStatus']);

        Route::get('users', [AdminUserController::class, 'index']);
        Route::get('users/{user}', [AdminUserController::class, 'show']);
        Route::put('users/{user}', [AdminUserController::class, 'update']);
    });

    $platforms = ['driver', 'client'];

    foreach ($platforms as $platform) {
        Route::prefix($platform)->group(function () use ($platform) {
            Route::prefix('auth')->group(function () use ($platform) {
                if ($platform === 'driver') {
                    Route::post('login', [AuthController::class, 'loginWithPassword'])->middleware('throttle:5,1');
                } else {
                    Route::post('request-otp', [AuthController::class, 'requestOtp'])->middleware('throttle:10,1');
                    Route::post('verify-otp', [AuthController::class, 'verifyOtp'])->middleware('throttle:10,1');
                }
                Route::post('logout', [AuthController::class, 'logout'])->middleware('auth:sanctum');
                Route::get('user', [AuthController::class, 'user'])->middleware('auth:sanctum');
            });

            Route::middleware(['auth:sanctum', 'platform.role:' . $platform])->group(function () use ($platform) {
                Route::prefix('trips')->group(function () {
                    Route::get('/', [TripController::class, 'index']);
                    Route::post('/', [TripController::class, 'store']);
                    Route::get('{trip}', [TripController::class, 'show']);
                    Route::patch('{trip}', [TripController::class, 'update']);
                    Route::delete('{trip}', [TripController::class, 'destroy']);
                    Route::get('{trip}/active', [TripController::class, 'active']);
                    Route::post('{trip}/rate', [TripController::class, 'rate']);
                    Route::post('{trip}/tip', [TripController::class, 'addTip']);
                });

                Route::prefix('drivers')->group(function () {
                    Route::get('/', [DriverController::class, 'index']);
                    Route::get('{driver}', [DriverController::class, 'show']);
                });

                Route::prefix('locations')->group(function () {
                    Route::get('/', [LocationController::class, 'index']);
                    Route::post('/', [LocationController::class, 'store']);
                    Route::get('popular', [LocationController::class, 'popular']);
                    Route::get('search', [LocationController::class, 'search']);
                });

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
        });
    }

    Route::prefix('shared')->group(function () {
        Route::get('health', fn () => response()->json([
            'status' => 'ok',
            'service' => 'lyonadministrateur',
            'platforms' => ['admin', 'driver', 'client'],
            'version' => 'v1',
        ]));
    });
});
