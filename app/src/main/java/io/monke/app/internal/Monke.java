package io.monke.app.internal;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.fabric.sdk.android.Fabric;
import io.monke.app.BuildConfig;
import io.monke.app.internal.di.DaggerMonkeComponent;
import io.monke.app.internal.di.HelpersModule;
import io.monke.app.internal.di.MonkeComponent;
import io.monke.app.internal.di.MonkeModule;
import io.monke.app.internal.di.RepoModule;
import io.monke.app.internal.mvp.ErrorView;
import io.monke.app.internal.mvp.ProgressView;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import network.minter.core.internal.exceptions.NetworkException;
import timber.log.Timber;

import static io.monke.app.internal.common.Preconditions.firstNonNull;

public class Monke extends Application implements HasAndroidInjector {

    public static final Locale LC_EN = Locale.US;
    @SuppressWarnings("ConstantConditions")
    public final static boolean ENABLE_CRASHLYTICS = BuildConfig.FLAVOR.equalsIgnoreCase("netTest") || BuildConfig.FLAVOR.equalsIgnoreCase("netMain");
    protected static MonkeComponent app;
    protected static boolean sEnableInject = true;

    static {
        Locale.setDefault(LC_EN);
    }

    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    /**
     * Usage:
     * <p>
     * Wallet.app().display().getWidth()
     * Wallet.app().res(); et cetera
     * @return MonkeComponent
     * @see MonkeComponent
     */
    public static MonkeComponent app() {
        return app;
    }

    private static boolean isChinese() {
        return Locale.getDefault() == Locale.CHINA ||
                Locale.getDefault() == Locale.CHINESE ||
                Locale.getDefault() == Locale.SIMPLIFIED_CHINESE ||
                Locale.getDefault() == Locale.TRADITIONAL_CHINESE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if (ENABLE_CRASHLYTICS) {
            Timber.plant(new CrashlyticsTree());
        }

        Rx.init();

        Locale.setDefault(LC_EN);


        if (sEnableInject) {
            app = DaggerMonkeComponent.builder()
                    .monkeModule(new MonkeModule(this, BuildConfig.DEBUG, ENABLE_CRASHLYTICS))
                    .helpersModule(new HelpersModule())
                    .repoModule(new RepoModule())
                    .build();

            app.inject(this);
        }
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }

    public static class Rx {

        public static void init() {
            RxJavaPlugins.setErrorHandler(errorHandler("Unhandled Rx exception!"));
        }

        /**
         * Просто пишет ошибку в лог
         */
        public static Consumer<Throwable> errorHandler() {
            return throwable -> Timber
                    .e(NetworkException.convertIfNetworking(throwable), "Unexpected error");
        }

        /**
         * Просто пишет ошибку в лог
         */
        public static Consumer<Throwable> errorHandler(String message) {
            return throwable -> Timber.e(NetworkException.convertIfNetworking(throwable), message);
        }

        /**
         * Если контекст является ErrorView то выведет ошибку в виде попапа или в человеческом виде
         * При этом запишет ошибку в лог
         */
        public static Consumer<Throwable> errorHandler(final Object viewContext) {
            return throwable -> {
                Throwable ex = NetworkException.convertIfNetworking(throwable);
                if (viewContext instanceof ProgressView) {
                    ((ProgressView) viewContext).hideProgress();
                }
                if (viewContext instanceof ErrorView) {
                    if (ex instanceof NetworkException) {
                        ((ErrorView) viewContext).onError(((NetworkException) ex).getUserMessage());
                    } else {
                        ((ErrorView) viewContext).onError(ex);
                    }

                }
                Timber.e(ex, "Error occurred %s", ex.getMessage());
            };
        }

        /**
         * Если контекст является ErrorView то выведет ошибку в виде попапа или в человеческом виде
         * При этом запишет ошибку в лог
         */
        public static Consumer<Throwable> errorChain(final Object viewContext,
                                                     final Consumer<Throwable> tAction) {
            return throwable -> {
                Throwable ex = NetworkException.convertIfNetworking(throwable);
                if (viewContext instanceof ProgressView) {
                    ((ProgressView) viewContext).hideProgress();
                }
                if (viewContext instanceof ErrorView) {
                    if (ex instanceof NetworkException) {
                        ((ErrorView) viewContext).onError(((NetworkException) ex).getUserMessage());
                    } else {
                        ((ErrorView) viewContext).onError(ex);
                    }
                }

                Timber.e(ex, "Error occurred");
                if (tAction != null) {
                    tAction.accept(throwable);
                }
            };
        }

        /**
         * Выведет человеческую ошибку и запишет ее в лог
         * @param message Если передать NULL то ошибка не выведется
         */
        public static Consumer<Throwable> errorHandler(final Object viewContext, final String message) {
            return throwable -> {
                Throwable ex = NetworkException.convertIfNetworking(throwable);
                if (viewContext instanceof ProgressView) {
                    ((ProgressView) viewContext).hideProgress();
                }
                if (viewContext instanceof ErrorView && message != null) {
                    ((ErrorView) viewContext).onError(message);
                }
                Timber.e(ex, "Error occurred: %s", firstNonNull(message, "[suppressed message]"));
            };
        }

        /**
         * Выведет человеческую ошибку и запишет ее в лог
         */
        public static Consumer<Throwable> errorChain(final Object viewContext, final String message,
                                                     Consumer<Throwable> tAction) {
            return throwable -> {
                Throwable ex = NetworkException.convertIfNetworking(throwable);
                if (viewContext instanceof ProgressView) {
                    ((ProgressView) viewContext).hideProgress();
                }
                if (viewContext instanceof ErrorView && message != null) {
                    ((ErrorView) viewContext).onError(message);
                }
                Timber.e(ex, "Error occurred: %s", message);
                if (tAction != null) {
                    tAction.accept(throwable);
                }
            };
        }
    }

    public static final class CrashlyticsTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.ERROR || priority == Log.WARN) {
                Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
                Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
                Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

                if (t == null) {
                    Crashlytics.logException(new Exception(message));
                } else {
                    Crashlytics.logException(t);
                }
            }
        }
    }
}
