package com.ismail.nooktv;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/** Session secrets never enter dashboard data, exports, or diagnostics. */
public final class Pairing {
    private final SecureRandom random = new SecureRandom();
    private final LongSupplier clock;
    private String code = "", ticket = "", pendingName = "";
    private long expires;
    private int failures;
    private boolean approved;
    private final Map<String, String> sessions = new HashMap<>();
    public Pairing() { this(System::currentTimeMillis); }
    Pairing(LongSupplier clock) { this.clock = clock; }
    public synchronized String begin() {
        cancel(); failures = 0;
        code = String.format(java.util.Locale.ROOT, "%06d", random.nextInt(1000000));
        expires = clock.getAsLong() + 120000;
        return code;
    }
    public synchronized String request(String submitted, String name) {
        if (clock.getAsLong() >= expires || code.isEmpty() || failures >= 5) return null;
        if (!code.equals(submitted)) { failures++; return null; }
        if (!ticket.isEmpty()) return null;
        ticket = secret(); pendingName = name; approved = false;
        return ticket;
    }
    public synchronized String pendingName() { return pendingName; }
    public synchronized void approve() { if (clock.getAsLong() < expires && !ticket.isEmpty()) approved = true; }
    public synchronized String[] claim(String submitted) {
        if (clock.getAsLong() >= expires || !ticket.equals(submitted) || ticket.isEmpty() || !approved) return null;
        if (sessions.size() >= 8) return null;
        String token = secret(), csrf = secret(); sessions.put(token, csrf); cancel();
        return new String[]{token, csrf};
    }
    public synchronized boolean authorized(String token, String csrf) {
        return token != null && csrf != null && csrf.equals(sessions.get(token));
    }
    public synchronized void revoke() { sessions.clear(); cancel(); }
    public synchronized void cancel() { code = ""; ticket = ""; pendingName = ""; expires = 0; approved = false; }
    private String secret() { byte[] bytes = new byte[32]; random.nextBytes(bytes); StringBuilder s = new StringBuilder(); for (byte b : bytes) s.append(String.format(java.util.Locale.ROOT, "%02x", b & 255)); return s.toString(); }
}
