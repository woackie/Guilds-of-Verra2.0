package com.guildsofverra.event;

public enum WorldEventType {
    BLOOD_MOON(
        "blood_moon",
        "Blood Moon",
        "The moon begins to glow crimson. Shelter before night fully takes hold.",
        "The Blood Moon rises. Hostile creatures are empowered until dawn is allowed to return.",
        "The crimson light fades and the night begins to loosen its grip."
    ),
    SEVERE_THUNDERSTORM(
        "severe_thunderstorm",
        "Severe Thunderstorm",
        "The air grows heavy and distant thunder rolls across the world.",
        "A severe thunderstorm breaks overhead. Open ground is no longer safe.",
        "The violent storm weakens and the skies begin to clear."
    ),
    CAVE_TREMOR(
        "cave_tremor",
        "Cave Tremor",
        "Stone groans deep beneath the surface. The caves are becoming unstable.",
        "A cave tremor begins. Mining slows and disturbed creatures emerge underground.",
        "The underground rumbling settles."
    ),
    NETHER_SURGE(
        "nether_surge",
        "Nether Surge",
        "Heat and hostile energy gather beyond the portals.",
        "A Nether Surge begins. The dimension fills with empowered and newly arriving threats.",
        "The Nether's violent surge recedes."
    ),
    PREDATOR_MIGRATION(
        "predator_migration",
        "Predator Migration",
        "Distant movement passes through the wilderness. Predators are on the move.",
        "A predator migration crosses the Overworld. Packs will pass through occupied regions.",
        "The migrating predators move beyond the settled lands."
    ),
    LONG_NIGHT(
        "long_night",
        "The Long Night",
        "Sunlight dims far too early. This night will not pass quickly.",
        "The Long Night begins. Dawn cannot arrive until the event ends.",
        "The horizon finally brightens as the Long Night ends."
    ),
    RESTLESS_DEAD(
        "restless_dead",
        "Restless Dead",
        "The ground feels cold and recently fallen undead begin to stir.",
        "The Restless Dead rise. Slain undead may return once more.",
        "The dead fall still again."
    );

    private final String id;
    private final String displayName;
    private final String warningMessage;
    private final String startMessage;
    private final String endMessage;

    WorldEventType(
        String id,
        String displayName,
        String warningMessage,
        String startMessage,
        String endMessage
    ) {
        this.id = id;
        this.displayName = displayName;
        this.warningMessage = warningMessage;
        this.startMessage = startMessage;
        this.endMessage = endMessage;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String warningMessage() {
        return warningMessage;
    }

    public String startMessage() {
        return startMessage;
    }

    public String endMessage() {
        return endMessage;
    }

    public static WorldEventType byId(String id) {
        if (id == null) {
            return null;
        }
        for (WorldEventType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
