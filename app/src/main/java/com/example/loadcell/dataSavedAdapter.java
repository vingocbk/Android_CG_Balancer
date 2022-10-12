package com.example.loadcell;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class dataSavedAdapter extends BaseAdapter {
    private Context context;
    private int idLayout;
    private List<dataSaved> listDataSaved;
    private int positionSelect = -1;

    public dataSavedAdapter(Context context, int idLayout, List<dataSaved> listDataSaved) {
        this.context = context;
        this.idLayout = idLayout;
        this.listDataSaved = listDataSaved;
    }

    @Override
    public int getCount() {
        if (listDataSaved.size() != 0 && !listDataSaved.isEmpty()) {
            return listDataSaved.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(idLayout, parent, false);
        }
        TextView txtNameDataSaved = (TextView) convertView.findViewById(R.id.txtNameDataSaved);
        TextView txtDataXYZSaved = (TextView) convertView.findViewById(R.id.txtDataXYZSaved);
        final dataSaved data = listDataSaved.get(position);

        if (listDataSaved != null && !listDataSaved.isEmpty()) {
            txtNameDataSaved.setText(data.getName());
            String dataXYZ = "X = " + String.valueOf(data.getX()) + "mm"
                            + ", Y = " + String.valueOf(data.getY()) + "mm"
                            + ", Z = " + String.valueOf(data.getZ()) + "mm";
            txtDataXYZSaved.setText(dataXYZ);
        }
        return convertView;
    }
}
