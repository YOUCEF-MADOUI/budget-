# Budget++ application-specific R8 rules belong here.
# Keep this file intentionally small until a dependency requires a documented rule.

# Privacy: remove Android logging calls from optimized release builds.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
