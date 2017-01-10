package com.example.chad.booksearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by Chad on 12/3/2016.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        final ViewHolder viewHolder;
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.titleView = (TextView) listItemView.findViewById(R.id.title);
            viewHolder.authorView = (TextView) listItemView.findViewById(R.id.author);

            listItemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) listItemView.getTag();
        }

        Book currentBook = getItem(position);

        if (currentBook != null) {
            viewHolder.titleView.setText(currentBook.getTitle());
            viewHolder.authorView.setText(currentBook.getAuthor());
        }

        return listItemView;
    }

    static class ViewHolder {
        TextView titleView;
        TextView authorView;
    }


}
