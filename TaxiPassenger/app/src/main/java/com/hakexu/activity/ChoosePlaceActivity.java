package com.hakexu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.hakexu.adapter.PlacesAdapter;
import com.hakexu.taxipassenger.R;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlaceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private EditText etPlace;
    private ListView lvPlaces;
    private ArrayList<String> placeList;
    private String title;
    private PlacesAdapter adapter;
    private String city;

    private SuggestionSearch suggestionSearch;
    private OnGetSuggestionResultListener suggestionResultListener;

    /*选择地点的类型*/
    public class PlaceType {
        public static final int INCEPTION = 1; //始发地
        public static final int DESTINATION = 2; //目的地
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_place);

        /*初始化检索对象*/
        suggestionSearch = SuggestionSearch.newInstance();
        suggestionResultListener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                /*搜索结果处理*/
                List<SuggestionResult.SuggestionInfo> suggestionInfos = suggestionResult.getAllSuggestions();
                placeList.clear();
                if (suggestionInfos!=null&&suggestionInfos.size()>0){
                    for(SuggestionResult.SuggestionInfo info:suggestionInfos){
                        placeList.add(info.district+info.key);
                    }
                    adapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(ChoosePlaceActivity.this,"没有匹配的地点",Toast.LENGTH_SHORT).show();
                }
            }
        };
        /*设置监听器*/
        suggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);


        /*获取从TaxiActivity传来的数据*/
        Intent intent = getIntent();
        placeList = new ArrayList<>();
        Bundle bundle = intent.getBundleExtra("data");
        /*获取，设置标题*/
        title = bundle.getString("title");
        city = bundle.getString("city");
        this.setTitle(title);
        /*获取百度sdk提供的候选位置*/
        String[] places = bundle.getStringArray("places");

        etPlace = (EditText) findViewById(R.id.et_place);
        lvPlaces = (ListView) findViewById(R.id.lv_places);


        adapter = new PlacesAdapter(placeList, this);
        lvPlaces.setAdapter(adapter);
        lvPlaces.setOnItemClickListener(this);

        /*填充目的地备选项*/
        if (places != null && places.length > 0) {
            for (String s : places) {
                placeList.add(s);
            }
        }else {
            suggestionSearch.requestSuggestion(new SuggestionSearchOption()
                    .keyword("机场")
                    .city(city));
        }

        etPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*实时检索位置*/
                suggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .keyword(s.toString())
                        .city(city));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("place", placeList.get(position));
        setResult(0, intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        suggestionSearch.destroy();
    }
}
