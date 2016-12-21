package dumplingyzr.hearthtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dumplingyzr on 2016/11/28.
 */

public class ClassSelectAdapter extends BaseAdapter {
    private String[] mClassNames = {
            "Warrior", "Shamman", "Rogue",
            "Paladin", "Hunter", "Druid",
            "Warlock", "Mage", "Priest"
    };

    private int[] mClassImages = {
            R.drawable.icon_warrior_64, R.drawable.icon_shaman_64, R.drawable.icon_rogue_64,
            R.drawable.icon_paladin_64, R.drawable.icon_hunter_64, R.drawable.icon_druid_64,
            R.drawable.icon_warlock_64, R.drawable.icon_mage_64, R.drawable.icon_priest_64
    };

    public int getCount() {
        return mClassNames.length;
    }

    public long getItemId(int position) {
        return 0;
    }

    public Object getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_select_item, parent, false);
        } else {
            view = convertView;
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.class_image);
        imageView.setImageResource(mClassImages[position]);
        TextView textView = (TextView) view.findViewById(R.id.class_name);
        textView.setText(mClassNames[position]);
        return view;
    }
}
