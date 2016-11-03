package redisLockQueue.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import redisLockQueue.task.Task;

public enum TaskJson {

	JSON;

	public String getJson(Task task) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String taskJson = mapper.writeValueAsString(task);
			return taskJson;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Task getTask(String taskJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Task task = mapper.readValue(taskJson, Task.class);
			return task;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
