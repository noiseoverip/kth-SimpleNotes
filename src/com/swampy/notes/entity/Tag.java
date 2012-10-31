package com.swampy.notes.entity;

/**
 * Tag entity class, holds tag properties
 * 
 * @author Saulius Alisauskas
 * 
 */
public class Tag {
	/**
	 * Nunber of notes with this tag
	 */
	public int mCount;

	/**
	 * Unique tag id
	 */
	public int mId;

	/**
	 * Tag title
	 */
	public String mName;

	/**
	 * Flag if tag is selected
	 */
	public boolean mSelected;

	public Tag(int id, String name) {
		this(id, name, 0);
	}

	public Tag(int id, String name, int count) {
		this.mId = id;
		this.mName = name;
		this.mCount = count;
	}

	@Override
	public boolean equals(Object o) {
		if (null != o && o instanceof Tag) {
			if (((Tag) o).mId == this.mId) {
				return true;
			}
		}
		return false;
	}
}
