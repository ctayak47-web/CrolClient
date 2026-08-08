
package crol.client.base.rotation.deeplearnig;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.initializer.Initializer;
import ai.djl.training.initializer.XavierInitializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.listener.TrainingListenerAdapter;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.ParameterTracker;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import net.minecraft.Identifier;
import crol.client.CrolClient;

public abstract class ModelWrapper<I, O>
implements Closeable {
    private static final int NUM_EPOCH = 500;
    private static final int BATCH_SIZE = 32;
    private final Translator<I, O> translator;
    private final long outputs;
    private final Model model;
    private final Predictor<I, O> predictor;
    private final String name;

    public ModelWrapper(String name, Translator<I, O> translator, long outputs) {
        this.name = name;
        this.translator = translator;
        this.outputs = outputs;
        this.model = Model.newInstance((String)name);
        this.model.setBlock((Block)ModelWrapper.createMlpBlock(outputs));
        this.predictor = this.model.newPredictor(translator);
    }

    public O predict(I input) throws TranslateException {
        return (O)this.predictor.predict(input);
    }

    public void train(float[][] features, float[][] labels) throws ModelException, IOException {
        if (features.length != labels.length || features.length == 0) {
            throw new IllegalArgumentException("Features and labels must have the same size and be non-empty");
        }
        long inputs = features[0].length;
        DefaultTrainingConfig trainingConfig = new DefaultTrainingConfig((Loss)Loss.l2Loss()).optInitializer((Initializer)new XavierInitializer(), "weight").optOptimizer((Optimizer)Adam.builder().optLearningRateTracker((ParameterTracker)Tracker.fixed((float)0.001f)).build()).addTrainingListeners(TrainingListener.Defaults.logging((String)"train")).addTrainingListeners(new TrainingListener[]{new TrainingListenerAdapter(this){

            public void onEpoch(Trainer trainer) {
                System.out.println("Epoch " + trainer.getTrainingResult().getEpoch() + " - Loss: " + trainer.getTrainingResult().getTrainLoss());
            }

            public void onTrainingBegin(Trainer trainer) {
            }

            public void onTrainingEnd(Trainer trainer) {
            }
        }});
        try (Trainer trainer = this.model.newTrainer((TrainingConfig)trainingConfig);
             NDManager manager = NDManager.newBaseManager();){
            ArrayDataset trainingSet = ((ArrayDataset.Builder)new ArrayDataset.Builder().setData(new NDArray[]{manager.create(features)}).optLabels(new NDArray[]{manager.create(labels)}).setSampling(32, true)).build();
            trainer.initialize(new Shape[]{new Shape(new long[]{32L, inputs})});
            EasyTrain.fit((Trainer)trainer, (int)500, (Dataset)trainingSet, null);
        }
        catch (TranslateException translateException) {
            
        }
    }

    public void load(InputStream stream) throws IOException, ModelException {
        this.model.load(stream);
    }

    public void load(Path path) throws IOException, ModelException {
        this.model.load(path, "tf");
    }

    public void load() {
        Identifier resourceId = CrolClient.id("models/" + this.name + ".params");
        String resourcePath = "/assets/" + resourceId.getNamespace() + "/" + resourceId.getPath();
        try (InputStream stream = this.getClass().getResourceAsStream(resourcePath);){
            if (stream == null) {
                throw new IOException("Unable to locate resource " + resourcePath);
            }
            this.load(stream);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load model from " + resourcePath, e);
        }
    }

    public void save(Path path) throws IOException, ModelException {
        this.model.save(path, "tf");
    }

    @Override
    public void close() {
        this.predictor.close();
        this.model.close();
    }

    private static SequentialBlock createMlpBlock(long outputs) {
        return new SequentialBlock().add((Block)Linear.builder().setUnits(128L).build()).add(Blocks.batchFlattenBlock()).add((Block)BatchNorm.builder().build()).add(Activation.reluBlock()).add((Block)Linear.builder().setUnits(64L).build()).add(Blocks.batchFlattenBlock()).add((Block)BatchNorm.builder().build()).add(Activation.reluBlock()).add((Block)Linear.builder().setUnits(32L).build()).add(Blocks.batchFlattenBlock()).add((Block)BatchNorm.builder().build()).add(Activation.reluBlock()).add((Block)Linear.builder().setUnits(outputs).build());
    }
}

