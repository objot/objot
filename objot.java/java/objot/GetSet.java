//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** the annotated field could be get while {@link Getting#go} or {@link Setting#go} */
@Target( { ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GetSet
{
	/**
	 * getting and setting rules about the class specified in {@link Getting#go} or
	 * {@link Setting#go}
	 */
	Class<?>[] value() default {};
}
