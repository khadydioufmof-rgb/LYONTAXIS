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
        if (!Schema::hasTable('drivers')) {
            Schema::create('drivers', function (Blueprint $table) {
                $table->id();
                $table->string('name');
                $table->string('phone_number')->unique();
                $table->decimal('rating', 3, 1)->default(5);
                $table->integer('total_trips')->default(0);
                $table->string('car_model');
                $table->string('license_plate')->unique();
                $table->string('car_color');
                $table->string('vehicle_category');
                $table->boolean('is_available')->default(true);
                $table->decimal('latitude', 10, 8)->nullable();
                $table->decimal('longitude', 11, 8)->nullable();
                $table->string('avatar_seed')->nullable();
                $table->timestamps();
            });
        }

        if (Schema::hasTable('trips') && Schema::hasColumn('drivers', 'id')) {
            Schema::table('trips', function (Blueprint $table) {
                $table->foreign('driver_id')->references('id')->on('drivers')->nullOnDelete();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        if (Schema::hasColumn('drivers', 'id')) {
            Schema::table('trips', function (Blueprint $table) {
                $table->dropForeign(['driver_id']);
            });
        }

        Schema::dropIfExists('drivers');
    }
};
