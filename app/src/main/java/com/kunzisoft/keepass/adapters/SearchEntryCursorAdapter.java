/*
 * Copyright 2018 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kunzisoft.keepass.R;
import com.kunzisoft.keepass.database.Database;
import com.kunzisoft.keepass.database.PwEntry;
import com.kunzisoft.keepass.database.PwIcon;
import com.kunzisoft.keepass.database.PwIconFactory;
import com.kunzisoft.keepass.database.PwIconStandard;
import com.kunzisoft.keepass.database.cursor.EntryCursor;
import com.kunzisoft.keepass.icons.IconPackChooser;

import java.util.UUID;

public class SearchEntryCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;
    private Database database;
    private int iconColor;

    public SearchEntryCursorAdapter(Context context, Database database) {
        super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.database = database;

        // Get the icon color
        int[] attrTextColor = {R.attr.textColorInverse};
        TypedArray taTextColor = context.getTheme().obtainStyledAttributes(attrTextColor);
        this.iconColor = taTextColor.getColor(0, Color.WHITE);
        taTextColor.recycle();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = cursorInflater.inflate(R.layout.search_entry, parent ,false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageViewIcon = view.findViewById(R.id.entry_icon);
        viewHolder.textViewTitle = view.findViewById(R.id.entry_text);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Retrieve elements from cursor
        PwIconFactory iconFactory = database.getPwDatabase().getIconFactory();
        PwIcon icon = iconFactory.getIcon(
                new UUID(cursor.getLong(cursor.getColumnIndex(EntryCursor.COLUMN_INDEX_ICON_CUSTOM_UUID_MOST_SIGNIFICANT_BITS)),
                        cursor.getLong(cursor.getColumnIndex(EntryCursor.COLUMN_INDEX_ICON_CUSTOM_UUID_LEAST_SIGNIFICANT_BITS))));
        if (icon.isUnknown()) {
            icon = iconFactory.getIcon(cursor.getInt(cursor.getColumnIndex(EntryCursor.COLUMN_INDEX_ICON_STANDARD)));
            if (icon.isUnknown())
                icon = iconFactory.getKeyIcon();
        }
        String title = cursor.getString( cursor.getColumnIndex(EntryCursor.COLUMN_INDEX_TITLE) );

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Assign image
        if (IconPackChooser.getSelectedIconPack(context).tintable()) {
            database.getDrawFactory().assignDatabaseIconTo(context, viewHolder.imageViewIcon, icon, true, iconColor);
        } else {
            database.getDrawFactory().assignDatabaseIconTo(context, viewHolder.imageViewIcon, icon);
        }
        // Assign title
        viewHolder.textViewTitle.setText(title);
    }

    private static class ViewHolder {
        ImageView imageViewIcon;
        TextView textViewTitle;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return database.searchEntry(constraint.toString());
    }

    public PwEntry getEntryFromPosition(int position) {
        PwEntry pwEntry = null;

        Cursor cursor = this.getCursor();
        if (cursor.moveToFirst()
            &&
            cursor.move(position)) {

            pwEntry = database.createEntry();
            database.populateEntry(pwEntry, cursor);

        }
        return pwEntry;
    }

}
