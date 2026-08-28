<?php

use App\Http\Controllers\Web\PageController;
use App\Http\Controllers\Web\AdminAuthController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => redirect()->route('admin.dashboard'));
Route::get('/admin/login', [AdminAuthController::class, 'create'])->middleware('guest')->name('admin.login');
Route::post('/admin/login', [AdminAuthController::class, 'store'])->middleware('guest')->name('admin.login.store');
Route::post('/admin/logout', [AdminAuthController::class, 'destroy'])->middleware('auth')->name('admin.logout');

Route::middleware(['auth', 'platform.role:admin'])->group(function () {
    Route::get('/admin', [PageController::class, 'admin'])->name('admin.dashboard');
    Route::get('/admin/dashboard', [PageController::class, 'admin'])->name('admin.dashboard.view');
    Route::get('/admin/drivers', [PageController::class, 'drivers'])->name('admin.drivers');
    Route::post('/admin/drivers', [PageController::class, 'saveDriver'])->name('admin.drivers.save');
});

Route::get('/login', [PageController::class, 'login'])->name('login');
Route::get('/booking', [PageController::class, 'booking'])->name('booking');
Route::post('/booking', [PageController::class, 'saveBooking'])->name('booking.save');
Route::get('/trips', [PageController::class, 'tripHistory'])->name('trips');
Route::get('/profile', [PageController::class, 'profile'])->name('profile');
Route::post('/profile', [PageController::class, 'saveProfile'])->name('profile.save');
Route::get('/notifications', [PageController::class, 'notifications'])->name('notifications');
Route::get('/payment-methods', [PageController::class, 'paymentMethods'])->name('payment-methods');
Route::post('/payment-methods', [PageController::class, 'savePaymentMethod'])->name('payment-methods.save');
Route::get('/tracking', [PageController::class, 'tracking'])->name('tracking');

Route::get('/health', fn () => response()->json([
    'status' => 'ok',
    'service' => 'lyonadministrateur',
]))->name('health');
