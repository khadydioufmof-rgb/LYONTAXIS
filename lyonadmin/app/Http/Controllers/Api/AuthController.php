<?php

namespace App\Http\Controllers\Api;

use App\Models\User;
use App\Models\PaymentMethod;
use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController
{
    /**
     * Request OTP for authentication
     */
    public function requestOtp(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'phone_or_email' => 'required|string',
        ]);

        $identifier = trim($validated['phone_or_email']);

        try {
            // In production, integrate with Supabase
            // For now, generate a 4-digit code
            $otpCode = str_pad(random_int(0, 9999), 4, '0', STR_PAD_LEFT);
            
            // Store in cache for 10 minutes
            cache(['otp_' . $identifier => $otpCode], now()->addMinutes(10));
            
            // Log for testing
            \Log::info("OTP for {$identifier}: {$otpCode}");

            return response()->json([
                'success' => true,
                'message' => 'OTP envoyé avec succès',
                'identifier' => $identifier,
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Erreur lors de l\'envoi du code',
            ], 500);
        }
    }

    /**
     * Verify OTP and create/login user
     */
    public function verifyOtp(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'phone_or_email' => 'required|string',
            'code' => 'required|string|size:4',
        ]);

        $identifier = trim($validated['phone_or_email']);
        $code = $validated['code'];

        // Verify OTP from cache
        $cachedOtp = cache('otp_' . $identifier);
        if (!$cachedOtp || $cachedOtp !== $code) {
            throw ValidationException::withMessages([
                'code' => 'Code OTP invalide ou expiré',
            ]);
        }

        try {
            $isEmail = str_contains($identifier, '@');

            // Find or create user
            if ($isEmail) {
                $user = User::firstOrCreate(
                    ['email' => $identifier],
                    [
                        'name' => explode('@', $identifier)[0],
                        'password' => Hash::make($code),
                        'member_level' => 'Membre',
                        'cash_balance' => 0,
                        'integral_points' => 0,
                        'coupons_count' => 0,
                    ]
                );
            } else {
                $user = User::firstOrCreate(
                    ['phone_number' => $identifier],
                    [
                        'name' => 'Utilisateur LyonTaxis',
                        'email' => 'user_' . time() . '@lyontaxis.local',
                        'password' => Hash::make($code),
                        'member_level' => 'Membre',
                        'cash_balance' => 0,
                        'integral_points' => 0,
                        'coupons_count' => 0,
                    ]
                );
            }

            // Create payment method if new user
            if ($user->wasRecentlyCreated) {
                PaymentMethod::create([
                    'user_id' => $user->id,
                    'type' => 'cash',
                    'title' => 'Espèces LyonTaxis',
                    'subtitle' => 'Paiement à bord',
                    'is_default' => true,
                    'is_selected' => true,
                ]);
            }

            // Create token
            $token = $user->createToken('api_token')->plainTextToken;

            // Clear OTP cache
            cache()->forget('otp_' . $identifier);

            return response()->json([
                'success' => true,
                'message' => 'Authentification réussie',
                'token' => $token,
                'user' => $user->only([
                    'id', 'name', 'email', 'phone_number', 'member_level',
                    'cash_balance', 'integral_points', 'coupons_count',
                ]),
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Erreur lors de la vérification',
            ], 500);
        }
    }

    /**
     * Get current authenticated user
     */
    public function user(Request $request): JsonResponse
    {
        $user = $request->user();
        
        return response()->json([
            'success' => true,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'phone_number' => $user->phone_number,
                'gender' => $user->gender,
                'birthday' => $user->birthday,
                'emergency_contact' => $user->emergency_contact,
                'home_address' => $user->home_address,
                'member_level' => $user->member_level,
                'cash_balance' => (float) $user->cash_balance,
                'integral_points' => $user->integral_points,
                'coupons_count' => $user->coupons_count,
                'referral_code' => $user->referral_code,
                'avatar_seed' => $user->avatar_seed,
            ],
        ]);
    }

    /**
     * Get user profile
     */
    public function profile(Request $request): JsonResponse
    {
        return $this->user($request);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request): JsonResponse
    {
        $user = $request->user();

        $validated = $request->validate([
            'name' => 'sometimes|string|max:255',
            'phone_number' => 'sometimes|string|unique:users,phone_number,' . $user->id,
            'gender' => 'sometimes|string|in:Homme,Femme,Autre',
            'birthday' => 'sometimes|date',
            'emergency_contact' => 'sometimes|string',
            'home_address' => 'sometimes|string',
            'avatar_seed' => 'sometimes|string',
        ]);

        $user->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Profil mis à jour',
            'user' => $user->only([
                'id', 'name', 'email', 'phone_number', 'gender', 'birthday',
                'emergency_contact', 'home_address', 'avatar_seed',
            ]),
        ]);
    }

    /**
     * Get user payment methods
     */
    public function paymentMethods(Request $request): JsonResponse
    {
        $methods = $request->user()->paymentMethods()
            ->get()
            ->map(fn($m) => [
                'id' => $m->id,
                'type' => $m->type,
                'title' => $m->title,
                'subtitle' => $m->subtitle,
                'is_default' => $m->is_default,
                'is_selected' => $m->is_selected,
            ]);

        return response()->json([
            'success' => true,
            'payment_methods' => $methods,
        ]);
    }

    /**
     * Add payment method
     */
    public function addPaymentMethod(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'type' => 'required|in:cash,visa,mastercard,paypal',
            'title' => 'required|string',
            'subtitle' => 'required|string',
        ]);

        $method = $request->user()->paymentMethods()->create($validated);

        return response()->json([
            'success' => true,
            'message' => 'Moyen de paiement ajouté',
            'payment_method' => $method,
        ], 201);
    }

    /**
     * Delete payment method
     */
    public function deletePaymentMethod(Request $request, $id): JsonResponse
    {
        $method = $request->user()->paymentMethods()->findOrFail($id);
        $method->delete();

        return response()->json([
            'success' => true,
            'message' => 'Moyen de paiement supprimé',
        ]);
    }

    /**
     * Get user notifications
     */
    public function notifications(Request $request): JsonResponse
    {
        $notifications = $request->user()->notifications()
            ->latest()
            ->get()
            ->map(fn($n) => [
                'id' => $n->id,
                'type' => $n->type,
                'title' => $n->title,
                'description' => $n->description,
                'is_read' => $n->is_read,
                'created_at' => $n->created_at,
            ]);

        return response()->json([
            'success' => true,
            'notifications' => $notifications,
        ]);
    }

    /**
     * Mark notification as read
     */
    public function markNotificationAsRead(Request $request, $id): JsonResponse
    {
        $notification = $request->user()->notifications()->findOrFail($id);
        $notification->update(['is_read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Notification marquée comme lue',
        ]);
    }

    /**
     * Logout
     */
    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Déconnecté avec succès',
        ]);
    }
}
