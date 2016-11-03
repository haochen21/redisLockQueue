package redisLockQueue.task;

import java.beans.Transient;
import java.io.Serializable;
import java.util.UUID;

import redisLockQueue.util.MacToHex;

public class Task implements Serializable {

	public String info;

	public String uid;

	public String mac;

	// 每一个任务都有一个唯一标识符
	private final UUID taskUUID = UUID.randomUUID();

	// 当前任务最大执行时间，超过时间，当前任务结点删除
	public static final int TTL = 10;

	private static final long serialVersionUID = -5204860760530042041L;

	private Task() {
		this.uid = taskUUID.toString();
	}

	public Task(String info, String mac) {
		this();
		this.info = info;
		this.mac = MacToHex.MACTOHEX.getHex(mac);
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * 
	 * @return 工作任务队列名称
	 */
	@Transient
	public String getTaskQueueName() {
		return mac + ":queue";
	}

	/**
	 * 
	 * @return 工作任务执行结果消息体名称
	 */
	@Transient
	public String getChannelName() {
		return mac + "-pubsub";
	}

	/**
	 * 
	 * @return 当前工作任务主键名称
	 */
	@Transient
	public String getKeyName() {
		return "terminal:task:" + mac + ":" + uid;
	}

	/**
	 * 
	 * @return 工作队列超时消息名称
	 */
	@Transient
	public String getExpriedChannelName() {
		return "__keyspace@0__:" + mac + ":queue";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Task [info=" + info + ", uid=" + uid + ", mac=" + mac + "]";
	}

}
