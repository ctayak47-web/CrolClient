package ru.crolclient.api.system.animation.implement;

public enum Easing {
    LINEAR {
        @Override
        public double ease(double t) {
            return t;
        }
    },
    EASE_IN_QUAD {
        @Override
        public double ease(double t) {
            return t * t;
        }
    },
    EASE_OUT_QUAD {
        @Override
        public double ease(double t) {
            return t * (2 - t);
        }
    },
    EASE_IN_OUT_QUAD {
        @Override
        public double ease(double t) {
            return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
        }
    },
    EASE_IN_CUBIC {
        @Override
        public double ease(double t) {
            return t * t * t;
        }
    },
    EASE_OUT_CUBIC {
        @Override
        public double ease(double t) {
            return (--t) * t * t + 1;
        }
    },
    EASE_IN_OUT_CUBIC {
        @Override
        public double ease(double t) {
            return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
        }
    },
    EASE_IN_EXPO {
        @Override
        public double ease(double t) {
            return t == 0 ? 0 : Math.pow(2, 10 * (t - 1));
        }
    },
    EASE_OUT_EXPO {
        @Override
        public double ease(double t) {
            return t == 1 ? 1 : -Math.pow(2, -10 * t) + 1;
        }
    },
    EASE_IN_OUT_EXPO {
        @Override
        public double ease(double t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            if ((t *= 2) < 1) return 0.5 * Math.pow(2, 10 * (t - 1));
            return 0.5 * (-Math.pow(2, -10 * --t) + 2);
        }
    },
    EASE_IN_ELASTIC {
        @Override
        public double ease(double t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return -Math.pow(2, 10 * (t - 1)) * Math.sin((t - 1.075) * (2 * Math.PI) / 0.3);
        }
    },
    EASE_OUT_ELASTIC {
        @Override
        public double ease(double t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return Math.pow(2, -10 * t) * Math.sin((t - 0.075) * (2 * Math.PI) / 0.3) + 1;
        }
    },
    EASE_IN_OUT_ELASTIC {
        @Override
        public double ease(double t) {
            if (t < 0.5) {
                return -0.5 * Math.pow(2, 10 * (t * 2 - 1)) * Math.sin(((t * 2 - 1.075) * (2 * Math.PI)) / 0.3);
            } else {
                return 0.5 * Math.pow(2, -10 * (t * 2 - 1)) * Math.sin(((t * 2 - 1.075) * (2 * Math.PI)) / 0.3) + 1;
            }
        }
    };

    public abstract double ease(double t);
}