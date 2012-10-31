package com.swampy.notes.utils;

import java.util.List;

import com.swampy.notes.entity.Tag;

/**
 * Singleton class for notes tag selection
 * 
 * @author Saulius Alisauskas
 */
public final class NoteTagsHolder {
	
	/**
	 * Singleton instance
	 */
	private static NoteTagsHolder mInstance;

	/**
	 * Get instance. Create if not created
	 * 
	 * @return
	 */
	public static NoteTagsHolder getTagHolder() {
		if (null == mInstance) {
			mInstance = new NoteTagsHolder();
		}
		return mInstance;
	}
	
	/**
	 * All tags (selected ones are with flagged)
	 */
	public List<Tag> mTags;
	
	/**
	 * Get selected filter items
	 * 
	 * @return
	 */
	public boolean[] getSelectedTags() {
		boolean[] checkedItems = new boolean[mTags.size()];
		for (int i = 0; i < checkedItems.length; i++) {
			checkedItems[i] = (mTags.get(i)).mSelected;
		}
		return checkedItems;
	}

	/**
	 * Get tag in specified position
	 * 
	 * @param position
	 * @return
	 */
	public Tag getTag(int position) {
		return mTags.get(position);
	}	
	
	/**
	 * Get id of tag at a specified position
	 * 
	 * @param position
	 * @return
	 */
	public int getTagId(int position) {
		return mTags.get(position).mId;
	}

	/**
	 * Get filter options (items)
	 * 
	 * @return
	 */
	public CharSequence[] getTagNames() {
		CharSequence[] filter_items_temp = new CharSequence[mTags.size()];
		for (int i = 0; i < filter_items_temp.length; i++) {
			filter_items_temp[i] = mTags.get(i).mName;
		}
		return filter_items_temp;
	}

	/**
	 * Initialize, set selected flag on tags that match tagsToDisplay
	 * @param tagsToDisplay
	 * @param selectedTags
	 */
	public void init(List<Tag> tagsToDisplay, List<Tag> selectedTags) {
		// Refresh tag on each call
		mInstance.mTags = tagsToDisplay;
		
		if (null != selectedTags) {
			for (Tag selectedTag : selectedTags) {
				for (Tag tag : mTags) {
					if (tag.equals(selectedTag)) {
						tag.mSelected = true;
					}
				}
			}
		}
	}

	/**
	 * Reset filter
	 */
	public void reset() {
		for (Tag tag : mTags) {
			tag.mSelected = false;
		}
	}

	/**
	 * Set filter item
	 * 
	 * @param position
	 * @param value
	 */
	public void setItemValue(int position, boolean value) {
		mTags.get(position).mSelected = value;
	}
}
