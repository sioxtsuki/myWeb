package com.factory;

import com.bl.CommonBl;
import com.utility.DBConnection;

abstract public class CreateFactory {

	/**
	 * 業務ロジック作成
	 *
	 * @return
	 */
	abstract public CommonBl create(DBConnection connection);

	// public static CreateFactory getManagersFactory() {
	// return new ManagersFactory();
	// }

}
