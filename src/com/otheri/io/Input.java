package com.otheri.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

import com.otheri.comm.Array;

public class Input extends InputStream {

	private static int defaultBufferSize = 8192;

	protected volatile byte buf[];
	protected int count;
	protected int pos;
	protected int markpos = -1;
	protected int marklimit;
	private InputStream in;

	public Input(InputStream in) {
		this(in, defaultBufferSize);
	}

	public Input(InputStream in, int bufferSize) {
		this.in = in;
		buf = new byte[bufferSize];
	}

	public void close() throws IOException {
		buf = null;
		in.close();
	}

	private void fill() throws IOException {

		if (markpos < 0)
			pos = 0; /* no mark: throw away the buffer */
		else if (pos >= buf.length) /* no room left in buffer */
			if (markpos > 0) { /* can throw away early part of the buffer */
				int sz = pos - markpos;
				System.arraycopy(buf, markpos, buf, 0, sz);
				pos = sz;
				markpos = 0;
			} else if (buf.length >= marklimit) {
				markpos = -1; /* buffer got too big, invalidate mark */
				pos = 0; /* drop buffer contents */
			} else { /* grow buffer */
				int nsz = pos * 2;
				if (nsz > marklimit)
					nsz = marklimit;
				byte nbuf[] = new byte[nsz];
				System.arraycopy(buf, 0, nbuf, 0, pos);
				buf = nbuf;
			}
		count = pos;
		int n = in.read(buf, pos, buf.length - pos);
		if (n > 0)
			count = n + pos;
	}

	public int read() throws IOException {
		if (pos >= count) {
			fill();
			if (pos >= count)
				return -1;
		}
		return buf[pos++] & 0xff;
	}

	private int read1(byte[] b, int off, int len) throws IOException {
		int avail = count - pos;
		if (avail <= 0) {
			/*
			 * If the requested length is at least as large as the buffer, and
			 * if there is no mark/reset activity, do not bother to copy the
			 * bytes into the local buffer. In this way buffered streams will
			 * cascade harmlessly.
			 */
			if (len >= buf.length && markpos < 0) {
				return in.read(b, off, len);
			}
			fill();
			avail = count - pos;
			if (avail <= 0)
				return -1;
		}
		int cnt = (avail < len) ? avail : len;
		System.arraycopy(buf, pos, b, off, cnt);
		pos += cnt;
		return cnt;
	}

	public synchronized int read(byte b[], int off, int len) throws IOException {
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int n = 0;
		for (;;) {
			int nread = read1(b, off + n, len - n);
			if (nread <= 0)
				return (n == 0) ? nread : n;
			n += nread;
			if (n >= len)
				return n;
			// if not closed but no bytes available, return
			InputStream input = in;
			if (input != null && input.available() <= 0)
				return n;
		}
	}

	public synchronized long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		long avail = count - pos;

		if (avail <= 0) {
			// If no mark position set then don't keep in buffer
			if (markpos < 0)
				return in.skip(n);

			// Fill in buffer to save bytes for reset
			fill();
			avail = count - pos;
			if (avail <= 0)
				return 0;
		}

		long skipped = (avail < n) ? avail : n;
		pos += skipped;
		return skipped;
	}

	public synchronized int available() throws IOException {
		return in.available() + (count - pos);
	}

	public synchronized void mark(int readlimit) {
		marklimit = readlimit;
		markpos = pos;
	}

	public synchronized void reset() throws IOException {
		if (markpos < 0)
			throw new IOException("Resetting to invalid mark");
		pos = markpos;
	}

	public boolean markSupported() {
		return true;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public boolean readBoolean() throws IOException {
		int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}

	public int readUnsignedByte() throws IOException {
		int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return ch;
	}

	public int readUnsignedShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (ch1 << 8) + (ch2 << 0);
	}

	public short readShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	public char readChar() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (char) ((ch1 << 8) + (ch2 << 0));
	}

	public int readInt() throws IOException {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public long readLong() throws IOException {
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public byte[] readBytes() throws IOException {
		int size = readInt();
		byte[] ret = new byte[size];
		in.read(ret);
		return ret;
	}

	public String readUTF8() throws IOException {
		int utflen = readUnsignedShort();
		if (utflen <= 0) {
			return "";
		}
		char str[] = new char[utflen];
		byte bytearr[] = new byte[utflen];
		int c, char2, char3;
		int count = 0;
		int strlen = 0;

		read(bytearr);

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				/* 0xxxxxxx */
				count++;
				str[strlen++] = (char) c;
				break;
			case 12:
			case 13:
				/* 110x xxxx 10xx xxxx */
				count += 2;
				if (count > utflen) {
					throw new UTFDataFormatException();
				}
				char2 = (int) bytearr[count - 1];
				if ((char2 & 0xC0) != 0x80) {
					throw new UTFDataFormatException();
				}
				str[strlen++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
				break;
			case 14:
				/* 1110 xxxx 10xx xxxx 10xx xxxx */
				count += 3;
				if (count > utflen) {
					throw new UTFDataFormatException();
				}
				char2 = (int) bytearr[count - 2];
				char3 = (int) bytearr[count - 1];
				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
					throw new UTFDataFormatException();
				}
				str[strlen++] = (char) (((c & 0x0F) << 12)
						| ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
				break;
			default:
				/* 10xx xxxx, 1111 xxxx */
				throw new UTFDataFormatException();
			}
		}
		// The number of chars produced may be less than utflen
		return new String(str, 0, strlen);
	}

	public Serializable readSerializable() throws IOException {
		String className = readUTF8();
		Serializable serializable = null;
		try {
			serializable = (Serializable) Class.forName(className)
					.newInstance();
			serializable.deserialize(this);
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
		return serializable;
	}

	public byte[] readAll() throws IOException {
		byte[] ret = null;
		Array temp = new Array();
		byte[] buffer = null;
		int readed = 0;
		int remain;
		int rd;
		do {
			if (buffer != null) {
				// buffer未满，则继续填充上次的buffer
				remain = defaultBufferSize - readed;
				rd = in.read(buffer, readed, remain);
				if (rd == -1) {
					if (readed > 0) {
						temp.add(buffer);
					}
					break;
				} else if (rd == remain) {
					temp.add(buffer);
					buffer = null;
				} else {
					// buffer未满
					readed += rd;
				}
			} else {
				buffer = new byte[defaultBufferSize];
				readed = 0;
				rd = in.read(buffer);
				if (rd == -1) {
					break;
				} else if (rd == defaultBufferSize) {
					temp.add(buffer);
					buffer = null;
				} else {
					// buffer未满
					readed += rd;
				}
			}
		} while (true);
		int size = temp.size() * defaultBufferSize - defaultBufferSize + readed;
		ret = new byte[size];
		for (int i = 0; i < temp.size(); i++) {
			byte[] t = (byte[]) temp.get(i);
			if (i == temp.size() - 1) {
				System.arraycopy(t, 0, ret, i * defaultBufferSize, readed);
			} else {
				System.arraycopy(t, 0, ret, i * defaultBufferSize,
						defaultBufferSize);
			}
		}
		return ret;
	}
}
