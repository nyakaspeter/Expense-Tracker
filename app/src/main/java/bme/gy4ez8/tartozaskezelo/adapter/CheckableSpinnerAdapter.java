package bme.gy4ez8.tartozaskezelo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import bme.gy4ez8.tartozaskezelo.R;

public class CheckableSpinnerAdapter<T> extends BaseAdapter {

    public static class SpinnerItem<T> {

        public String txt;
        public T item;

        public SpinnerItem(T t, String s) {
            item = t;
            txt = s;
        }
    }

    private Context context;
    public Set<T> selected_items;
    public List<SpinnerItem<T>> all_items;
    public String headerText;
    public Spinner sp_parent;
    public TextView tv_kinek;

    public CheckableSpinnerAdapter(Context context,
                            String headerText,
                            List<SpinnerItem<T>> all_items,
                            Set<T> selected_items) {
        this.context = context;
        this.headerText = headerText;
        this.all_items = all_items;
        this.selected_items = selected_items;
    }

    @Override
    public int getCount() {
        return all_items.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if( position < 1 ) {
            return null;
        }
        else {
            return all_items.get(position-1);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    @NonNull
    public View getView(int position, View convertView, @NonNull final ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null ) {
            LayoutInflater layoutInflator = LayoutInflater.from(context);
            convertView = layoutInflator.inflate(R.layout.checkable_spinner_item, parent, false);

            holder = new ViewHolder();
            holder.mTextView = convertView.findViewById(R.id.text);
            holder.mCheckBox = convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if( position == 0 ) {
            holder.mCheckBox.setVisibility(View.GONE);
            holder.mTextView.setText(headerText);
        }
        else {
            final int listPos = position - 1;
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mTextView.setText(all_items.get(listPos).txt);

            final T item = all_items.get(listPos).item;
            boolean isSel = selected_items.contains(item);

            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(isSel);

            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if( isChecked ) {
                        selected_items.add(item);
                    }
                    else {
                        selected_items.remove(item);
                    }

                    if(selected_items.isEmpty()) {
                        headerText = "Kinek";
                        if (tv_kinek != null) tv_kinek.setVisibility(View.GONE);
                    }
                    else {
                        headerText = TextUtils.join(", ", selected_items);
                        if (tv_kinek != null) tv_kinek.setVisibility(View.VISIBLE);
                    }
                    if (sp_parent != null) sp_parent.setAdapter(CheckableSpinnerAdapter.this);
                }
            });

            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mCheckBox.toggle();
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView mTextView;
        private CheckBox mCheckBox;
    }
}
