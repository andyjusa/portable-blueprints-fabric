package nimble.portable_blueprints.mod_compatibility;

/** The upstream integrations depend on NeoForge APIs and are unavailable in this Fabric port. */
public final class ModCompatibilityInit {
    public static final boolean isProjectELoaded = false;
    public static final boolean isSophisticatedBackpacksLoaded = false;
    public static void init() {}
}
