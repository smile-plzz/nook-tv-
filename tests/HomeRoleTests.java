package com.ismail.nooktv;

public final class HomeRoleTests {
    private static int assertions;
    private static void check(boolean condition, String description) {
        assertions++;
        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }
    }

    public static void main(String[] args) {
        // Test 1: Alias disabled always produces ALIAS_DISABLED
        check(HomeCoordinator.evaluateStatus(false, false, false, false, false) == HomeCoordinator.Status.ALIAS_DISABLED, "alias disabled legacy");
        check(HomeCoordinator.evaluateStatus(false, true, true, true, false) == HomeCoordinator.Status.ALIAS_DISABLED, "alias disabled overrides role/resolver");

        // Test 2: API 29+ RoleManager matrix
        check(HomeCoordinator.evaluateStatus(true, true, true, true, false) == HomeCoordinator.Status.CONFIRMED_DEFAULT, "role + resolved = confirmed default");
        check(HomeCoordinator.evaluateStatus(true, true, true, false, false) == HomeCoordinator.Status.ROLE_HELD_ONLY, "role held only");
        check(HomeCoordinator.evaluateStatus(true, true, true, false, true) == HomeCoordinator.Status.ROLE_HELD_ONLY, "role held but resolver active");
        check(HomeCoordinator.evaluateStatus(true, true, false, true, false) == HomeCoordinator.Status.RESOLVED_ONLY, "resolved only");
        check(HomeCoordinator.evaluateStatus(true, true, false, false, false) == HomeCoordinator.Status.AVAILABLE_NOT_DEFAULT, "available not default");
        check(HomeCoordinator.evaluateStatus(true, true, false, false, true) == HomeCoordinator.Status.AVAILABLE_NOT_DEFAULT, "available with resolver pending");

        // Test 3: API < 29 (no RoleManager) matrix
        check(HomeCoordinator.evaluateStatus(true, false, false, true, false) == HomeCoordinator.Status.CONFIRMED_DEFAULT, "legacy resolved = confirmed default");
        check(HomeCoordinator.evaluateStatus(true, false, false, false, false) == HomeCoordinator.Status.AVAILABLE_NOT_DEFAULT, "legacy available not default");

        // Test 4: State object guards and helper methods
        HomeCoordinator.State confirmed = new HomeCoordinator.State(
            HomeCoordinator.Status.CONFIRMED_DEFAULT, true, true, true, true, false, "com.ismail.nooktv.compat", "Nook TV", 2);
        check(confirmed.isDefault(), "confirmed isDefault()");
        check(!confirmed.canDisableAlias(), "cannot disable alias while confirmed default");
        check(confirmed.getStatusHeadline().contains("default Home"), "headline describes default");

        HomeCoordinator.State roleHeldOnly = new HomeCoordinator.State(
            HomeCoordinator.Status.ROLE_HELD_ONLY, true, true, true, false, false, "com.google.android.tvlauncher", "Android TV Home", 2);
        check(!roleHeldOnly.isDefault(), "role held only is not confirmed default");
        check(!roleHeldOnly.canDisableAlias(), "cannot disable alias while role held");
        check(roleHeldOnly.getStatusHeadline().contains("differs"), "headline mentions handler difference");

        HomeCoordinator.State resolvedOnly = new HomeCoordinator.State(
            HomeCoordinator.Status.RESOLVED_ONLY, true, true, false, true, false, "com.ismail.nooktv.compat", "Nook TV", 2);
        check(!resolvedOnly.isDefault(), "resolved only is not confirmed default");
        check(!resolvedOnly.canDisableAlias(), "cannot disable alias while resolved default");

        HomeCoordinator.State available = new HomeCoordinator.State(
            HomeCoordinator.Status.AVAILABLE_NOT_DEFAULT, true, true, false, false, false, "com.google.android.tvlauncher", "Android TV Home", 2);
        check(!available.isDefault(), "available is not default");
        check(available.canDisableAlias(), "can disable alias when not default and role not held");
        check(available.getGuidanceMessage().contains("Current default is Android TV Home"), "guidance identifies other launcher");

        HomeCoordinator.State disabled = new HomeCoordinator.State(
            HomeCoordinator.Status.ALIAS_DISABLED, false, true, false, false, false, "", "", 1);
        check(!disabled.isDefault(), "disabled is not default");
        check(disabled.canDisableAlias(), "can disable alias when already disabled");

        System.out.println("PASS: " + assertions + " HomeCoordinator role and state assertions");
    }
}
