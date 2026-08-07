
package vurst.visual.base.rotation.deeplearnig;

import vurst.visual.base.rotation.deeplearnig.FloatArrayInAndOutTranslator;
import vurst.visual.base.rotation.deeplearnig.ModelWrapper;

public class MinaraiModel
extends ModelWrapper<float[], float[]> {
    public MinaraiModel(String name) {
        super(name, new FloatArrayInAndOutTranslator(), 2L);
    }
}

