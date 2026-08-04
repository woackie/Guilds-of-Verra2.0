package com.guildsofverra.core;

public record RequirementResult(boolean allowed, String reason) {
    public static RequirementResult allow() { return new RequirementResult(true, ""); }
    public static RequirementResult deny(String reason) { return new RequirementResult(false, reason); }
}
