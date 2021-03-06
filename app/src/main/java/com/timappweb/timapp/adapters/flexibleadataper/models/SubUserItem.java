package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.florent37.materialviewpager.Utils;;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.adapters.flexibleadataper.ExpandableHeaderItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SubUserItem extends AbstractModelItem<SubUserItem.UserViewHolder>
		implements ISectionable<SubUserItem.UserViewHolder, IHeader>, IFilterable {

	private static final long 		serialVersionUID 		= 2519281529221244211L;
	private static final String 	TAG 					= "SubUserItem";

	// ---------------------------------------------------------------------------------------------

	private User 					user;
	IHeader 						header;

	// ---------------------------------------------------------------------------------------------

	public SubUserItem(String id, User user) {
		super(id);
		this.user = user;
	}

	public SubUserItem(String id, User user, ExpandableHeaderItem header) {
		this(id, user);
		this.setHeader(header);
	}

	@Override
	public IHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(IHeader header) {
		this.header = header;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_user;
	}

	@Override
	public UserViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new UserViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, UserViewHolder holder, int position, List payloads) {
		//In case of searchText matches with Title or with an SimpleItem's remoteField
		// this will be highlighted
		Context context = holder.itemView.getContext();
		if (adapter.hasSearchText()) {
			eu.davidea.flexibleadapter.utils.Utils.highlightText(context, holder.tvUsername,
					user.getUsername(), adapter.getSearchText(), context.getResources().getColor(R.color.colorAccent));
		} else {
			holder.tvUsername.setText(user.getUsername());
		}

		final String pic = user.getProfilePictureUrl();
		if(pic !=null) {
			Uri uri = Uri.parse(pic);
			holder.ivProfilePicture.setImageURI(uri);
		}
		//holder.tvTime.setText(user.getTimeCreated());
		holder.user = user;

		//This "if-else" is just an example of what you can do with item animation
		/*
		if (adapter.isSelected(position)) {
			adapter.animateView(holder.itemView, position, true);
		} else {
			adapter.animateView(holder.itemView, position, false);
		}*/
	}



	@Override
	public boolean filter(String constraint) {
		return user.getUsername() != null && user.getUsername().toLowerCase().trim().contains(constraint);
	}

	@Override
	public String toString() {
		return "SubItem[" + super.toString() + "]";
	}

	public User getUser() {
		return user;
	}

	public class UserViewHolder extends FlexibleViewHolder{

		User 							user;
		View 							cv;
		TextView 						tvUsername;
		TextView 						tvTime;
		RecyclerView 					rvPostTags;
		SimpleDraweeView 				ivProfilePicture;

		// ---------------------------------------------------------------------------------------------

		UserViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			itemView.setOnClickListener(this);
			cv = itemView.findViewById(R.id.cv);
			tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
			tvTime = (TextView) itemView.findViewById(R.id.tv_time);
			rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
			ivProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.profile_picture);

			rvPostTags.setVisibility(View.GONE);
            Util.setSelectionsBackgroundAdapter(itemView, R.color.white, R.color.colorAccentLight, R.color.LightGrey);
		}

		@Override
		public void onClick(View view) {
			super.onClick(view);
			Log.v(TAG, "Click on user item: " + user);
			IntentsUtils.profile(user);
		}
	}

}