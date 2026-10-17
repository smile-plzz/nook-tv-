package com.ismail.nooktv;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates Android TV Home launcher role selection, fallback navigation,
 * multi-signal status verification, and safe recovery.
 */
public final class HomeCoordinator {
    public static final String HOME_ALIAS_CLASS = "com.ismail.nooktv.HomeEntry";
    public static final int REQUEST_CODE_ROLE_HOME = 0x484F; // "HO"

    public enum Status {
        /** Confirmed default: Role held (if supported) and HOME intent resolves to Nook. */
        CONFIRMED_DEFAULT,
        /** RoleManager reports role held, but default activity resolves elsewhere or to system resolver. */
        ROLE_HELD_ONLY,
        /** HOME intent resolves to Nook, but RoleManager reports role not held (on API 29+). */
        RESOLVED_ONLY,
        /** HomeEntry alias is enabled, but another launcher or the system resolver is default. */
        AVAILABLE_NOT_DEFAULT,
        /** HomeEntry alias is disabled; Nook cannot be chosen by the system as Home. */
        ALIAS_DISABLED
    }

    public static final class State {
        public final Status status;
        public final boolean aliasEnabled;
        public final boolean roleSupported;
        public final boolean roleHeld;
        public final boolean defaultResolved;
        public final boolean isResolver;
        public final String currentHandlerPackage;
        public final String currentHandlerLabel;
        public final int homeAppCount;

        public State(Status status, boolean aliasEnabled, boolean roleSupported, boolean roleHeld,
                     boolean defaultResolved, boolean isResolver,
                     String currentHandlerPackage, String currentHandlerLabel, int homeAppCount) {
            this.status = status;
            this.aliasEnabled = aliasEnabled;
            this.roleSupported = roleSupported;
            this.roleHeld = roleHeld;
            this.defaultResolved = defaultResolved;
            this.isResolver = isResolver;
            this.currentHandlerPackage = currentHandlerPackage;
            this.currentHandlerLabel = currentHandlerLabel;
            this.homeAppCount = homeAppCount;
        }

        public boolean isDefault() {
            return status == Status.CONFIRMED_DEFAULT;
        }

        public boolean canDisableAlias() {
            // Guard: Nook cannot disable its Home alias while it is the active default or holds the role.
            return !isDefault() && !roleHeld && !defaultResolved;
        }

        public String getStatusHeadline() {
            switch (status) {
                case CONFIRMED_DEFAULT:
                    return "Nook TV is your default Home app.";
                case ROLE_HELD_ONLY:
                    return "Home role granted, but system handler differs.";
                case RESOLVED_ONLY:
                    return "Nook resolves as default, but Home role is not recorded.";
                case AVAILABLE_NOT_DEFAULT:
                    if (isResolver) {
                        return "Home selection is pending in the system.";
                    }
                    return "Nook is available for Home selection.";
                case ALIAS_DISABLED:
                default:
                    return "Nook Home option is currently disabled.";
            }
        }

        public String getGuidanceMessage() {
            switch (status) {
                case CONFIRMED_DEFAULT:
                    return "Pressing Home on your TV remote will return here. Your original launcher remains installed and safe.";
                case ROLE_HELD_ONLY:
                    return "The system granted Nook the Home role, but " + currentHandlerLabel + " is currently handling Home. Press Home on your remote to test.";
                case RESOLVED_ONLY:
                    return "Nook is currently answering Home intents. You can re-verify or request the official Home role if needed.";
                case AVAILABLE_NOT_DEFAULT:
                    if (isResolver) {
                        return "No single launcher is set as default. Press Home on your remote and choose Nook > 'Always', or open Home settings.";
                    }
                    return "Current default is " + currentHandlerLabel + " (" + currentHandlerPackage + "). You can switch to Nook below or in TV settings.";
                case ALIAS_DISABLED:
                default:
                    return "Enable Nook Home to let your TV system discover Nook as a launcher candidate.";
            }
        }
    }

    public static ComponentName getHomeAlias(Context context) {
        return new ComponentName(context, HOME_ALIAS_CLASS);
    }

    public static boolean isAliasEnabled(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.getComponentEnabledSetting(getHomeAlias(context)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void setAliasEnabled(Context context, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        int flag = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(getHomeAlias(context), flag, PackageManager.DONT_KILL_APP);
    }

    public static boolean isRoleSupported(Context context) {
        if (Build.VERSION.SDK_INT < 29) { // Build.VERSION_CODES.Q
            return false;
        }
        try {
            RoleManager rm = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            return rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isRoleHeld(Context context) {
        if (Build.VERSION.SDK_INT < 29) {
            return false;
        }
        try {
            RoleManager rm = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            return rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && rm.isRoleHeld(RoleManager.ROLE_HOME);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Pure status evaluation matrix used by both Android runtime and JVM test suite.
     */
    public static Status evaluateStatus(boolean aliasEnabled, boolean roleSupported, boolean roleHeld,
                                        boolean defaultResolved, boolean isResolver) {
        if (!aliasEnabled) {
            return Status.ALIAS_DISABLED;
        }
        if (roleSupported) {
            if (roleHeld && defaultResolved) {
                return Status.CONFIRMED_DEFAULT;
            }
            if (roleHeld && !defaultResolved) {
                return Status.ROLE_HELD_ONLY;
            }
            if (!roleHeld && defaultResolved) {
                return Status.RESOLVED_ONLY;
            }
            return Status.AVAILABLE_NOT_DEFAULT;
        } else {
            if (defaultResolved) {
                return Status.CONFIRMED_DEFAULT;
            }
            return Status.AVAILABLE_NOT_DEFAULT;
        }
    }

    public static State inspect(Context context) {
        boolean aliasEnabled = isAliasEnabled(context);
        boolean roleSupported = isRoleSupported(context);
        boolean roleHeld = isRoleHeld(context);

        PackageManager pm = context.getPackageManager();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolved = pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean defaultResolved = false;
        boolean isResolver = false;
        String handlerPkg = "";
        String handlerLabel = "Unknown";

        if (resolved != null && resolved.activityInfo != null) {
            handlerPkg = resolved.activityInfo.packageName;
            CharSequence label = resolved.loadLabel(pm);
            handlerLabel = label != null ? label.toString() : handlerPkg;

            if (context.getPackageName().equals(handlerPkg)) {
                defaultResolved = true;
            } else if ("android".equals(handlerPkg) || handlerPkg.contains("resolver")) {
                isResolver = true;
                handlerLabel = "System Resolver (No default set)";
            }
        }

        List<ResolveInfo> allHomes = pm.queryIntentActivities(homeIntent, 0);
        int homeCount = allHomes != null ? allHomes.size() : 0;

        Status status = evaluateStatus(aliasEnabled, roleSupported, roleHeld, defaultResolved, isResolver);
        return new State(status, aliasEnabled, roleSupported, roleHeld, defaultResolved, isResolver, handlerPkg, handlerLabel, homeCount);
    }

    /**
     * Attempts to initiate Home role selection.
     * Returns true if a system role prompt was successfully launched; false if fallback is required.
     */
    public static boolean requestHomeRole(Activity activity, int requestCode) {
        if (activity == null) return false;
        // Step 1: Ensure alias is enabled so Nook is a valid Home candidate
        if (!isAliasEnabled(activity)) {
            setAliasEnabled(activity, true);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                RoleManager rm = (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                        // Already held
                        return false;
                    }
                    Intent roleIntent = rm.createRequestRoleIntent(RoleManager.ROLE_HOME);
                    activity.startActivityForResult(roleIntent, requestCode);
                    return true;
                }
            } catch (ActivityNotFoundException | SecurityException ignored) {
                // Firmware lacks role dialog activity; fall through to settings
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Opens system Home settings with multi-tier fallback.
     * Returns true if an intent was started, false if none could be resolved.
     */
    public static boolean openHomeSettingsWithFallback(Context context) {
        // Step 1: Ensure alias is enabled
        if (!isAliasEnabled(context)) {
            setAliasEnabled(context, true);
        }

        // Try Settings.ACTION_HOME_SETTINGS
        try {
            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Throwable ignored) {}

        // Try Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
        try {
            Intent intent = new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS");
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Throwable ignored) {}

        // Try Settings.ACTION_SETTINGS
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }
}
