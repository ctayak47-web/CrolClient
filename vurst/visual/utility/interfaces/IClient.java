
package vurst.visual.utility.interfaces;

import vurst.visual.VurstVisual;
import vurst.visual.base.rotation.AimManager;
import vurst.visual.base.rotation.RotationManager;
import vurst.visual.base.rotation.deeplearnig.DeepLearningManager;
import vurst.visual.utility.interfaces.IWindow;

public interface IClient
extends IWindow {
    public static final VurstVisual INSTANCE = VurstVisual.getInstance();
    public static final DeepLearningManager deepLearningManager = VurstVisual.getInstance().getDeepLearningManager();
    public static final RotationManager rotationManager = VurstVisual.getInstance().getRotationManager();
    public static final AimManager aimManager = rotationManager.getAimManager();
}

