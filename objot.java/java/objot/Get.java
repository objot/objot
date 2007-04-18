//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/** the annotated field could be get while {@link Getting#go} */
@Retention(RetentionPolicy.RUNTIME)
public @interface Get
{
	/** getting rules about the class specified in {@link Getting#go} */
	Class<?>[] value() default {};
}
