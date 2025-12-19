package sumogame.model;

public enum ArenaType {
    PINK_CIRCLE("Розовая арена", "#FFC0CB",
            "Круглая арена розового цвета"),

    PEACH_CIRCLE("Персиковая арена", "#FFDAB9",
            "Круглая арена персикового цвета"),

    RED_CIRCLE("Красная арена", "#FF0000",
            "Круглая арена красного цвета");

    private final String name;
    private final String colorHex;
    private final String description;

    ArenaType(String name, String colorHex, String description) {
        this.name = name;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public String getDescription() { return description; }

    public static ArenaType getByRoundNumber(int roundNumber) {
        ArenaType[] arenas = values();
        int index = (roundNumber - 1) % arenas.length;
        return arenas[index];
    }
}