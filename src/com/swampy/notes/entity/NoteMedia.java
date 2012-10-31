package com.swampy.notes.entity;

import android.graphics.Bitmap;

/**
 * Note media entity
 * 
 * @author Saulius ALisauskas
 * 
 */
public class NoteMedia {
	public enum Type {
		IMG, SOUND
	}

	private Object mData;
	private int mId;
	private String mTitle;
	private String mType;

	/**
	 * Constructor complete.
	 * 
	 * @param id
	 * @param title
	 * @param type
	 * @param data
	 */
	public NoteMedia(int id, String title, Type type, Object data) {
		this.mId = id;
		this.mTitle = title;
		this.mType = type.toString();
		this.mData = data;
	}

	/**
	 * Convenience constructor
	 * 
	 * @param title
	 * @param type
	 * @param data
	 */
	public NoteMedia(String title, Type type, Object data) {
		this(-1, title, type, data);
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return mData;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return mType;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Object data) {
		this.mData = data;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.mId = id;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.mTitle = title;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.mType = type;
	}

	@Override
	public String toString() {
		return "NoteMedia id:" + mId + " title:" + mTitle + " type:" + mType + " data size:"
				+ ((mData instanceof Bitmap) ? ((Bitmap) mData).getRowBytes() : "0");
	}
}
