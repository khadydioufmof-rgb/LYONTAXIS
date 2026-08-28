import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authorization = request.headers.get("Authorization");
    if (!authorization?.startsWith("Bearer ")) {
      return json({ error: "Authentication required" }, 401);
    }

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { global: { headers: { Authorization: authorization } } },
    );
    const token = authorization.replace("Bearer ", "");
    const { data: authData, error: authError } = await supabase.auth.getUser(token);
    if (authError || !authData.user) {
      return json({ error: "Invalid session" }, 401);
    }

    const { rideId } = await request.json();
    if (typeof rideId !== "string" || rideId.length === 0) {
      return json({ error: "rideId is required" }, 400);
    }

    const { data: ride, error: rideError } = await supabase
      .from("rides")
      .select("id, user_id, fare, status")
      .eq("id", rideId)
      .eq("user_id", authData.user.id)
      .single();
    if (rideError || !ride || !["pending", "confirmed"].includes(ride.status)) {
      return json({ error: "Ride cannot be paid" }, 404);
    }

    const stripeBody = new URLSearchParams({
      amount: String(Math.round(Number(ride.fare) * 100)),
      currency: "eur",
      "metadata[ride_id]": ride.id,
      "metadata[user_id]": authData.user.id,
    });
    const stripeResponse = await fetch("https://api.stripe.com/v1/payment_intents", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${Deno.env.get("STRIPE_SECRET_KEY")}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: stripeBody,
    });
    const paymentIntent = await stripeResponse.json();
    if (!stripeResponse.ok) {
      return json({ error: paymentIntent.error?.message ?? "Stripe request failed" }, 502);
    }

    const { error: transactionError } = await supabase.from("payment_transactions").insert({
      user_id: authData.user.id,
      ride_id: ride.id,
      provider: "stripe",
      provider_payment_intent_id: paymentIntent.id,
      amount: Number(ride.fare),
      currency: "eur",
      status: "pending",
    });
    if (transactionError) {
      return json({ error: "Could not record payment transaction" }, 500);
    }

    return json({ clientSecret: paymentIntent.client_secret });
  } catch (_error) {
    return json({ error: "Unexpected payment error" }, 500);
  }
});

function json(body: Record<string, string>, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
