package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class XpCurveTest {
    @Test void exactTotalMatchesBlueprint() { assertEquals(561950L, XpCurve.totalXpToMax()); }
    @Test void curveIsStrictlyIncreasing() { for (int i=1;i<100;i++) assertTrue(XpCurve.xpToNextLevel(i) > XpCurve.xpToNextLevel(i-1)); }
}
