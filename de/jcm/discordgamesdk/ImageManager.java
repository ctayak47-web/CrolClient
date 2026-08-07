
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.image.ImageDimensions;
import de.jcm.discordgamesdk.image.ImageHandle;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.commands.GetImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.imageio.ImageIO;

public class ImageManager {
    private final Core.CorePrivate core;
    private final Map<ImageHandle, BufferedImage> imageCache = new HashMap<ImageHandle, BufferedImage>();

    ImageManager(Core.CorePrivate core) {
        this.core = core;
    }

    public void fetch(ImageHandle handle, boolean refresh, BiConsumer<Result, ImageHandle> callback) {
        if (!refresh && this.imageCache.containsKey(handle)) {
            callback.accept(Result.OK, handle);
        } else {
            this.core.sendCommand(Command.Type.GET_IMAGE, new GetImage.Args(handle), c -> {
                Result r = this.core.checkError((Command)c);
                if (r != Result.OK) {
                    callback.accept(r, null);
                    return;
                }
                try {
                    GetImage.Response response = this.core.getGson().fromJson(c.getData(), GetImage.Response.class);
                    byte[] data = response.getData();
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                    this.imageCache.put(handle, img);
                    callback.accept(r, handle);
                }
                catch (IOException e) {
                    this.core.log(LogLevel.ERROR, e.toString());
                    callback.accept(Result.INTERNAL_ERROR, handle);
                }
            });
        }
    }

    public ImageDimensions getDimensions(ImageHandle handle) {
        if (!this.imageCache.containsKey(handle)) {
            throw new GameSDKException(Result.NOT_FETCHED);
        }
        BufferedImage img = this.imageCache.get(handle);
        return new ImageDimensions(img.getWidth(), img.getHeight());
    }

    public byte[] getData(ImageHandle handle, ImageDimensions dimensions) {
        return this.getData(handle, dimensions.getWidth() * dimensions.getHeight() * 4);
    }

    public byte[] getData(ImageHandle handle, int length) {
        if (!this.imageCache.containsKey(handle)) {
            throw new GameSDKException(Result.NOT_FETCHED);
        }
        BufferedImage img = this.imageCache.get(handle);
        byte[] data = new byte[length];
        return (byte[])img.getRaster().getDataElements(0, 0, data);
    }

    @Deprecated
    public BufferedImage getAsBufferedImage(ImageHandle handle, ImageDimensions dimensions) {
        return this.getAsBufferedImage(handle);
    }

    public BufferedImage getAsBufferedImage(ImageHandle handle) {
        if (!this.imageCache.containsKey(handle)) {
            throw new GameSDKException(Result.NOT_FETCHED);
        }
        return this.imageCache.get(handle);
    }
}

