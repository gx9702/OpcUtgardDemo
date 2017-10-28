package org.zihao.opc.main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.common.ResultSet;
import org.openscada.opc.dcom.da.IOPCDataCallback;
import org.openscada.opc.dcom.da.ValueData;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.ServerConnectionStateListener;
import org.zihao.opc.utils.PropertyFileManager;

public class Test {

	public static void main(String[] args) throws Exception {
		PropertyFileManager.loadPropertyFile("opcclsid.properties");
		String clsid = PropertyFileManager.getValue("CIMPLICITY");

		System.out.println("clsid=" + clsid);

		if (null == clsid) {
			return;
		}

		final ConnectionInformation ci = new ConnectionInformation();
		ci.setHost("127.1");
		ci.setDomain("localhost");
		ci.setClsid(clsid);

		/** DCOM user */
		ci.setUser("OpcUser");
		ci.setPassword("Pass1234");

		final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
		try {
			server.connect();

			server.addStateListener(new ServerConnectionStateListener() {
				@Override
				public void connectionStateChanged(boolean arg0) {
					System.out.println("connectionStateChanged=" + arg0);

				}
			});

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

			// read item
			for (String s : map.keySet()) {
				Item it = map.get(s);
				JIVariant value = it.read(true).getValue();
				String s1 = value.getObject().toString();
				System.out.println("item:" + it.getId() + ",value:" + s1);
			}

			// write item
			item.setActive(true);
			item.write(new JIVariant("1000"));

			Thread.sleep(1000);
			System.out.println(
					"write item:" + item.getId() + ",value:" + item.read(true).getValue().getObject().toString());

			// data change
			group.attach(new IOPCDataCallback() {

				@Override
				public void writeComplete(int arg0, int arg1, int arg2, ResultSet<Integer> arg3) {
					// TODO Auto-generated method stub

				}

				@Override
				public void readComplete(int arg0, int arg1, int arg2, int arg3,
						KeyedResultSet<Integer, ValueData> arg4) {
					// TODO Auto-generated method stub

				}

				@Override
				public void dataChange(int arg0, int arg1, int arg2, int arg3,
						KeyedResultSet<Integer, ValueData> arg4) {
					System.out.println(
							" arg0=" + arg0 + "\r\n arg1=" + arg1 + "\r\n arg2=" + arg2 + "\r\n arg3=" + arg3 + "\r\n");

					for (int i = 0; i < arg4.size(); i++) {
						Integer key = arg4.get(i).getKey();
						ValueData value = arg4.get(i).getValue();
						try {
							JIVariant value2 = value.getValue();
							String s = value2.getObject().toString();
							System.out.println("key=" + key + ",value=" + s);
						} catch (JIException e) {
							e.printStackTrace();
						}
					}

				}

				@Override
				public void cancelComplete(int arg0, int arg1) {
					// TODO Auto-generated method stub

				}
			});

			while (true) {
				System.out.println("show changed dates:");
				Thread.sleep(3000);
			}

		} catch (final JIException e) {
			e.printStackTrace();
		}
	}
}
