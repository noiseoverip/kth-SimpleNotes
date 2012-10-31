package com.swampy.notes.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Note entity class
 * 
 * @author Saulius Alisauskas
 * 
 */
public class Note {
	private Calendar mCalendar; // due calendar
	private String mDate;
	private int mId;
	private List<NoteMedia> mMedia;
	private List<Tag> mTags;
	private String mText;
	private String mTitle;

	/**
	 * @return the calendar
	 */
	public Calendar getCalendar() {
		return mCalendar;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return mDate;
	}

	public String getDueDate() {
		if (mCalendar != null) {
			return new SimpleDateFormat("yy-MM-dd").format(mCalendar.getTime());
		}
		return null;
	}

	public String getDueTime() {
		if (mCalendar != null) {
			return new SimpleDateFormat("HH:mm").format(mCalendar.getTime());
		}
		return null;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return the media
	 */
	public List<NoteMedia> getMedia() {
		if (mMedia == null) {
			mMedia = new ArrayList<NoteMedia>();
		}
		return mMedia;
	}

	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		if (null == mTags) {
			mTags = new ArrayList<Tag>();
		}
		return mTags;
	}

	/**
	 * @return the message
	 */
	public String getText() {
		return mText;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param calendar
	 *            the calendar to set
	 */
	public void setCalendar(Calendar calendar) {
		this.mCalendar = calendar;
	}

	public void setDate(long timestamp) {
		// TODO: formatter could be static
		this.mDate = new SimpleDateFormat("yy-MM-dd HH:mm").format(new Date(timestamp));
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.mDate = date;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.mId = id;
	}

	/**
	 * @param media
	 *            the media to set
	 */
	public void setMedia(List<NoteMedia> media) {
		this.mMedia = media;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.mTags = tags;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setText(String text) {
		this.mText = text;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.mTitle = title;
	}

	@Override
	public String toString() {
		return "Note id:" + mId + " title:" + mTitle + " text:" + mText + " date:" + mDate + " dueDate:" + mCalendar
				+ " media count:" + ((mMedia != null) ? mMedia : "0") + " tags:" + ((mTags != null) ? mTags : "0");
	}

}
