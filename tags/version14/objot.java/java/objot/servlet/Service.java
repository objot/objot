//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import objot.Objot;


/**
 * For {@link ObjotServlet}, the annotated method is service. the returned object graph
 * must keep unchanged (see {@link Objot#get}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Service
{
}