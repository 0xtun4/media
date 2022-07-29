/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.session;

import static androidx.media3.common.util.Assertions.checkNotNull;
import static java.lang.annotation.ElementType.TYPE_USE;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.app.BundleCompat;
import androidx.media3.common.Bundleable;
import androidx.media3.common.Player;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by {@link MediaSession} to send its state to the {@link MediaController} when the
 * connection request is accepted.
 */
/* package */ class ConnectionState implements Bundleable {

  public final int libraryVersion;

  public final int sessionInterfaceVersion;

  public final IMediaSession sessionBinder;

  @Nullable public final PendingIntent sessionActivity;

  public final SessionCommands sessionCommands;

  public final Player.Commands playerCommandsFromSession;

  public final Player.Commands playerCommandsFromPlayer;

  public final Bundle tokenExtras;

  public final PlayerInfo playerInfo;

  public ConnectionState(
      int libraryVersion,
      int sessionInterfaceVersion,
      IMediaSession sessionBinder,
      @Nullable PendingIntent sessionActivity,
      SessionCommands sessionCommands,
      Player.Commands playerCommandsFromSession,
      Player.Commands playerCommandsFromPlayer,
      Bundle tokenExtras,
      PlayerInfo playerInfo) {
    this.libraryVersion = libraryVersion;
    this.sessionInterfaceVersion = sessionInterfaceVersion;
    this.sessionBinder = sessionBinder;
    this.sessionCommands = sessionCommands;
    this.playerCommandsFromSession = playerCommandsFromSession;
    this.playerCommandsFromPlayer = playerCommandsFromPlayer;
    this.sessionActivity = sessionActivity;
    this.tokenExtras = tokenExtras;
    this.playerInfo = playerInfo;
  }

  // Bundleable implementation.

  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @Target(TYPE_USE)
  @IntDef({
    FIELD_LIBRARY_VERSION,
    FIELD_SESSION_BINDER,
    FIELD_SESSION_ACTIVITY,
    FIELD_SESSION_COMMANDS,
    FIELD_PLAYER_COMMANDS_FROM_SESSION,
    FIELD_PLAYER_COMMANDS_FROM_PLAYER,
    FIELD_TOKEN_EXTRAS,
    FIELD_PLAYER_INFO,
    FIELD_SESSION_INTERFACE_VERSION,
  })
  private @interface FieldNumber {}

  private static final int FIELD_LIBRARY_VERSION = 0;
  private static final int FIELD_SESSION_BINDER = 1;
  private static final int FIELD_SESSION_ACTIVITY = 2;
  private static final int FIELD_SESSION_COMMANDS = 3;
  private static final int FIELD_PLAYER_COMMANDS_FROM_SESSION = 4;
  private static final int FIELD_PLAYER_COMMANDS_FROM_PLAYER = 5;
  private static final int FIELD_TOKEN_EXTRAS = 6;
  private static final int FIELD_PLAYER_INFO = 7;
  private static final int FIELD_SESSION_INTERFACE_VERSION = 8;
  // Next field key = 9

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putInt(keyForField(FIELD_LIBRARY_VERSION), libraryVersion);
    BundleCompat.putBinder(bundle, keyForField(FIELD_SESSION_BINDER), sessionBinder.asBinder());
    bundle.putParcelable(keyForField(FIELD_SESSION_ACTIVITY), sessionActivity);
    bundle.putBundle(keyForField(FIELD_SESSION_COMMANDS), sessionCommands.toBundle());
    bundle.putBundle(
        keyForField(FIELD_PLAYER_COMMANDS_FROM_SESSION), playerCommandsFromSession.toBundle());
    bundle.putBundle(
        keyForField(FIELD_PLAYER_COMMANDS_FROM_PLAYER), playerCommandsFromPlayer.toBundle());
    bundle.putBundle(keyForField(FIELD_TOKEN_EXTRAS), tokenExtras);
    bundle.putBundle(
        keyForField(FIELD_PLAYER_INFO),
        playerInfo.toBundle(
            /* excludeMediaItems= */ !playerCommandsFromPlayer.contains(Player.COMMAND_GET_TIMELINE)
                || !playerCommandsFromSession.contains(Player.COMMAND_GET_TIMELINE),
            /* excludeMediaItemsMetadata= */ !playerCommandsFromPlayer.contains(
                    Player.COMMAND_GET_MEDIA_ITEMS_METADATA)
                || !playerCommandsFromSession.contains(Player.COMMAND_GET_MEDIA_ITEMS_METADATA),
            /* excludeCues= */ !playerCommandsFromPlayer.contains(Player.COMMAND_GET_TEXT)
                || !playerCommandsFromSession.contains(Player.COMMAND_GET_TEXT),
            /* excludeTimeline= */ false,
            /* excludeTracks= */ !playerCommandsFromPlayer.contains(Player.COMMAND_GET_TRACKS)
                || !playerCommandsFromSession.contains(Player.COMMAND_GET_TRACKS)));
    bundle.putInt(keyForField(FIELD_SESSION_INTERFACE_VERSION), sessionInterfaceVersion);
    return bundle;
  }

  /** Object that can restore a {@link ConnectionState} from a {@link Bundle}. */
  public static final Creator<ConnectionState> CREATOR = ConnectionState::fromBundle;

  private static ConnectionState fromBundle(Bundle bundle) {
    int libraryVersion = bundle.getInt(keyForField(FIELD_LIBRARY_VERSION), /* defaultValue= */ 0);
    int sessionInterfaceVersion =
        bundle.getInt(keyForField(FIELD_SESSION_INTERFACE_VERSION), /* defaultValue= */ 0);
    IBinder sessionBinder =
        checkNotNull(BundleCompat.getBinder(bundle, keyForField(FIELD_SESSION_BINDER)));
    @Nullable
    PendingIntent sessionActivity = bundle.getParcelable(keyForField(FIELD_SESSION_ACTIVITY));
    @Nullable Bundle sessionCommandsBundle = bundle.getBundle(keyForField(FIELD_SESSION_COMMANDS));
    SessionCommands sessionCommands =
        sessionCommandsBundle == null
            ? SessionCommands.EMPTY
            : SessionCommands.CREATOR.fromBundle(sessionCommandsBundle);
    @Nullable
    Bundle playerCommandsFromPlayerBundle =
        bundle.getBundle(keyForField(FIELD_PLAYER_COMMANDS_FROM_PLAYER));
    Player.Commands playerCommandsFromPlayer =
        playerCommandsFromPlayerBundle == null
            ? Player.Commands.EMPTY
            : Player.Commands.CREATOR.fromBundle(playerCommandsFromPlayerBundle);
    @Nullable
    Bundle playerCommandsFromSessionBundle =
        bundle.getBundle(keyForField(FIELD_PLAYER_COMMANDS_FROM_SESSION));
    Player.Commands playerCommandsFromSession =
        playerCommandsFromSessionBundle == null
            ? Player.Commands.EMPTY
            : Player.Commands.CREATOR.fromBundle(playerCommandsFromSessionBundle);
    @Nullable Bundle tokenExtras = bundle.getBundle(keyForField(FIELD_TOKEN_EXTRAS));
    @Nullable Bundle playerInfoBundle = bundle.getBundle(keyForField(FIELD_PLAYER_INFO));
    PlayerInfo playerInfo =
        playerInfoBundle == null
            ? PlayerInfo.DEFAULT
            : PlayerInfo.CREATOR.fromBundle(playerInfoBundle);
    return new ConnectionState(
        libraryVersion,
        sessionInterfaceVersion,
        IMediaSession.Stub.asInterface(sessionBinder),
        sessionActivity,
        sessionCommands,
        playerCommandsFromSession,
        playerCommandsFromPlayer,
        tokenExtras == null ? Bundle.EMPTY : tokenExtras,
        playerInfo);
  }

  private static String keyForField(@FieldNumber int field) {
    return Integer.toString(field, Character.MAX_RADIX);
  }
}
