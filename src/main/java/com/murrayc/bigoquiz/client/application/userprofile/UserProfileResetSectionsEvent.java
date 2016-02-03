package com.murrayc.bigoquiz.client.application.userprofile;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by murrayc on 1/25/16.
 */
public class UserProfileResetSectionsEvent extends GwtEvent<UserProfileResetSectionsEvent.UserProfileResetSectionsEventHandler> {

    public UserProfileResetSectionsEvent() {
    }

    public interface UserProfileResetSectionsEventHandler extends EventHandler {
        void onUserProfileResetSections(UserProfileResetSectionsEvent event);
    }

    public static final Type<UserProfileResetSectionsEventHandler> TYPE = new Type<>();

    public static void fire(final HasHandlers source) {
        if (TYPE != null) {
            source.fireEvent(new UserProfileResetSectionsEvent());
        }
    }

    /*
    public static Type<UserProfileResetSectionsEventHandler> getType() {
        return TYPE;
    }
    */

    @Override
    public Type<UserProfileResetSectionsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final UserProfileResetSectionsEventHandler handler) {
        handler.onUserProfileResetSections(this);
    }
}
