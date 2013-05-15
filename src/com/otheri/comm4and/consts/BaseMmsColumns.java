package com.otheri.comm4and.consts;

import android.provider.BaseColumns;

public interface BaseMmsColumns extends BaseColumns {

	public static final int MESSAGE_BOX_ALL = 0;
	public static final int MESSAGE_BOX_INBOX = 1;
	public static final int MESSAGE_BOX_SENT = 2;
	public static final int MESSAGE_BOX_DRAFTS = 3;
	public static final int MESSAGE_BOX_OUTBOX = 4;

	/**
	 * The date the message was sent.
	 * <P>
	 * Type: INTEGER (long)
	 * </P>
	 */
	public static final String DATE = "date";

	/**
	 * The box which the message belong to, for example, MESSAGE_BOX_INBOX.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MESSAGE_BOX = "msg_box";

	/**
	 * Has the message been read.
	 * <P>
	 * Type: INTEGER (boolean)
	 * </P>
	 */
	public static final String READ = "read";

	/**
	 * Indicates whether this message has been seen by the user. The "seen" flag
	 * will be used to figure out whether we need to throw up a statusbar
	 * notification or not.
	 */
	public static final String SEEN = "seen";

	/**
	 * The Message-ID of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MESSAGE_ID = "m_id";

	/**
	 * The subject of the message, if present.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String SUBJECT = "sub";

	/**
	 * The character set of the subject, if present.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String SUBJECT_CHARSET = "sub_cs";

	/**
	 * The Content-Type of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CONTENT_TYPE = "ct_t";

	/**
	 * The Content-Location of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CONTENT_LOCATION = "ct_l";

	/**
	 * The address of the sender.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String FROM = "from";

	/**
	 * The address of the recipients.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String TO = "to";

	/**
	 * The address of the cc. recipients.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CC = "cc";

	/**
	 * The address of the bcc. recipients.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String BCC = "bcc";

	/**
	 * The expiry time of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String EXPIRY = "exp";

	/**
	 * The class of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MESSAGE_CLASS = "m_cls";

	/**
	 * The type of the message defined by MMS spec.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MESSAGE_TYPE = "m_type";

	/**
	 * The version of specification that this message conform.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MMS_VERSION = "v";

	/**
	 * The size of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MESSAGE_SIZE = "m_size";

	/**
	 * The priority of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String PRIORITY = "pri";

	/**
	 * The read-report of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String READ_REPORT = "rr";

	/**
	 * Whether the report is allowed.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String REPORT_ALLOWED = "rpt_a";

	/**
	 * The response-status of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String RESPONSE_STATUS = "resp_st";

	/**
	 * The status of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String STATUS = "st";

	/**
	 * The transaction-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String TRANSACTION_ID = "tr_id";

	/**
	 * The retrieve-status of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String RETRIEVE_STATUS = "retr_st";

	/**
	 * The retrieve-text of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String RETRIEVE_TEXT = "retr_txt";

	/**
	 * The character set of the retrieve-text.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String RETRIEVE_TEXT_CHARSET = "retr_txt_cs";

	/**
	 * The read-status of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String READ_STATUS = "read_status";

	/**
	 * The content-class of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String CONTENT_CLASS = "ct_cls";

	/**
	 * The delivery-report of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String DELIVERY_REPORT = "d_rpt";

	/**
	 * The delivery-time-token of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String DELIVERY_TIME_TOKEN = "d_tm_tok";

	/**
	 * The delivery-time of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String DELIVERY_TIME = "d_tm";

	/**
	 * The response-text of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String RESPONSE_TEXT = "resp_txt";

	/**
	 * The sender-visibility of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String SENDER_VISIBILITY = "s_vis";

	/**
	 * The reply-charging of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String REPLY_CHARGING = "r_chg";

	/**
	 * The reply-charging-deadline-token of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String REPLY_CHARGING_DEADLINE_TOKEN = "r_chg_dl_tok";

	/**
	 * The reply-charging-deadline of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String REPLY_CHARGING_DEADLINE = "r_chg_dl";

	/**
	 * The reply-charging-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String REPLY_CHARGING_ID = "r_chg_id";

	/**
	 * The reply-charging-size of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String REPLY_CHARGING_SIZE = "r_chg_sz";

	/**
	 * The previously-sent-by of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String PREVIOUSLY_SENT_BY = "p_s_by";

	/**
	 * The previously-sent-date of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String PREVIOUSLY_SENT_DATE = "p_s_d";

	/**
	 * The store of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String STORE = "store";

	/**
	 * The mm-state of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MM_STATE = "mm_st";

	/**
	 * The mm-flags-token of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MM_FLAGS_TOKEN = "mm_flg_tok";

	/**
	 * The mm-flags of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MM_FLAGS = "mm_flg";

	/**
	 * The store-status of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String STORE_STATUS = "store_st";

	/**
	 * The store-status-text of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String STORE_STATUS_TEXT = "store_st_txt";

	/**
	 * The stored of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String STORED = "stored";

	/**
	 * The totals of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String TOTALS = "totals";

	/**
	 * The mbox-totals of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MBOX_TOTALS = "mb_t";

	/**
	 * The mbox-totals-token of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MBOX_TOTALS_TOKEN = "mb_t_tok";

	/**
	 * The quotas of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String QUOTAS = "qt";

	/**
	 * The mbox-quotas of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MBOX_QUOTAS = "mb_qt";

	/**
	 * The mbox-quotas-token of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MBOX_QUOTAS_TOKEN = "mb_qt_tok";

	/**
	 * The message-count of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String MESSAGE_COUNT = "m_cnt";

	/**
	 * The start of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String START = "start";

	/**
	 * The distribution-indicator of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String DISTRIBUTION_INDICATOR = "d_ind";

	/**
	 * The element-descriptor of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String ELEMENT_DESCRIPTOR = "e_des";

	/**
	 * The limit of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String LIMIT = "limit";

	/**
	 * The recommended-retrieval-mode of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String RECOMMENDED_RETRIEVAL_MODE = "r_r_mod";

	/**
	 * The recommended-retrieval-mode-text of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String RECOMMENDED_RETRIEVAL_MODE_TEXT = "r_r_mod_txt";

	/**
	 * The status-text of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String STATUS_TEXT = "st_txt";

	/**
	 * The applic-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String APPLIC_ID = "apl_id";

	/**
	 * The reply-applic-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String REPLY_APPLIC_ID = "r_apl_id";

	/**
	 * The aux-applic-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AUX_APPLIC_ID = "aux_apl_id";

	/**
	 * The drm-content of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String DRM_CONTENT = "drm_c";

	/**
	 * The adaptation-allowed of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String ADAPTATION_ALLOWED = "adp_a";

	/**
	 * The replace-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String REPLACE_ID = "repl_id";

	/**
	 * The cancel-id of the message.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CANCEL_ID = "cl_id";

	/**
	 * The cancel-status of the message.
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String CANCEL_STATUS = "cl_st";

	/**
	 * The thread ID of the message
	 * <P>
	 * Type: INTEGER
	 * </P>
	 */
	public static final String THREAD_ID = "thread_id";

	/**
	 * Has the message been locked?
	 * <P>
	 * Type: INTEGER (boolean)
	 * </P>
	 */
	public static final String LOCKED = "locked";

	/**
	 * Meta data used externally.
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String META_DATA = "meta_data";
}