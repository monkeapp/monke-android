package io.monke.app.internal.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import androidx.annotation.Dimension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * AtlasAndroid. 2017
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@Documented
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
@Dimension(unit = Dimension.DP)
public @interface Dp {
}
