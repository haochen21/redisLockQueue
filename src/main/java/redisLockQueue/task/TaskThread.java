package redisLockQueue.task;

import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskThread implements Callable<Boolean> {

	private Task task;

	private final static Logger logger = LoggerFactory.getLogger(TaskThread.class);

	public TaskThread(Task task) {
		super();
		this.task = task;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			logger.info("begin task thread:" + task);

			// ---------------- 工作执行方法----------------------------
			int excuteTime = new Random().nextInt(5) + 1;
			Thread.sleep(excuteTime * 1000);

			return true;
		} catch (Exception ex) {
			logger.info("task thread error:" + task, ex);
			return false;
		}
	}

}
