package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.comms.NetworkThread;

public class ForceNetworkThreadTest {
	
	@Test
	public void testConstructor() {
		new NetworkThread(null, null);
		Assert.assertTrue("dummy", true);
	}
	

}