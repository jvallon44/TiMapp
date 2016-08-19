package com.timappweb.timapp.adapters.flexibleadataper;

import android.content.Context;

import com.timappweb.timapp.adapters.flexibleadataper.models.SubUserItem;

import java.util.LinkedList;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 *
 */
public class MyFlexibleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = MyFlexibleAdapter.class.getSimpleName();
	protected Context mContext;//this should not be necessary for view holders


	public MyFlexibleAdapter(Context context) {
		super(new LinkedList<AbstractFlexibleItem>(), context);
		mContext = context;
		setNotifyChangeOfUnfilteredItems(true);
	}


	public boolean addItem(AbstractFlexibleItem item){
		return this.addItem(getItemCount(), item);
	}

	public int removeItems(ExpandableHeaderItem headerItem) {
		int headerPosition = getGlobalPositionOf(headerItem);
		int size = headerItem.getSubItems() != null ? headerItem.getSubItems().size() : 0;
		if (size > 0) {
			if (headerItem.isExpanded()) {
				removeRange(headerPosition + 1, size);
			}
			headerItem.removeSubItems();
		}
		return size;
	}

	public void addSubItem(ExpandableHeaderItem headerItem, SubUserItem item) {
		int size = headerItem.getSubItems() != null ? headerItem.getSubItems().size(): 0;
		if (headerItem.isExpanded()){
			addItemToSection(item, headerItem, size);
		}
		headerItem.addSubItem(item);
		notifyItemChanged(getGlobalPositionOf(headerItem));
	}

	public void expand(ExpandableHeaderItem headerItem) {
		if (headerItem.isExpanded()) return;
		this.expand(getGlobalPositionOf(headerItem));
	}

	public AbstractFlexibleItem getLastItem() {
		return this.getItem(this.getItemCount() - 1);
	}
}