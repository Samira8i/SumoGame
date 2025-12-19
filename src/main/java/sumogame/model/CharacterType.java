package sumogame.model;

public enum CharacterType {
    PINK("Розовый", "#FFC0CB", "Ускорение",
            "Увеличивает скорость на 50% на 3 секунды",
            3.0, 1.5, 1.0, 1.0),

    GREEN("Зеленый", "#90EE90", "Увеличение",
            "Увеличивает размер на 60% на 4 секунды",
            4.0, 1.0, 1.6, 1.0),

    BLUE("Синий", "#ADD8E6", "Сила",
            "Увеличивает силу на 80% на 3 секунды",
            3.0, 1.0, 1.0, 1.8);

    private final String name;
    private final String colorHex;
    private final String abilityName;
    private final String abilityDescription;
    private final double abilityDuration;
    private final double speedMultiplier;
    private final double sizeMultiplier;
    private final double strengthMultiplier;

    CharacterType(String name, String colorHex, String abilityName,
                  String abilityDescription, double abilityDuration,
                  double speedMultiplier, double sizeMultiplier, double strengthMultiplier) {
        this.name = name;
        this.colorHex = colorHex;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.abilityDuration = abilityDuration;
        this.speedMultiplier = speedMultiplier;
        this.sizeMultiplier = sizeMultiplier;
        this.strengthMultiplier = strengthMultiplier;
    }

    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public String getAbilityName() { return abilityName; }
    public String getAbilityDescription() { return abilityDescription; }
    public double getAbilityDuration() { return abilityDuration; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getSizeMultiplier() { return sizeMultiplier; }
    public double getStrengthMultiplier() { return strengthMultiplier; }
}