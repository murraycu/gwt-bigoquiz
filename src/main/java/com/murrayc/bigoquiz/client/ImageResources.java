package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/28/16.
 */
public interface ImageResources extends ClientBundle {
    ImageResources INSTANCE = GWT.create(ImageResources.class);

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/images/btn_google_signin_light_normal_web.png")
    ImageResource getGoogleSignInNormal();

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/images/btn_google_signin_light_focus_web.png")
    ImageResource getGoogleSignInFocus();

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/images/btn_google_signin_light_pressed_web.png")
    ImageResource getGoogleSignInPressed();
}
