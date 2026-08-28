<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('trips', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->unsignedBigInteger('driver_id')->nullable();
            $table->string('vehicle_category');
            $table->json('pickup_location');
            $table->json('dropoff_location');
            $table->json('intermediate_stops')->nullable();
            $table->decimal('fare', 8, 2);
            $table->decimal('base_fare', 8, 2);
            $table->decimal('distance_fare', 8, 2);
            $table->decimal('time_fare', 8, 2);
            $table->decimal('stop_fee', 8, 2)->default(0);
            $table->decimal('service_fee', 8, 2)->default(1.00);
            $table->decimal('discount', 8, 2)->default(0);
            $table->decimal('tip', 8, 2)->default(0);
            $table->string('status')->default('pending');
            $table->decimal('distance_km', 8, 2);
            $table->integer('duration_min');
            $table->string('payment_method')->default('cash');
            $table->json('preferences')->nullable();
            $table->boolean('is_scheduled')->default(false);
            $table->timestamp('scheduled_at')->nullable();
            $table->integer('rating')->nullable();
            $table->text('review_comment')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('trips');
    }
};
