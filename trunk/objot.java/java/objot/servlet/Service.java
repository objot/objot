//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package objot.servlet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import objot.Getting;


/**
 * Mark a method as service method for {@link ObjotServlet}, the returned object graph
 * must keep unchanged (see {@link Getting#go(objot.Objot, Class, Object)}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Service
{
}
