<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Driver extends Model
{
    protected $fillable = [
        'name',
        'phone_number',
        'rating',
        'total_trips',
        'car_model',
        'license_plate',
        'car_color',
        'vehicle_category',
        'is_available',
        'latitude',
        'longitude',
        'avatar_seed',
    ];

    protected $casts = [
        'rating' => 'decimal:1',
        'latitude' => 'decimal:8',
        'longitude' => 'decimal:8',
    ];

    public function trips(): HasMany
    {
        return $this->hasMany(Trip::class);
    }
}
