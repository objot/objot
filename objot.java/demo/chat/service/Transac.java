//
// Copyright 2007 Qianyan Cai
// Under the terms of The GNU General Public License version 2
//
package chat.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;


@Retention(RetentionPolicy.RUNTIME)
public @interface Transac
{
	static final int READ_COMMIT = Connection.TRANSACTION_READ_COMMITTED;
	static final int REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
	static final int SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;

	/**
	 * {@value #READ_COMMIT} or {@value #REPEATABLE_READ} may depend on whether optimistic
	 * lock used
	 */
	static final int DEFAULT = READ_COMMIT;

	/** <= 0 for no database access */
	int level() default DEFAULT;

	boolean readOnly() default false;
}
