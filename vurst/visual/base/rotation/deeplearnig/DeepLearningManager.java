
package vurst.visual.base.rotation.deeplearnig;

import vurst.visual.base.rotation.deeplearnig.MinaraiModel;

public class DeepLearningManager {
    private MinaraiModel model;
    private MinaraiModel speedModer;
    private MinaraiModel slowModel;

    private MinaraiModel loadModel(String name) {
        try {
            MinaraiModel model = new MinaraiModel(name);
            model.load();
            return model;
        }
        catch (Exception e) {
            System.err.println("Failed to load deep learning model: " + name);
            e.printStackTrace();
            return null;
        }
    }

    public synchronized MinaraiModel getModel() {
        if (this.model == null) {
            this.model = this.loadModel("model");
        }
        return this.model;
    }

    public synchronized MinaraiModel getSpeedModer() {
        if (this.speedModer == null) {
            this.speedModer = this.loadModel("tf-0100");
        }
        return this.speedModer;
    }

    public synchronized MinaraiModel getSlowModel() {
        if (this.slowModel == null) {
            this.slowModel = this.loadModel("slow");
        }
        return this.slowModel;
    }
}

