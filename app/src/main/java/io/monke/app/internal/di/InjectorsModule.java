/*
 * Copyright (C) by MinterTeam. 2018
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.monke.app.internal.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import io.monke.app.ime.MonkeKeyboard;
import io.monke.app.internal.di.annotations.ActivityScope;
import io.monke.app.internal.di.annotations.FragmentScope;
import io.monke.app.internal.di.annotations.ServiceScope;
import io.monke.app.services.LiveBalanceService;
import io.monke.app.settings.ui.BackupSeedActivity;
import io.monke.app.settings.ui.SettingsActivity;
import io.monke.app.settings.ui.SettingsFragment;
import io.monke.app.setup.ui.DepositBottomDialog;
import io.monke.app.setup.ui.SetupActivity;
import io.monke.app.splash.ui.SplashActivity;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@Module(includes = AndroidSupportInjectionModule.class)
public interface InjectorsModule {

    @ContributesAndroidInjector
    @ActivityScope
    SetupActivity setupActivityInjector();

    @ContributesAndroidInjector
    @ActivityScope
    SplashActivity splashActivityInjector();

    @ContributesAndroidInjector
    @ServiceScope
    MonkeKeyboard monkeKeyboardInjector();

    @ContributesAndroidInjector
    @ServiceScope
    LiveBalanceService liveBalanceServiceInjector();

    @ContributesAndroidInjector
    @ActivityScope
    SettingsActivity settingsActivityInjector();


    @ContributesAndroidInjector
    @FragmentScope
    DepositBottomDialog depositFragmentInjector();

    @ContributesAndroidInjector
    @FragmentScope
    SettingsFragment settingsFragmentInjector();

    @ContributesAndroidInjector
    @ActivityScope
    BackupSeedActivity backupSeedActivityInjector();
}
