package chat.service;

public @interface Transac
{
	boolean need() default true;

	boolean readOnly() default false;

	boolean serial() default false;
}
