package com.otheri.comm;

public class Array {

	private static final int DEFAULT = 8;
	private Object[] objs;
	private int count;
	private int increase;

	public Array(int size, int increase) {

		objs = new Object[size];
		count = 0;
		this.increase = increase;
	}

	public Array(int size) {
		this(size, DEFAULT);
	}

	public Array() {
		this(DEFAULT, DEFAULT);
	}

	public synchronized int size() {
		return count;
	}

	public synchronized void add(Object obj) {
		int len = objs.length;
		if (count >= len) {
			Object[] newArray = new Object[len + increase];
			System.arraycopy(objs, 0, newArray, 0, len);
			objs = newArray;
		}
		objs[count++] = obj;
	}

	public synchronized void addArray(Array array) {
		int len = objs.length;
		int size = array.size();
		if (count + size >= len) {
			Object[] newArray = new Object[len + size];
			System.arraycopy(objs, 0, newArray, 0, len);
			objs = newArray;
		}
		for (int i = 0; i < size; i++) {
			objs[count++] = array.objs[i];
		}
	}

	public synchronized Object get(int index) {
		return objs[index];
	}

	public synchronized void set(int index, Object obj) {
		objs[index] = obj;
	}

	public synchronized void del(int index) {
		int j = count - index - 1;
		if (j > 0) {
			System.arraycopy(objs, index + 1, objs, index, j);
		}
		count--;
		objs[count] = null;
	}

	public synchronized void insert(int index, Object obj) {
		if (index < 0 || index > count) {
			throw new ArrayIndexOutOfBoundsException(index);
		} else if (index == count) {
			add(obj);
		} else {
			int len = objs.length;
			if (count >= len) {
				Object[] newArray = new Object[len + increase];
				System.arraycopy(objs, 0, newArray, 0, index);
				System.arraycopy(objs, index, newArray, index + 1, count
						- index);
				objs = newArray;
			} else {
				System.arraycopy(objs, index, objs, index + 1, count - index);
			}
			objs[index] = obj;
			count++;
		}
	}

	public synchronized void clear() {
		for (int i = 0; i < count; i++) {
			objs[i] = null;
		}
		count = 0;
	}

	public synchronized int indexOf(Object obj, int startIndex) {
		if (obj == null) {
			for (int i = startIndex; i < count; i++) {
				if (objs[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = startIndex; i < count; i++) {
				if (obj.equals(objs[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public synchronized int lastIndexOf(Object obj, int startIndex) {
		if (startIndex >= count) {
			throw new ArrayIndexOutOfBoundsException(startIndex);
		}
		if (obj == null) {
			for (int i = startIndex; i >= 0; i--) {
				if (objs[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = startIndex; i >= 0; i--) {
				if (obj.equals(objs[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public synchronized Array subArray(int start, int length) {
		if (start < 0 || start >= count) {
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if (length <= 0 || start + length > count) {
			throw new ArrayIndexOutOfBoundsException(length);
		}
		Array ret = new Array(length);
		System.arraycopy(objs, start, ret.objs, 0, length);
		ret.count = length;
		return ret;
	}

	/**
	 * Removes the object at the top of this stack and returns that object as
	 * the value of this function.
	 * 
	 * @return The object at the top of this stack (the last item of the Array).
	 */
	public synchronized Object pop() {
		if (count > 0) {
			Object ret = objs[count - 1];
			del(count - 1);
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Removes the object at the bottom of this stack and returns that object as
	 * the value of this function.
	 * 
	 * 小心，性能低下
	 * 
	 * @return The object at the bottom of this stack (the first item of the
	 *         Array).
	 */
	public synchronized Object poll() {
		if (count > 0) {
			Object ret = objs[0];
			del(0);
			return ret;
		} else {
			return null;
		}
	}

	public synchronized boolean empty() {
		return count <= 0;
	}

	public synchronized void reverse() {
		int temp = count / 2;
		int last;
		for (int i = 0; i < temp; i++) {
			last = count - 1 - i;
			Object obj = objs[last];
			objs[last] = objs[i];
			objs[i] = obj;
		}
	}
}
