package chat.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Transac
{
	boolean need() default true;

	boolean readOnly() default false;

	boolean serial() default false;
}
