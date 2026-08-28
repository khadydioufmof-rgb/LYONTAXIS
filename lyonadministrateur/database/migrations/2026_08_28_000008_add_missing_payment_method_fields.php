<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasColumn('payment_methods', 'is_selected')) {
            Schema::table('payment_methods', function (Blueprint $table) {
                $table->boolean('is_selected')->default(false);
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasColumn('payment_methods', 'is_selected')) {
            Schema::table('payment_methods', function (Blueprint $table) {
                $table->dropColumn('is_selected');
            });
        }
    }
};
