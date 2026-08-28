<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PaymentMethod extends Model
{
    protected $fillable = [
        'user_id',
        'type',
        'title',
        'subtitle',
        'is_default',
        'is_selected',
    ];

    protected $casts = [
        'is_default' => 'boolean',
        'is_selected' => 'boolean',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
