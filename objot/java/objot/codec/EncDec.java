//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.codec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * the annotated field could be encoded and decoded while {@link Encoder#go} or
 * {@link Decoder#go}
 */
@Target( { ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EncDec
{
	/**
	 * encoding and decoding rules about the class specified in {@link Encoder#go} or
	 * {@link Decoder#go}
	 */
	Class<?>[] value() default {};
}
