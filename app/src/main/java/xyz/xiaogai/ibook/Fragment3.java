package xyz.xiaogai.ibook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Fragment3 extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_3, container, false);
		init(view);

		return view;
	}

	private void init(View view) {
//		ListView listView = view.findViewById(R.id.lv_show3);
//		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Toast.makeText(getActivity(),
//						parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
//			}
//		});

	}
}