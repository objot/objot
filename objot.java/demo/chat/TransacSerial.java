//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** service in transaction with serializable isolation */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TransacSerial
{
}
