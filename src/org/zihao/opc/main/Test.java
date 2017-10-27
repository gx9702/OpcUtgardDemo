package org.zihao.opc.main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.Server;

public class Test {

	public static void main(String[] args) throws Exception {
		// create connection information
		final ConnectionInformation ci = new ConnectionInformation();
		ci.setHost("127.0.0.1");
		ci.setDomain("");

		//ci.setProgId(OpcType.CIMPLICITY); // s7 注意 使用progId必须要求dcom配置正确

		// 我们建议使用Clsid，因为使用Grogid时，Openscada的内部处理还是会通过JISystem.getClsidFromProgId(
		// progId )方法将其转换为Clsid，并且还需要进行服务器上用户的权限的高级配置才可以使用。
		// ci.setClsid("B3AF0BF6-4C0C-4804-A122-6F3B160F4397");//kepware
		ci.setClsid("B01241E8-921B-11d2-B43F-204C4F4F5020");// cim

		// DCOM user
		ci.setUser("OpcUser");
		ci.setPassword("Pass1234");

		// create a new server
		final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
		try {
			server.connect();

			Group group = server.addGroup("group");
			group.setActive(true);

			// KEPWARE ITEM
			// final Item item = group.addItem("Test.Device.Test");
			// String[] test = new String[] { "Test.Device.Test1", "Test.Device.Test2" };

			// CIM ITEM
			final Item item = group.addItem("\\\\OPCCONNDEMO\\TEST");
			String[] test = new String[] { "\\\\OPCCONNDEMO\\TEST1", "\\\\OPCCONNDEMO\\TEST2" };

			Map<String, Item> map = new HashMap<String, Item>();
			map = group.addItems(test);

			for (String s : map.keySet()) {
				Item it = map.get(s);
				JIVariant value = it.read(true).getValue();
				String s1 = value.getObject().toString();
				System.out.println("item:" + it.getId() + ",value:" + s1);
			}

			// write value
			item.setActive(true);
			item.write(new JIVariant("1000"));

			Thread.sleep(1000);
			System.out.println("write item:" + item.getId() + ",value:"
					+ item.read(true).getValue().getObject().toString() + " <------==");

		} catch (final JIException e) {
			e.printStackTrace();
		}
	}
}
