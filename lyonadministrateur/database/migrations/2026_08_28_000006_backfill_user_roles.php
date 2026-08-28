<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        DB::table('users')->whereNull('role')->update(['role' => 'client']);
    }

    public function down(): void
    {
        // Existing users keep their role when this data backfill is rolled back.
    }
};
