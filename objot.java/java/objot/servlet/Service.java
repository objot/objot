//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import objot.codec.Codec;


/**
 * For {@link ObjotServlet}, the annotated method is service. the returned object graph
 * must keep unchanged (see {@link Codec#get}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Service
{
}
