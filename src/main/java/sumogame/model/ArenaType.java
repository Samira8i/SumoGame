package sumogame.model;

public enum ArenaType {
    PINK_CIRCLE("Розовая арена", "#FFD1DC"),
    PEACH_CIRCLE("Персиковая арена", "#FFDAB9"),
    RED_CIRCLE("Лавандовая арена", "#E6E6FA");

    private final String name;
    private final String colorHex;

    ArenaType(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
}