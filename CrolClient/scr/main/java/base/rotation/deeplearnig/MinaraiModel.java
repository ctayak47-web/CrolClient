
package crol.client.base.rotation.deeplearnig;

import crol.client.base.rotation.deeplearnig.FloatArrayInAndOutTranslator;
import crol.client.base.rotation.deeplearnig.ModelWrapper;

public class MinaraiModel
extends ModelWrapper<float[], float[]> {
    public MinaraiModel(String name) {
        super(name, new FloatArrayInAndOutTranslator(), 2L);
    }
}

