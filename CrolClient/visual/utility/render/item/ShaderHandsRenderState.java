
package crol.client.utility.render.item;

public final class ShaderHandsRenderState {
    private static final ThreadLocal<State> STATE = new ThreadLocal();

    private ShaderHandsRenderState() {
    }

    public static void begin(float redMul, float greenMul, float blueMul, float alphaMul) {
        State current = STATE.get();
        if (current == null) {
            current = new State();
            STATE.set(current);
        }
        ++current.depth;
        current.redMul = redMul;
        current.greenMul = greenMul;
        current.blueMul = blueMul;
        current.alphaMul = alphaMul;
    }

    public static void end() {
        State current = STATE.get();
        if (current == null) {
            return;
        }
        --current.depth;
        if (current.depth <= 0) {
            STATE.remove();
        }
    }

    public static boolean isActive() {
        State current = STATE.get();
        return current != null && current.depth > 0;
    }

    public static float tintRed(float value) {
        State current = STATE.get();
        return current == null ? value : value * current.redMul;
    }

    public static float tintGreen(float value) {
        State current = STATE.get();
        return current == null ? value : value * current.greenMul;
    }

    public static float tintBlue(float value) {
        State current = STATE.get();
        return current == null ? value : value * current.blueMul;
    }

    public static float tintAlpha(float value) {
        State current = STATE.get();
        return current == null ? value : value * current.alphaMul;
    }

    private static final class State {
        private int depth;
        private float redMul;
        private float greenMul;
        private float blueMul;
        private float alphaMul;

        private State() {
        }
    }
}

