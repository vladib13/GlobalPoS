package com.pax.tradepaypw.view;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.jemv.demo.R;
import com.pax.tradepaypw.abl.core.AAction;
import com.pax.tradepaypw.abl.core.ATransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenld on 2017/3/14.
 */

public class GridViewAdapter extends BaseAdapter {

    private List<GridItem> itemList;
    private Context context;
    private int pageIndex;
    private int maxItemNumPerPage;

    public GridViewAdapter(Context context, List<?> list, int pageIndex, int maxItemNumPerPage) {
        this.context = context;
        this.pageIndex = pageIndex;
        this.maxItemNumPerPage = maxItemNumPerPage;
        itemList = new ArrayList<GridItem>();
        int list_index = pageIndex * maxItemNumPerPage;
        for (int i = list_index; i < list.size(); i++) {
            itemList.add((GridItem) list.get(i));
        }
    }

    public GridViewAdapter(Context context, List<?> list) {
        this.context = context;
        this.pageIndex = 0;
        this.maxItemNumPerPage = list.size();
        itemList = new ArrayList<GridItem>();
        int list_index = pageIndex * maxItemNumPerPage;
        for (int i = list_index; i < list.size(); i++) {
            itemList.add((GridItem) list.get(i));
        }
    }

    private int columns = 3;

    public void setColumns(int columns) {
        this.columns = columns;
    }

    @Override
    public int getCount() {
        if (maxItemNumPerPage % columns == 0) {
            return maxItemNumPerPage;
        } else {
            return maxItemNumPerPage + (columns - maxItemNumPerPage % columns);
        }
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        ImageView iv = BaseViewHolder.get(convertView, R.id.iv_item);
        TextView tv = BaseViewHolder.get(convertView, R.id.tv_item);

        if (position < itemList.size()) {
            tv.setText(getViewText(position));
            iv.setImageResource(getViewIcon(position));
        }

        return convertView;
    }

    private Integer getViewIcon(int position) {
        Integer resId = 0;
        GridItem holder = itemList.get(position);
        resId = holder.getIcon();
        return resId;
    }

    private String getViewText(int position) {
        String result = null;
        GridItem holder = itemList.get(position);
        result = holder.getName();
        return result;
    }

    public static class GridItem {

        private String name;
        private int icon;
        private ATransaction trans;
        private Class<?> activity;
        private AAction action;
        private Intent intent;

        public GridItem(String name, int icon, ATransaction trans) {
            this.name = name;
            this.icon = icon;
            this.trans = trans;
        }

        public GridItem(String name, int icon, Class<?> act) {
            this.name = name;
            this.icon = icon;
            this.activity = act;
        }

        public GridItem(String name, int icon, AAction action) {
            this.name = name;
            this.icon = icon;
            this.action = action;
        }

        public GridItem(String name, int icon, Intent intent) {
            this.name = name;
            this.icon = icon;
            this.intent = intent;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public ATransaction getTrans() {
            return trans;
        }

        public void setTrans(ATransaction trans) {
            this.trans = trans;
        }

        public Class<?> getActivity() {
            return activity;
        }

        public void setActivity(Class<?> act) {
            this.activity = act;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AAction getAction() {
            return action;
        }

        public void setAction(AAction action) {
            this.action = action;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }

    }

    public static class BaseViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {

            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();

            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }

            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }

            return (T) childView;
        }
    }

}
