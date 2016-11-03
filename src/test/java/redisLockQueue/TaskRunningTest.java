package redisLockQueue;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.pubsub.SendMessage;
import redisLockQueue.task.Task;
import redisLockQueue.task.TaskHandle;
import redisLockQueue.task.TaskNode;

public class TaskRunningTest implements MessageListener {

	@Autowired
	private TerminalJedisPool terminalJedisPool;

	@Autowired
	private SendMessage sendMessage;

	@Autowired
	TaskNode taskNode;

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				if (objectMessage.getObject() instanceof Task) {
					Task task = (Task) objectMessage.getObject();
					TaskHandle handle = new TaskHandle(terminalJedisPool, taskNode, task, sendMessage, true);
					handle.start();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(TaskRunningConfig.class);
		ctx.refresh();
	}
}
