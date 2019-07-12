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

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.monke.app.internal.helpers.DisplayHelper;
import io.monke.app.internal.helpers.FingerprintHelper;
import io.monke.app.internal.helpers.ImageHelper;
import io.monke.app.internal.helpers.NetworkHelper;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@Module
public class HelpersModule {

    @Provides
    @WalletApp
    public DisplayHelper provideDisplayHelper(Context context) {
        return new DisplayHelper(context);
    }

    @Provides
    @WalletApp
    public ImageHelper provideImageHelper(Context context, DisplayHelper displayHelper) {
        return new ImageHelper(context, displayHelper);
    }

    @Provides
    @WalletApp
    public NetworkHelper provideNetworkHelper(Context context) {
        return new NetworkHelper(context);
    }

    @Provides
    @WalletApp
    public FingerprintHelper provideFingerprintHelper(Context context) {
        return new FingerprintHelper(context);
    }


}
