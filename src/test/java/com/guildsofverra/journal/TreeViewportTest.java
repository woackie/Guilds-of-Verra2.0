package com.guildsofverra.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class TreeViewportTest {
    private static final double EPSILON = 0.0001;

    @Test
    void clampsPanToContentBounds() {
        TreeViewport.State state = TreeViewport.pan(
            new TreeViewport.State(1.0, 0.0, 0.0),
            -10_000,
            -10_000,
            1_000,
            800,
            400,
            300
        );

        assertEquals(-600.0, state.panX(), EPSILON);
        assertEquals(-500.0, state.panY(), EPSILON);
    }

    @Test
    void zoomKeepsTheAnchorOnTheSameWorldPoint() {
        TreeViewport.State start = new TreeViewport.State(1.0, -200.0, -100.0);
        double anchorX = 240.0;
        double anchorY = 160.0;
        double worldX = TreeViewport.worldX(start, anchorX);
        double worldY = TreeViewport.worldY(start, anchorY);

        TreeViewport.State zoomed = TreeViewport.zoomAround(
            start,
            0.25,
            anchorX,
            anchorY,
            2_000,
            1_200,
            600,
            400
        );

        assertEquals(anchorX, TreeViewport.screenX(zoomed, worldX), EPSILON);
        assertEquals(anchorY, TreeViewport.screenY(zoomed, worldY), EPSILON);
    }

    @Test
    void clampsZoomAndRejectsNonFiniteState() {
        assertEquals(TreeViewport.MIN_ZOOM, TreeViewport.clampZoom(-10), EPSILON);
        assertEquals(TreeViewport.MAX_ZOOM, TreeViewport.clampZoom(10), EPSILON);
        assertThrows(
            IllegalArgumentException.class,
            () -> new TreeViewport.State(Double.NaN, 0, 0)
        );
    }
}
