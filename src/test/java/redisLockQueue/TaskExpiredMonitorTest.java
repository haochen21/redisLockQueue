package redisLockQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redisLockQueue.config.ServiceConfig;
import redisLockQueue.task.Task;
import redisLockQueue.task.TaskExpiredMonitor;
import redisLockQueue.task.TaskNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceConfig.class })
public class TaskExpiredMonitorTest {

	@Autowired
	TaskExpiredMonitor taskExpiredMonitor;

	@Autowired
	TaskNode taskNode;

	@Test
	public void expiredMonitor() {
		String mac = "AA:BB:CC:DD:EE:FF";

		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				taskExpiredMonitor.monitor();
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000L);
					Task task = new Task("no-" + 1, mac);
					taskNode.createTaskNode(task);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		});
		t2.start();
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
