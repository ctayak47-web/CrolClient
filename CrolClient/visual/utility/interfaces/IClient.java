
package crol.client.utility.interfaces;

import crol.client.CrolClient;
import crol.client.base.rotation.AimManager;
import crol.client.base.rotation.RotationManager;
import crol.client.base.rotation.deeplearnig.DeepLearningManager;
import crol.client.utility.interfaces.IWindow;

public interface IClient
extends IWindow {
    public static final CrolClient INSTANCE = CrolClient.getInstance();
    public static final DeepLearningManager deepLearningManager = CrolClient.getInstance().getDeepLearningManager();
    public static final RotationManager rotationManager = CrolClient.getInstance().getRotationManager();
    public static final AimManager aimManager = rotationManager.getAimManager();
}

