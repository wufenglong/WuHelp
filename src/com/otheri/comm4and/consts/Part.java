package com.otheri.comm4and.consts;

import android.provider.BaseColumns;

public interface Part extends BaseColumns {
	/**
	 * The identifier of the message which this part belongs to.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MSG_ID = "mid";

	/**
	 * The order of the part.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String SEQ = "seq";

	/**
	 * The content type of the part.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CONTENT_TYPE = "ct";

	/**
	 * The name of the part.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String NAME = "name";

	/**
	 * The charset of the part.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CHARSET = "chset";

	/**
	 * The file name of the part.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String FILENAME = "fn";

	/**
	 * The content disposition of the part.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CONTENT_DISPOSITION = "cd";

	/**
	 * The content ID of the part.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String CONTENT_ID = "cid";

	/**
	 * The content location of the part.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String CONTENT_LOCATION = "cl";

	/**
	 * The start of content-type of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String CT_START = "ctt_s";

	/**
	 * The type of content-type of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CT_TYPE = "ctt_t";

	/**
	 * The location(on filesystem) of the binary data of the part.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String _DATA = "_data";

	public static final String TEXT = "text";
}
