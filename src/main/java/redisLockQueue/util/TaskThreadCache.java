package redisLockQueue.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum TaskThreadCache {

	CACHE;

	private Set<String> tasks = Collections.synchronizedSet(new HashSet<String>());

	public int add(String taskUid) {
		tasks.add(taskUid);
		return tasks.size();
	}

	public int remove(String taskUid) {
		tasks.remove(taskUid);
		return tasks.size();
	}

	public boolean exist(String taskUid) {
		return tasks.contains(taskUid);
	}
}
