package mod.bitsnblocks.notifications;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.bitsnblocks.api.notifications.INotification;
import mod.bitsnblocks.api.notifications.INotificationManager;
import mod.bitsnblocks.client.screens.components.toasts.ChiselsAndBitsNotificationToast;

public final class NotificationManager implements INotificationManager
{
    private static final NotificationManager INSTANCE = new NotificationManager();

    public static NotificationManager getInstance()
    {
        return INSTANCE;
    }

    private NotificationManager()
    {
    }

    @Override
    public void notify(final INotification notification)
    {
        DistExecutor.unsafeRunWhenOn(
          Dist.CLIENT,
          () -> () -> ChiselsAndBitsNotificationToast.notifyOf(notification)
        );
    }
}
