package redisLockQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import redisLockQueue.task.Task;

public class SendTaskTest {

	@Autowired
	private SendTaskJms sendTaskJms;

	public SendTaskTest() {

	}

	public void runningTask() {
		String mac = "AA:BB:CC:DD:EE:FF";
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(500);
				Task task = new Task("no-" + i, mac);
				sendTaskJms.sendingTask(task);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(SendJmsConfig.class);
		ctx.refresh();
		SendTaskTest sendTaskTest = (SendTaskTest) ctx.getBean("sendTaskTest");
		sendTaskTest.runningTask();
		System.exit(0);
	}
}
