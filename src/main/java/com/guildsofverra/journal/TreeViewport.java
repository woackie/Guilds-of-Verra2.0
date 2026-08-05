package com.guildsofverra.journal;

/** Pure viewport mathematics for panning and cursor-anchored zooming. */
public final class TreeViewport {
    public static final double MIN_ZOOM = 0.50;
    public static final double MAX_ZOOM = 1.75;

    private TreeViewport() {}

    public static State initial(int contentWidth, int contentHeight, int viewportWidth, int viewportHeight) {
        return clamp(
            new State(1.0, 0.0, 0.0),
            contentWidth,
            contentHeight,
            viewportWidth,
            viewportHeight
        );
    }

    public static State pan(
        State state,
        double deltaX,
        double deltaY,
        int contentWidth,
        int contentHeight,
        int viewportWidth,
        int viewportHeight
    ) {
        return clamp(
            new State(state.zoom(), state.panX() + deltaX, state.panY() + deltaY),
            contentWidth,
            contentHeight,
            viewportWidth,
            viewportHeight
        );
    }

    public static State zoomAround(
        State state,
        double zoomDelta,
        double anchorX,
        double anchorY,
        int contentWidth,
        int contentHeight,
        int viewportWidth,
        int viewportHeight
    ) {
        double nextZoom = clampZoom(state.zoom() + zoomDelta);
        double worldX = (anchorX - state.panX()) / state.zoom();
        double worldY = (anchorY - state.panY()) / state.zoom();
        State anchored = new State(
            nextZoom,
            anchorX - worldX * nextZoom,
            anchorY - worldY * nextZoom
        );
        return clamp(
            anchored,
            contentWidth,
            contentHeight,
            viewportWidth,
            viewportHeight
        );
    }

    public static double screenX(State state, double worldX) {
        return state.panX() + worldX * state.zoom();
    }

    public static double screenY(State state, double worldY) {
        return state.panY() + worldY * state.zoom();
    }

    public static double worldX(State state, double screenX) {
        return (screenX - state.panX()) / state.zoom();
    }

    public static double worldY(State state, double screenY) {
        return (screenY - state.panY()) / state.zoom();
    }

    public static State clamp(
        State state,
        int contentWidth,
        int contentHeight,
        int viewportWidth,
        int viewportHeight
    ) {
        double zoom = clampZoom(state.zoom());
        double scaledWidth = Math.max(0, contentWidth) * zoom;
        double scaledHeight = Math.max(0, contentHeight) * zoom;
        double minimumX = Math.min(0.0, viewportWidth - scaledWidth);
        double minimumY = Math.min(0.0, viewportHeight - scaledHeight);
        double panX = clamp(state.panX(), minimumX, 0.0);
        double panY = clamp(state.panY(), minimumY, 0.0);
        return new State(zoom, panX, panY);
    }

    public static double clampZoom(double zoom) {
        return clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record State(double zoom, double panX, double panY) {
        public State {
            if (!Double.isFinite(zoom)
                || !Double.isFinite(panX)
                || !Double.isFinite(panY)) {
                throw new IllegalArgumentException("Viewport values must be finite");
            }
        }
    }
}
