
package vurst.visual.base.rotation.deeplearnig;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class FloatArrayInAndOutTranslator
implements Translator<float[], float[]> {
    public NDList processInput(TranslatorContext ctx, float[] input) {
        return new NDList(new NDArray[]{ctx.getNDManager().create(input)});
    }

    public float[] processOutput(TranslatorContext ctx, NDList list) {
        return ((NDArray)list.get(0)).toFloatArray();
    }
}

