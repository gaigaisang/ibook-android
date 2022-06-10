package xyz.xiaogai.ibook;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_2, container, false);
		init(view);

		return view;
	}

	private void init(View view) {
		ListView listView = view.findViewById(R.id.lv_show2);
		registerForContextMenu(listView);
	}

	@Override
	public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
									@Nullable ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.menu_context, menu);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_modify:
				Toast.makeText(getActivity(),
						"MODIFY", Toast.LENGTH_SHORT).show();
				break;
			case R.id.action_info:
				Toast.makeText(getActivity(),
						"INFO", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onContextItemSelected(item);
	}
}
