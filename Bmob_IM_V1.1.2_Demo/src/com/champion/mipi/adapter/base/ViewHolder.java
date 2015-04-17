package com.champion.mipi.adapter.base;

import android.util.SparseArray;
import android.view.View;

/**
  * @ClassName: ViewHolder
  * @Description: TODO
  */

public class ViewHolder {
	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewArray = (SparseArray<View>) view.getTag();
		if (viewArray == null) {
			viewArray = new SparseArray<View>();
			view.setTag(viewArray);
		}
		View childView = viewArray.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewArray.put(id, childView);
		}
		return (T) childView;
	}
}
