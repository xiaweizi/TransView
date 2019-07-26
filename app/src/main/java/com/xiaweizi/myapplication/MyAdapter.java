package com.xiaweizi.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : xiaweizi
 *     class  : com.xiaweizi.myapplication.MyAdapter
 *     e-mail : 1012126908@qq.com
 *     time   : 2019/07/26
 *     desc   :
 * </pre>
 */
public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mData;

    public MyAdapter(Context context) {
        this.mContext = context;
        mData = new ArrayList<>();
    }

    private int getItemLayout() {
        return R.layout.item_list;
    }

    public void setData(List<String> data) {
        if (data != null) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    public void addData(String bean) {
        if (bean != null) {
            mData.add(bean);
            notifyDataSetChanged();
        }
    }

    public List<String> getData() {
        return this.mData;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(getItemLayout(), null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.bind(mData.get(position));
        return convertView;
    }

    static class ViewHolder {
        private TextView tvContent;
        ViewHolder(View itemView) {
            tvContent = itemView.findViewById(R.id.tv_content);
        }

        void bind(String bean) {
            tvContent.setText(bean);
        }
    }
}