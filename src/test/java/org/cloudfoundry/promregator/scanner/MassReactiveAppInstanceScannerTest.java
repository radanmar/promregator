package org.cloudfoundry.promregator.scanner;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.cloudfoundry.promregator.config.Target;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.prometheus.client.CollectorRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MockedMassReactiveAppInstanceScannerSpringApplication.class)
@TestPropertySource(locations="default.properties")
public class MassReactiveAppInstanceScannerTest {

	@Autowired
	private AppInstanceScanner appInstanceScanner;
	
	@AfterClass
	public static void releaseInternalMetrics() {
		CollectorRegistry.defaultRegistry.clear();
	}
	
	@Test
	public void testPerformance() {
		List<ResolvedTarget> targets = new LinkedList<>();
		
		ResolvedTarget t = null;
		
		final int numberOfApps = 10000;
		
		for (int i = 0;i<numberOfApps;i++) {
			t = new ResolvedTarget();
			t.setOrgName("unittestorg");
			t.setSpaceName("unittestspace");
			t.setApplicationName("testapp"+i);
			t.setPath("/testpath");
			t.setProtocol("http");
			targets.add(t);
		}
		
		Instant start = Instant.now();
		
		List<Instance> result = this.appInstanceScanner.determineInstancesFromTargets(targets, null, null);
		
		Instant stop = Instant.now();
		
		Assert.assertEquals(numberOfApps*10, result.size());
		
		// test to be faster than 6 seconds
		Duration d = Duration.between(start, stop);
		Assert.assertTrue(d.minusSeconds(6).isNegative());
	}

	@Test
	public void testPerformanceWithFilter() {
		List<Target> targets = new LinkedList<>();
		
		Target t = null;
		
		final int numberOfApps = 10000;
		
		for (int i = 0;i<numberOfApps;i++) {
			t = new Target();
			t.setOrgName("unittestorg");
			t.setSpaceName("unittestspace");
			t.setApplicationName("testapp"+i);
			t.setPath("/testpath");
			t.setProtocol("http");
			targets.add(t);
		}
		
		Instant start = Instant.now();
		
		List<Instance> result = this.appInstanceScanner.determineInstancesFromTargets(targets, null, instance -> {
			// filters out all instances, but only the instance "0" is kept
			
			if (instance.getInstanceId().endsWith(":0"))
				return true;
			
			return false;
		});
		
		Instant stop = Instant.now();
		
		Assert.assertEquals(numberOfApps*1, result.size());
		
		// test to be faster than 6 seconds
		Duration d = Duration.between(start, stop);
		Assert.assertTrue(d.minusSeconds(6).isNegative());
	}

}
