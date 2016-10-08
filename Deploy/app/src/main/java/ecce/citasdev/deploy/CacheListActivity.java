package ecce.citasdev.deploy;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ecce.citasdev.deploy.utils.CacheDetails;

public class CacheListActivity extends ListActivity {

    private static final String TAG = CacheListActivity.class.getSimpleName();

    private ArrayList<CacheDetails> _cacheItems = new ArrayList<CacheDetails>();
    private ArrayAdapter<CacheDetails> _cacheAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sink_list);

        _cacheAdapter = new ArrayAdapter<CacheDetails>(this, android.R.layout.simple_list_item_1, _cacheItems);
        setListAdapter(_cacheAdapter);
        ListView cacheList = (ListView) findViewById(android.R.id.list);


        CacheDetails dummy = new CacheDetails("addr1", "San Jose", "deactivated");
        _cacheItems.add(dummy);

        cacheList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CacheDetails cacheDetails = _cacheAdapter.getItem(i);
                Intent intent = new Intent(CacheListActivity.this, CacheDetailsActivity.class);
                startActivity(intent);
            }
        });
    }



}
