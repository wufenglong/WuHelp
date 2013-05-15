package com.otheri.comm4and.model;

import java.util.ArrayList;

/**
 * 一.电话本单元 节点：
 * 
 * id
 * 
 * name,
 * 
 * phone, 电话
 * 
 * email， 邮箱
 * 
 * im， 即时通
 * 
 * postal， 地址
 * 
 * organizations，组织
 * 
 * 
 * 二.phone节点分如下子节点：
 * 
 * work，工作电话
 * 
 * mobile，手机
 * 
 * home，家庭
 * 
 * fax_work，工作传真
 * 
 * fax_home，家庭传真
 * 
 * pager，传呼
 * 
 * other，其它
 * 
 * custom，自定义
 * 
 * customlabel，自定义电话的标题名
 * 
 * 三. email节点分如下子节点：
 * 
 * home，家庭
 * 
 * work，工作
 * 
 * other，其它
 * 
 * custom，自定义
 * 
 * customlabel，自定义email的标题名
 * 
 * 四.IM节点分如下子节点：
 * 
 * aim,
 * 
 * windowlive,
 * 
 * yahoo,
 * 
 * skype,
 * 
 * qq,
 * 
 * googletalk,
 * 
 * icq,
 * 
 * jabber,
 * 
 * 五. postal节点分如下子节点：
 * 
 * home，家庭
 * 
 * work，工作
 * 
 * other，其它
 * 
 * custom，自定义
 * 
 * customlabel，自定义postal的标题名
 * 
 * 六.organizations节点分如下子节点：(组织这三个节点里还有2到3个子节点)
 * 
 * work,{company,title}
 * 
 * other,{company,title}其它
 * 
 * custom,{company,title,customlabel}自定义
 * 
 * 
 * 
 * 
 * */
public class ModelContact {

	/* person_id作为主键，唯一标识联系人 */
	public String person_id;

	/* name和person_id关联 */
	public String name;

	private ArrayList<String[]> array_Dispalyname = new ArrayList<String[]>();

	/**
	 *@return String[]
	 */
	public ArrayList<String[]> getArray_Displayname() {
		return array_Dispalyname;
	}

	public void addArray_Displayname(String[] arrayDisplayname) {
		array_Dispalyname.add(arrayDisplayname);
	}

	/* 电话 */

	private ArrayList<String[]> array_phone = new ArrayList<String[]>();

	/**
	 * @return String[]
	 */
	public ArrayList<String[]> getArray_phone() {
		return array_phone;
	}

	public void addArray_phone(String[] _arrayPhone) {
		array_phone.add(_arrayPhone);
	}

	/* email */

	private ArrayList<String[]> array_email = new ArrayList<String[]>();

	/**
	 * Email
	 * 
	 * @return String[]
	 * 
	 */
	public ArrayList<String[]> getArray_email() {
		return array_email;
	}

	public void addArray_email(String[] strEmail) {
		array_email.add(strEmail);
	}

	/*
	 * IM数据
	 * 
	 * @return String[]
	 */
	private ArrayList<String[]> array_Im = new ArrayList<String[]>();

	public ArrayList<String[]> getArray_Im() {
		return array_Im;
	}

	public void addArray_Im(String[] strIm) {
		array_Im.add(strIm);
	}

	/* 地址 */
	private ArrayList<String[]> array_Address = new ArrayList<String[]>();

	/**
	 * 
	 * @return
	 */
	public ArrayList<String[]> getArray_Address() {
		return array_Address;
	}

	public void addArray_Address(String[] strAddress) {
		array_Address.add(strAddress);
	}

	/* 组织 */
	private ArrayList<String[]> array_Organization = new ArrayList<String[]>();

	/**
	 * 
	 * @return
	 */
	public ArrayList<String[]> getArray_Organization() {
		return array_Organization;
	}

	public void addArray_Organization(String[] strOrganization) {
		array_Organization.add(strOrganization);
	}

	// 关于Notes

	public String noteContent;
	private ArrayList<String[]> array_Notes = new ArrayList<String[]>();

	/**
	 * 
	 * @return
	 */
	public ArrayList<String[]> getArray_Notes() {
		return array_Notes;
	}

	public void addArray_Notes(String[] strNote) {
		array_Notes.add(strNote);
	}

	// 关于NickName

	private ArrayList<String[]> array_Nicknames = new ArrayList<String[]>();

	/**
	 * 
	 * @return
	 */
	public ArrayList<String[]> getArray_Nicknames() {
		return array_Nicknames;
	}

	public void addArray_Nicknames(String[] strNickname) {
		array_Nicknames.add(strNickname);
	}

	// 关于Websites

	private ArrayList<String[]> array_Websites = new ArrayList<String[]>();

	/**
	 * 
	 * @return
	 */
	public ArrayList<String[]> getArray_Websites() {
		return array_Websites;
	}

	public void addArray_Websites(String[] strWebsite) {
		array_Websites.add(strWebsite);
	}
}
