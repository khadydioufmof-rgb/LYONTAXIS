<?php

use App\Http\Controllers\Web\PageController;
use Illuminate\Support\Facades\Route;

Route::get('/', [PageController::class, 'home'])->name('home');
Route::get('/admin', [PageController::class, 'admin'])->name('admin.dashboard');
Route::get('/login', [PageController::class, 'login'])->name('login');
Route::get('/booking', [PageController::class, 'booking'])->name('booking');
Route::post('/booking/save', [PageController::class, 'saveBooking'])->name('booking.save');
Route::get('/trips', [PageController::class, 'tripHistory'])->name('trips');
Route::get('/tracking', [PageController::class, 'tracking'])->name('tracking');
Route::get('/profile', [PageController::class, 'profile'])->name('profile');
Route::post('/profile/save', [PageController::class, 'saveProfile'])->name('profile.save');
Route::get('/notifications', [PageController::class, 'notifications'])->name('notifications');
Route::get('/payment-methods', [PageController::class, 'paymentMethods'])->name('payment-methods');
Route::post('/payment-methods/save', [PageController::class, 'savePaymentMethod'])->name('payment-methods.save');
