/*
 * Copyright (C) by MinterTeam. 2019
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

package io.monke.app.apis.wrapper;

import network.minter.core.internal.exceptions.NetworkException;
import network.minter.explorer.models.BCExplorerResult;
import retrofit2.HttpException;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ExpGateErrorMapped<Result> extends BCExplorerResult<Result> implements ResultErrorMapper {
    @Override
    public boolean mapError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            // don't handle, we need real error data, not just status info
            return false;
        }

        if (!NetworkException.isNetworkError(throwable)) {
            return false;
        }
        NetworkException e = (NetworkException) NetworkException.convertIfNetworking(throwable);
        error = new BCExplorerResult.ErrorResult();
        error.code = -1;
        error.message = e.getUserMessage();
        result = null;
        statusCode = e.getStatusCode();
        return true;
    }
}
