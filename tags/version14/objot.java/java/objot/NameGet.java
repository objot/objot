//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/** The name of the encoded field for getting */
@Retention(RetentionPolicy.RUNTIME)
public @interface NameGet
{
	String value();
}
