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
import org.openscada.opc.lib.list.ServerList;
import org.zihao.opc.utils.OpcType;

/**
 * it's a simple example for testing the communication between java and opc
 * using utgard.
 */
public class App {
	private static String host = "127.0.0.1";
	private static String domain = "";
	private static String progId = OpcType.KEPWARE;
	private static String user = "OpcUser";
	private static String password = "***";

	public static void main(String[] args) throws Exception {
		Map<String, Item> map = new HashMap<String, Item>();

		/** get clsid */
		ServerList serverList = new ServerList(host, user, password, domain);
		String clsIdFromProgId = serverList.getClsIdFromProgId(progId);
		
		final ConnectionInformation ci = new ConnectionInformation();
		ci.setHost(host);
		ci.setDomain(domain);
		ci.setClsid(clsIdFromProgId);
		ci.setUser(user);
		ci.setPassword(password);
		
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
			
			/** read item each 2s */
			while (true) {
				Thread.sleep(2000);

				read(map);
			}
			
		} catch (final JIException e) {
			e.printStackTrace();
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
