//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/** the annotated field could be set while {@link Setting#go} */
@Retention(RetentionPolicy.RUNTIME)
public @interface Set
{
	/** setting rules about the class specified in {@link Setting#go} */
	Class<?>[] value() default {};
}
