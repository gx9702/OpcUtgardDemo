package org.zihao.opc.main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIUnsigned;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.Server;
import org.zihao.opc.utils.OpcType;
import org.zihao.opc.utils.PropertyFileManager;

/**
 * it's a simple example for testing the communication between java and opc
 * using utgard.
 */
public class App {

	public static void main(String[] args) throws Exception {
		Map<String, Item> map = new HashMap<String, Item>();

		/** get clsid */
		PropertyFileManager.loadPropertyFile("opcclsid.properties");
		String clsid = PropertyFileManager.getValue(OpcType.KEPWARE);

		if (null == clsid) {
			return;
		}

		final ConnectionInformation ci = new ConnectionInformation();
		ci.setHost("localhost");
		ci.setDomain("localhost");
		ci.setClsid(clsid);

		/** enter your user info */
		ci.setUser("OpcUser");
		ci.setPassword("Pass1234");

		final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
		try {
			server.connect();

			Group group = server.addGroup("group");
			group.setActive(true);

			/** KEPWARE ITEM */
			final Item item = group.addItem("Test.Device.Test");
			String[] test = new String[] { "Test.Device.Test1", "Test.Device.Test2" };

			/** CIM ITEM */
			// final Item item = group.addItem("\\\\OPCCONNDEMO\\TEST");
			// String[] test = new String[] { "\\\\OPCCONNDEMO\\TEST1",
			// "\\\\OPCCONNDEMO\\TEST2" };

			map = group.addItems(test);

			/** read item */
			read(map);

			/** write item */
			item.setActive(true);
			item.write(new JIVariant("999"));

		} catch (final JIException e) {
			e.printStackTrace();
		}

		/** read item each 2s */
		while (true) {
			Thread.sleep(2000);

			read(map);
		}
	}

	private static void read(Map<String, Item> map) throws JIException {
		for (String s : map.keySet()) {
			Item it = map.get(s);
			JIVariant jiVariant = it.read(true).getValue();
			if (!jiVariant.isNull()) {
				Object object = jiVariant.getObject();
				int intValue = -1;

				/** each opc type has it's Value type */
				if (object instanceof IJIUnsigned) {
					IJIUnsigned objectAsUnsigned = jiVariant.getObjectAsUnsigned();
					intValue = objectAsUnsigned.getValue().intValue();

				} else if (object instanceof Number) {
					intValue = ((Number) object).intValue();
				}
				System.out.println("item:" + it.getId() + ",value:" + intValue);
			}
		}
	}
}
