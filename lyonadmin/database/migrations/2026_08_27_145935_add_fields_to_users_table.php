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
        Schema::table('users', function (Blueprint $table) {
            $table->string('phone_number')->nullable()->after('email');
            $table->string('gender')->nullable()->after('phone_number');
            $table->date('birthday')->nullable()->after('gender');
            $table->string('emergency_contact')->nullable()->after('birthday');
            $table->string('home_address')->nullable()->after('emergency_contact');
            $table->string('member_level')->default('Membre')->after('home_address');
            $table->decimal('cash_balance', 10, 2)->default(0)->after('member_level');
            $table->integer('integral_points')->default(0)->after('cash_balance');
            $table->integer('coupons_count')->default(0)->after('integral_points');
            $table->string('referral_code')->unique()->nullable()->after('coupons_count');
            $table->string('avatar_seed')->nullable()->after('referral_code');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            //
        });
    }
};
