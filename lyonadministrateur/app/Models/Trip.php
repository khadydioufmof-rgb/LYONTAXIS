<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Trip extends Model
{
    protected $fillable = [
        'user_id',
        'driver_id',
        'vehicle_category',
        'pickup_location',
        'dropoff_location',
        'intermediate_stops',
        'fare',
        'base_fare',
        'distance_fare',
        'time_fare',
        'stop_fee',
        'service_fee',
        'discount',
        'tip',
        'status',
        'distance_km',
        'duration_min',
        'payment_method',
        'preferences',
        'is_scheduled',
        'scheduled_at',
        'rating',
        'review_comment',
    ];

    protected $casts = [
        'pickup_location' => 'json',
        'dropoff_location' => 'json',
        'intermediate_stops' => 'json',
        'preferences' => 'json',
        'scheduled_at' => 'datetime',
        'fare' => 'decimal:2',
        'base_fare' => 'decimal:2',
        'distance_fare' => 'decimal:2',
        'time_fare' => 'decimal:2',
        'stop_fee' => 'decimal:2',
        'service_fee' => 'decimal:2',
        'discount' => 'decimal:2',
        'tip' => 'decimal:2',
        'distance_km' => 'decimal:2',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function driver(): BelongsTo
    {
        return $this->belongsTo(Driver::class);
    }

    public function commission(): \Illuminate\Database\Eloquent\Relations\HasOne
    {
        return $this->hasOne(Commission::class);
    }
}
